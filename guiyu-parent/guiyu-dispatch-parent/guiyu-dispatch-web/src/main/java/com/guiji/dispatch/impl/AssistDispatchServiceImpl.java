package com.guiji.dispatch.impl;

import com.google.common.collect.Lists;
import com.guiji.calloutserver.api.ICallPlan;
import com.guiji.clm.api.LineMarketRemote;
import com.guiji.clm.model.SipLineVO;
import com.guiji.common.exception.GuiyuException;
import com.guiji.component.lock.DistributedLockHandler;
import com.guiji.component.lock.Lock;
import com.guiji.component.result.Result;
import com.guiji.dispatch.constant.RedisConstant;
import com.guiji.dispatch.dao.DispatchPlanMapper;
import com.guiji.dispatch.dao.entity.DispatchPlan;
import com.guiji.dispatch.dao.entity.DispatchPlanExample;
import com.guiji.dispatch.exception.DispatchCodeExceptionEnum;
import com.guiji.dispatch.service.AssistAgentService;
import com.guiji.dispatch.service.AssistDispatchService;
import com.guiji.dispatch.service.GetApiService;
import com.guiji.dispatch.util.Constant;
import com.guiji.robot.api.IRobotRemote;
import com.guiji.robot.model.UserResourceCache;
import com.guiji.utils.BeanUtil;
import com.guiji.utils.RedisUtil;
import com.guiji.ws.api.WsOnlineUserApi;
import com.guiji.ws.model.WsSenceEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Zhouzl
 * @date 2019年08月19日
 * @since 1.0
 */
@Slf4j
@Service
public class AssistDispatchServiceImpl implements AssistDispatchService {
    @Autowired
    private DispatchPlanMapper dispatchPlanMapper;
    @Autowired
    private GetApiService getApiService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private WsOnlineUserApi wsOnlineUserApi;
    @Autowired
    private LineMarketRemote lineMarketRemote;
    @Autowired
    private IRobotRemote robotRemote;
    @Autowired
    private ICallPlan callPlanCenter;
    @Autowired
    private DistributedLockHandler lockHandler;
    @Autowired
    private AssistAgentService assistAgentService;

    @Override
    public Result.ReturnData dispatch(Long userId) {
        Lock dispatchLock = new Lock(RedisConstant.RedisConstantKey.DISPATCH_LOCK + userId, userId.toString());
        if (!lockHandler.tryLock(dispatchLock, 100L)) {
            //避免重复调度(比如坐席提交和呼叫完成自动调度同时触发)
            return Result.error(DispatchCodeExceptionEnum.REPEATED_DISPATCH.getErrorCode());
        }
        try {
            return doDispatch(userId);
        } catch (GuiyuException e) {
            log.error("坐席[{}]协呼调度条件不满足:{}", userId, e.getErrorMessage());
            return Result.error(e.getErrorCode());
        } finally {
            lockHandler.releaseLock(dispatchLock);
        }
    }

    @Override
    public void agentBroken(Long userId) {
        Lock lock = new Lock(RedisConstant.RedisConstantKey.AGENT_BROKEN + userId, userId.toString());
        //锁5分钟,5分钟后即使不解锁也会触发坐席下线
        lockHandler.tryLock(lock, 5000L, 20L, 30000L);
    }

    @Override
    public void agentReconnected(Long userId) {
        Lock lock = new Lock(RedisConstant.RedisConstantKey.AGENT_BROKEN + userId, userId.toString());
        lockHandler.releaseLock(lock);
    }

    /**
     * 执行坐席任务调度,如果条件不满足,直接抛异常
     *
     * @param userId
     * @return
     */
    private Result.ReturnData doDispatch(Long userId) {
        log.info("坐席[{}]请求任务调度", userId);
        if (wsOnlineUserApi.queryOnlineUserByUserId(WsSenceEnum.montiorcall.name(), userId.toString()).getBody() == null) {
            //坐席未开启协呼(由任务领取功能触发调度,满足坐席开启协呼的条件)
            throw new GuiyuException(DispatchCodeExceptionEnum.AGENT_NOT_ONLINE);
        }
        Lock connectLock = new Lock(RedisConstant.RedisConstantKey.AGENT_BROKEN + userId, userId.toString());
        if (lockHandler.isLockExist(connectLock)) {
            //如果坐席断线(由坐席重连触发调度)
            throw new GuiyuException(DispatchCodeExceptionEnum.AGENT_BROKEN);
        }
        if (assistAgentService.existTaskLock(userId)) {
            //存在任务锁,表示坐席处于挂起状态,不进行协呼任务调度(由坐席取消挂起触发调度)
            throw new GuiyuException(DispatchCodeExceptionEnum.AGENT_HANGUPED);
        }
        String currentCallKey = RedisConstant.RedisConstantKey.USER_CURRENT_CALLING + userId;
        if (redisUtil.get(currentCallKey) != null) {
            //当前存在未回调的呼叫任务,放弃调度(由用户提交保存或者通话结束5秒钟触发调度)
            throw new GuiyuException(DispatchCodeExceptionEnum.AGENT_HAS_CALLING);
        }
        //获取坐席任务
        DispatchPlan dispatchPlan = getDispatchPlan(userId);
        UserResourceCache userResourceCache = robotRemote.queryUserResourceCache(userId.toString()).getBody();
        if (null == userResourceCache || userResourceCache.getTempAiNumMap().get(dispatchPlan.getRobot()) == null) {
            //坐席资源不足,放弃调度
            throw new GuiyuException(DispatchCodeExceptionEnum.AGENT_NO_RESOURCES);
        }
        //获取坐席线路
        SipLineVO sipLineVO = getLine(userId).getBody();
        Result.ReturnData startMakeCall = callPlanCenter.startMakeCall(initCallRequest(dispatchPlan, sipLineVO.getLineId()));
        if (startMakeCall.success) {
            //通知呼叫中心成功,记录当前呼叫号码
            redisUtil.set(currentCallKey, dispatchPlan.getPlanUuid());
            return Result.ok();
        } else {
            log.error("通知呼叫中心失败,返回code:[{}],返回message:[{}]", startMakeCall.getCode(), startMakeCall.getMsg());
            throw new GuiyuException(DispatchCodeExceptionEnum.CALLCENTER_ERROR);
        }
    }

    /**
     * 获取坐席需要调度的通话信息
     *
     * @return
     */
    private DispatchPlan getDispatchPlan(Long userId) {
        Integer orgId = getApiService.getOrgIdByUser(userId.toString());
        DispatchPlanExample example = new DispatchPlanExample();
        example.createCriteria().andOrgIdEqualTo(orgId).andSeatIdEqualTo(userId.intValue()).andFlagEqualTo(Constant.IS_FLAG_2)
                .andStatusPlanEqualTo(Constant.STATUSPLAN_1);
        example.setOrderByClause("call_data asc,id asc");
        example.setLimitStart(0);
        example.setLimitEnd(1);
        List<DispatchPlan> dispatchPlans = dispatchPlanMapper.selectByExample(example);
        if (dispatchPlans == null || dispatchPlans.isEmpty()) {
            //坐席任务为空,取消调度(领取任务再进行触发调度)
            throw new GuiyuException(DispatchCodeExceptionEnum.AGENT_TASK_EMPTY);
        }
        return dispatchPlans.get(0);
    }

    /**
     * 获取坐席线路
     *
     * @return
     */
    private Result.ReturnData<SipLineVO> getLine(Long userId) {
        List<SipLineVO> sipLineVOS = lineMarketRemote.queryUserSipLineList(userId.toString()).getBody();
        if (sipLineVOS == null || sipLineVOS.isEmpty()) {
            //坐席线路获取失败
            throw new GuiyuException(DispatchCodeExceptionEnum.AGENT_LINE_ERROR);
        }
        SipLineVO sipLineVO = sipLineVOS.stream().filter(sipLine -> sipLine.getLineType() == 1).findAny().orElseThrow(() -> new GuiyuException(DispatchCodeExceptionEnum.AGENT_LINE_ERROR));
        return Result.ok(sipLineVO);
    }

    /**
     * 初始化呼叫请求
     *
     * @param dispatchPlan 呼叫计划
     * @param line         线路ID
     * @return
     */
    private com.guiji.calloutserver.entity.DispatchPlan initCallRequest(DispatchPlan dispatchPlan, Integer line) {
        com.guiji.calloutserver.entity.DispatchPlan callBean = new com.guiji.calloutserver.entity.DispatchPlan();
        BeanUtil.copyProperties(dispatchPlan, callBean);
        callBean.setTempId(dispatchPlan.getRobot());            //模板
        callBean.setAgentGroupId(dispatchPlan.getCallAgent());    //坐席组
        callBean.setRemarks(dispatchPlan.getAttach());            //参数
        callBean.setAnswerUser(dispatchPlan.getCustName());    //客户名称
        callBean.setEnterprise(dispatchPlan.getCustCompany());    //客户所属单位
        callBean.setImportTime(dispatchPlan.getGmtCreate());    //导入时间
        callBean.setSimCall(false);
        callBean.setLineList(Lists.newArrayList(line));
        return callBean;
    }
}
