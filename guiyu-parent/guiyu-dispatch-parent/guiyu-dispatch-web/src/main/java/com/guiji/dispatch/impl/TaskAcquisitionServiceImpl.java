package com.guiji.dispatch.impl;

import com.guiji.botsentence.api.AdminUserAvailableTemplate;
import com.guiji.botsentence.api.entity.BotAvailableTemplate;
import com.guiji.common.exception.GuiyuException;
import com.guiji.component.lock.DistributedLockHandler;
import com.guiji.component.lock.Lock;
import com.guiji.component.result.Result;
import com.guiji.component.result.ServerResult;
import com.guiji.dispatch.dao.DispatchPlanMapper;
import com.guiji.dispatch.dao.TaskAcquisitionRulesMapper;
import com.guiji.dispatch.dao.entity.DispatchPlan;
import com.guiji.dispatch.dao.entity.DispatchPlanExample;
import com.guiji.dispatch.dao.entity.TaskAcquisitionRules;
import com.guiji.dispatch.dao.entity.TaskAcquisitionRulesExample;
import com.guiji.dispatch.exception.DispatchCodeExceptionEnum;
import com.guiji.dispatch.service.AssistDispatchService;
import com.guiji.dispatch.service.TaskAcquisitionService;
import com.guiji.dispatch.util.Constant;
import com.guiji.robot.api.CustAiAccountRemote;
import com.guiji.robot.model.UserAiCfgBaseInfoVO;
import com.guiji.ws.api.WsOnlineUserApi;
import com.guiji.ws.model.WsSenceEnum;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Service
public class TaskAcquisitionServiceImpl  implements TaskAcquisitionService {
    private Logger logger = LoggerFactory.getLogger(TaskAcquisitionServiceImpl.class);
    @Autowired
    private TaskAcquisitionRulesMapper taskAcquisitionRulesMapper;
    @Autowired
    TaskAcquisitionService  taskAcquisitionService;
    @Autowired
    CustAiAccountRemote custAiAccountRemote;
    @Autowired
    private DistributedLockHandler distributedLockHandler;
    @Autowired
    DispatchPlanMapper dispatchPlanMapper;
    @Autowired
    WsOnlineUserApi wsOnlineUserApi;
    @Autowired
    AssistDispatchService assistDispatchService;
    @Override
    public List<TaskAcquisitionRules> getObjByOrgCode(String orgCode) {
        TaskAcquisitionRulesExample example=new TaskAcquisitionRulesExample();
        example.createCriteria().andOrgCodeEqualTo(orgCode);
        List<TaskAcquisitionRules> selectByExample =taskAcquisitionRulesMapper.selectByExample(example);
        return selectByExample;
    }

    @Override
    public void saveOrUpdate(Integer userId,String orgCode,TaskAcquisitionRules taskAcquisitionRules) {
        TaskAcquisitionRulesExample example=new TaskAcquisitionRulesExample();
        example.createCriteria().andOrgCodeEqualTo(orgCode);
        List<TaskAcquisitionRules> selectByExample =taskAcquisitionRulesMapper.selectByExample(example);
        if (null==selectByExample||selectByExample.size()==0)
        {    //新增
            taskAcquisitionRules.setCreateTime(new Date());
            taskAcquisitionRules.setCreateId(userId);
            taskAcquisitionRules.setOrgCode(orgCode);
            taskAcquisitionRulesMapper.insert(taskAcquisitionRules);
        }
        else
        {
            //修改
            taskAcquisitionRules.setId(selectByExample.get(0).getId());
            taskAcquisitionRules.setUpdateId(userId);
            taskAcquisitionRules.setUpdateTime(new Date());
            taskAcquisitionRules.setOrgCode(orgCode);
            taskAcquisitionRulesMapper.updateByPrimaryKey(taskAcquisitionRules);
        }
    }

    @Override
    public Result.ReturnData<Object> oneClickPickup(Integer userId, String orgCode,String robotname) {
        if (wsOnlineUserApi.queryOnlineUserByUserId(WsSenceEnum.montiorcall.name(), userId.toString()) == null) {
            //坐席未开启协呼
            return  new Result.ReturnData<>("001","未开始协呼任务，不能领取!",false);
        }
        //获取组织下领取规则
        List<TaskAcquisitionRules>  list=taskAcquisitionService.getObjByOrgCode(orgCode);
        if(null==list||list.size()==0)
        {
            return  new Result.ReturnData<>("001","领取规则配置为空",false);
        }
        int singleCount=list.get(0).getSingleCount();
        int maxCount=list.get(0).getMaxCount();
        Lock lock = new Lock("dispatch.pickup.lock" + orgCode, "dispatch.pickup.lock" + orgCode);
        //任务分配策略
        if (distributedLockHandler.tryLock(lock)) {
            try {
             //获取计划日期在当天及以前并且未分配坐席未计划的任务
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String startTime = sdf.format(new Date());
                DispatchPlanExample ex = new DispatchPlanExample();
                ex.createCriteria().andSeatIdEqualTo(null).andCallDataLessThanOrEqualTo(Integer.parseInt(startTime)).
                        andStatusPlanEqualTo(0).andIsDelEqualTo(Constant.IS_DEL_0).andFlagEqualTo(Constant.IS_FLAG_2);
                //任务池总量
                int  totalCount=dispatchPlanMapper.countByExample(ex);
                if(totalCount==0)
                {
                    return  new Result.ReturnData<>("001","当前无任务可分配",false);
                }
                //查询计划中数量
                ex.clear();
                ex.createCriteria().andSeatIdEqualTo(userId).andStatusPlanEqualTo(1);
                int count=dispatchPlanMapper.countByExample(ex);
                if (count>=maxCount)
                {
                    return  new Result.ReturnData<>("001","当前待拨打任务已超出设置值，请勿重复领取！",false);
                }
                //如果单次领取大于剩余可领取数量 则按照剩余可领取数量过滤分配
                int availableCount=0;
                if(singleCount>=totalCount)
                {
                   if(totalCount+count>=maxCount)
                   {
                       //本次可领取数量
                       availableCount=maxCount-count;

                   }else
                   {
                       availableCount=totalCount;

                   }
                }
                else
                {
                    //如果单次领取小于剩余可领取数量 则按照单次领取数量过滤分配
                    if(singleCount+count>=maxCount)
                    {
                        //本次可领取数量
                        availableCount=maxCount-count;
                    }else
                    {
                        availableCount=singleCount;
                    }
                }
                if(availableCount==0)
                {
                    return new Result.ReturnData<>("001","当前无可分配任务",false);
                }
                //筛选满足条件的记录
                ex.clear();
                ex.createCriteria().andRobotEqualTo(robotname).andSeatIdEqualTo(null).andCallDataLessThanOrEqualTo(Integer.parseInt(startTime)).
                        andStatusPlanEqualTo(0).andIsDelEqualTo(Constant.IS_DEL_0).andFlagEqualTo(Constant.IS_FLAG_2);
                ex.setLimitStart(0);
                ex.setLimitEnd(availableCount);
                List<DispatchPlan>  list1=dispatchPlanMapper.selectByExample(ex);
                //更新数据
                if(null!=list1&&list1.size()>0)
                {
                    for(DispatchPlan dispatchPlan:list1)
                    {
                        ex.clear();
                        ex.createCriteria().andSeatIdEqualTo(userId).andStatusPlanEqualTo(1);
                        dispatchPlanMapper.updateByExample(dispatchPlan,ex);
                    }
                }else
                {
                    return new Result.ReturnData<>("001","当前无可分配任务",false);
                }
            }catch (Exception e){
                logger.info("oneClickPickup代码异常了", e);
            }
            finally {
                distributedLockHandler.releaseLock(lock); // 释放锁
            }
        }
        //触发调度
        try
        {
            assistDispatchService.dispatch(Long.valueOf(userId.toString()));
        }catch (Exception e){
            logger.info("领取任务后触发调度失败{}" ,e);
        }

        return new Result.ReturnData<>("000","任务领取成功",true);
    }

    @Override
    public Result.ReturnData<List<String>> getAvailbleTemplateIds(Integer userId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String startTime = sdf.format(new Date());
        DispatchPlanExample ex = new DispatchPlanExample();
        //获取用户下配置的话术
        Result.ReturnData<UserAiCfgBaseInfoVO> serverResult=custAiAccountRemote.queryUserAiCfgBaseInfoByUserId(userId.longValue());
        if(null==serverResult)
        {
            return  new Result.ReturnData<>("001","获取用户下配置的话术为空",false);
        }
        UserAiCfgBaseInfoVO userAiCfgBaseInfoVO= (UserAiCfgBaseInfoVO)serverResult.getBody();
        if(null==userAiCfgBaseInfoVO|| StringUtils.isBlank(userAiCfgBaseInfoVO.getTemplateIds()))
        {
            return  new Result.ReturnData<>("001","获取用户下配置的话术为空",false);
        }
        ex.clear();
        ex.createCriteria().andSeatIdEqualTo(null).andCallDataLessThanOrEqualTo(Integer.parseInt(startTime)).andStatusPlanEqualTo(0)
        .andIsDelEqualTo(Constant.IS_DEL_0).andFlagEqualTo(Constant.IS_FLAG_2);
        //任务池总量
        List<DispatchPlan> list=dispatchPlanMapper.selectByExample(ex);
        if(null==list||list.size()==0)
        {
            return  new Result.ReturnData<>("001","当前无任务可分配",false);
        }
        List list1=new ArrayList<String>();
        for(DispatchPlan dispatchPlan:list)
        {
            if(userAiCfgBaseInfoVO.getTemplateIds().contains(dispatchPlan.getRobotName()))
            {
                list1.add(dispatchPlan.getRobotName());
            }

        }
        Result.ReturnData<List<String>> result=new Result.ReturnData<>();
        result.setBody(list1);
        return  result;
    }
}
