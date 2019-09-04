package com.guiji.dispatch.controller;

import com.guiji.component.result.Result;
import com.guiji.component.result.Result.ReturnData;
import com.guiji.dispatch.api.IDispatchPlanOut;
import com.guiji.dispatch.dao.DispatchPlanMapper;
import com.guiji.dispatch.dao.entity.DispatchBatchLine;
import com.guiji.dispatch.dao.entity.DispatchPlanExample;
import com.guiji.dispatch.entity.ExportFileRecord;
import com.guiji.dispatch.enums.SysDelEnum;
import com.guiji.dispatch.line.IDispatchBatchLineService;
import com.guiji.dispatch.model.*;
import com.guiji.dispatch.service.GetAuthUtil;
import com.guiji.dispatch.service.IDispatchPlanService;
import com.guiji.dispatch.service.IExportFileService;
import com.guiji.dispatch.service.IResourcePoolService;
import com.guiji.dispatch.util.DateTimeUtils;
import com.guiji.dispatch.vo.AssistGroupVO;
import com.guiji.utils.BeanUtil;
import com.guiji.utils.IdGengerator.IdUtils;
import com.guiji.utils.JsonUtils;
import com.guiji.utils.RedisUtil;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class DispatchOutApiController implements IDispatchPlanOut {

    static Logger logger = LoggerFactory.getLogger(DispatchOutApiController.class);

    @Autowired
    private IDispatchPlanService dispatchPlanService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private IResourcePoolService resourcePoolService;

    @Autowired
    private DispatchPlanMapper mapper;
    @Autowired
    private IDispatchBatchLineService lineService;
    @Autowired
    private DispatchPlanMapper dispatchMapper;

    @Autowired
    private IExportFileService exportFileService;
    @Autowired
    private GetAuthUtil getAuthUtil;

    /**
     * 完成
     *
     * @param planUuid 任务id
     * @return 响应报文
     * @throws Exception 异常
     */
    @Override
    @GetMapping(value = "out/successSchedule")
    public ReturnData<Boolean> successSchedule(String planUuid, String label) {
        boolean result = dispatchPlanService.successSchedule(planUuid, label);
        ReturnData<Boolean> res = new ReturnData<>();
        res.setBody(result);
        return res;
    }

    /**
     * 返回可以拨打的任务给呼叫中心
     *
     * @param userId
     * @param requestCount
     * @param lineId
     * @return
     */
    @Override
    @GetMapping(value = "out/queryAvailableSchedules")
    public ReturnData<List<DispatchPlan>> queryAvailableSchedules(Integer userId, int requestCount, int lineId) {
        long start = System.currentTimeMillis();
        com.guiji.dispatch.dao.entity.DispatchPlan dis = new com.guiji.dispatch.dao.entity.DispatchPlan();

        List<com.guiji.dispatch.dao.entity.DispatchPlan> queryAvailableSchedules = dispatchPlanService
                .queryAvailableSchedules(userId, requestCount, lineId, dis, true);
        List<DispatchPlan> list = new ArrayList<>();
        try {
            for (com.guiji.dispatch.dao.entity.DispatchPlan plan : queryAvailableSchedules) {
                DispatchPlan bean = new DispatchPlan();
                BeanUtils.copyProperties(plan, bean);
                list.add(bean);
            }
        } catch (Exception e) {
            logger.error("error", e);
        }

        // if (list.size() > 0) {
        // list.get(list.size() - 1).setSuccess(dis.isSuccess());
        // }
        long end = System.currentTimeMillis();
        logger.info("返回可以拨打的任务给呼叫中心结果数量:" + list.size());
        if (list.size() > 0) {
            logger.info("返回可以拨打的任务给呼叫中心结果号码:" + list.get(0).getPhone());
            logger.info("返回可以拨打的任务给呼叫中心结果uuid:" + list.get(0).getPlanUuid());
        }
        return Result.ok(list);
    }

    @Override
    public ReturnData<Boolean> receiveRobotId(String RobotId) {
        logger.info("receiveRobotId 接受到了。");
        ReturnData<Boolean> result = new ReturnData<>();
        boolean res = redisUtil.set(RobotId, RobotId);
        result.setBody(res);
        return result;
    }

    @Override
    public ReturnData<Boolean> initResourcePool() {
        boolean result = resourcePoolService.initResourcePool();
        return new ReturnData<Boolean>(result);
    }

    @Override
    public ReturnData<Boolean> successSchedule4TempId(String tempId) {
        logger.info("successSchedule4TempId  完成模板通知升级:" + tempId);
        ReturnData<Boolean> result = new ReturnData<>();
        redisUtil.del(tempId);
        result.setBody(true);
        return result;
    }

    @Override
    public ReturnData<PlanCountVO> getPlanCountByUserId(String orgCode, Integer orgId) {
        PlanCountVO planCountByUserId = dispatchPlanService.getPlanCountByUserId(orgCode, orgId);
        ReturnData<PlanCountVO> result = new ReturnData<>();
        result.setBody(planCountByUserId);
        return result;
    }

    @Override
    public ReturnData<Boolean> opertationStopPlanByUserId(String orgCode, String type, Integer orgId) {
        boolean stopPlanByorgCode = dispatchPlanService.stopPlanByorgCode(orgCode, orgId, type);
        ReturnData<Boolean> result = new ReturnData<>();
        result.setBody(stopPlanByorgCode);
        return result;
    }

    @Override
    public ReturnData<Boolean> updateLabelByUUID(String planuuid, String label) {
        DispatchPlanExample ex = new DispatchPlanExample();
        ex.createCriteria().andPlanUuidEqualTo(Long.valueOf(planuuid)).andOrgIdEqualTo(IdUtils.doParse(Long.valueOf(planuuid)).getOrgId());
        com.guiji.dispatch.dao.entity.DispatchPlan dis = new com.guiji.dispatch.dao.entity.DispatchPlan();
        dis.setResult(label);
        int count = mapper.updateByExampleSelective(dis, ex);
        ReturnData<Boolean> result = new ReturnData<>();
        result.setBody(count > 0 ? true : false);
        return result;
    }

    @Override
    public ReturnData<Boolean> lineIsUsedByUserId(Integer lineId, Integer userId) {
        ReturnData<Boolean> res = new ReturnData<>();
        // 查询当前任务中心
        List<DispatchBatchLine> lines = null;
        if (userId != null) {
            lines = lineService.queryListByUserIdLineId(Long.valueOf(userId), lineId);
        } else {
            lines = lineService.queryListByLineId(lineId);
        }

        if (lines == null || lines.isEmpty()) {
            res.setBody(false);
            return res;
        }

        List<Integer> orgIds = new ArrayList<>();
        List<Integer> batchIds = new ArrayList<>();
        for (DispatchBatchLine line : lines) {
            orgIds.add(line.getOrgId());
            batchIds.add(line.getBatchId());
        }

        DispatchPlanExample planEx = new DispatchPlanExample();
        DispatchPlanExample.Criteria criteria = planEx.createCriteria();
        criteria
                .andStatusPlanEqualTo(Integer.valueOf(com.guiji.dispatch.model.Constant.STATUSPLAN_PLANING))
                .andIsDelEqualTo(SysDelEnum.NORMAL.getState());
        if (userId != null) {
            criteria.andUserIdEqualTo(userId);
        }
        if (null != batchIds && batchIds.size() > 0) {
            criteria.andBatchIdIn(batchIds);
        }


        int count = dispatchMapper.countByExample(planEx);
        if (count > 0) {
            res.setBody(true);
            return res;
        }

        res.setBody(false);
        return res;
    }

    @Override
    public ReturnData<Boolean> lineIsUsed(@RequestBody LineIsUseDto lineIsUseDto) {
        boolean bool = false;
        logger.info("com.guiji.dispatch.controller.DispatchOutApiController.lineIsUsed, lineIsUseDto:{}", JsonUtils.bean2Json(lineIsUseDto));
        try {
            if (null != lineIsUseDto && null != lineIsUseDto.getLineId()
                    && null != lineIsUseDto.getUserIdList() && lineIsUseDto.getUserIdList().size() > 0) {
                Integer lineId = lineIsUseDto.getLineId();
                List<Integer> userIdList = lineIsUseDto.getUserIdList();
                //查询用户、线路关联数据
                List<DispatchBatchLine> batchLineList = lineService.queryListByUserIdLineId(userIdList, lineId);
                if (null != batchLineList && batchLineList.size() > 0) {
                    //获取批次ID列表
                    List<Integer> batchIdList = new ArrayList<Integer>();
                    for (DispatchBatchLine batchLine : batchLineList) {
                        batchIdList.add(batchLine.getBatchId());
                    }

                    DispatchPlanExample planEx = new DispatchPlanExample();
                    DispatchPlanExample.Criteria criteria = planEx.createCriteria();
                    criteria.andStatusPlanEqualTo(Integer.valueOf(com.guiji.dispatch.model.Constant.STATUSPLAN_PLANING))
                            .andIsDelEqualTo(SysDelEnum.NORMAL.getState());
                    criteria.andUserIdIn(userIdList);
                    criteria.andBatchIdIn(batchIdList);
                    //查询“计划中”状态数据
                    int count = dispatchMapper.countByExample(planEx);
                    bool = count > 0 ? true : false;
                }
            }
        } catch (Exception e) {
            logger.error("查询线路是否占用异常", e);
        }
        logger.info("查询线路是否占用:{}", bool);
        return new ReturnData<Boolean>(bool);
    }

    /**
     * 查询任务计划
     *
     * @param planUuid
     * @return
     */
    @Override
    public ReturnData<DispatchPlan> queryDispatchPlanById(String planUuid) {
        com.guiji.dispatch.dao.entity.DispatchPlan plan = !StringUtils.isEmpty(planUuid) ?
                dispatchPlanService.queryDispatchPlanById(Long.valueOf(planUuid)) : null;
        DispatchPlan dispatchPlan = null;
        if (null != plan) {
            dispatchPlan = new DispatchPlan();
            BeanUtils.copyProperties(plan, dispatchPlan, DispatchPlan.class);
        }
        return new ReturnData<DispatchPlan>(dispatchPlan);
    }

    /**
     * 查询任务计划备注
     *
     * @param planUuid
     * @return
     */
    @Override
    @ApiOperation(value = "查询任务计划备注", notes = "查询任务计划备注")
//	@RequestMapping(value = "/dispatch/queryPlanRemarkById", method = {RequestMethod.GET})
    public ReturnData<String> queryPlanRemarkById(@RequestParam("planUuid") String planUuid) {
        String planAttach = !StringUtils.isEmpty(planUuid) ?
                dispatchPlanService.queryPlanRemarkById(Long.valueOf(planUuid)) : null;
        return new ReturnData<String>(planAttach);
    }

    /**
     * 新增文件导出记录
     *
     * @param exportFileDto
     * @return
     */
    @ApiOperation(value = "新增导出记录", notes = "新增导出记录")
    @Override
    public ReturnData<ExportFileRecordVo> addExportFile(@RequestBody ExportFileDto exportFileDto) {
        logger.info("新增文件导出记录入参", JsonUtils.bean2Json(exportFileDto));
        ExportFileRecord record = exportFileService.addExportFile(exportFileDto);
        ExportFileRecordVo recordVo = null;
        if (null != record) {
            recordVo = new ExportFileRecordVo();
            BeanUtils.copyProperties(record, recordVo, ExportFileRecordVo.class);
        }
        return new ReturnData<ExportFileRecordVo>(recordVo);
    }

    /**
     * 变更文件导出记录状态和文件导出地址
     *
     * @param recordId
     * @param status
     * @return
     */
    @Override
    public ReturnData<Boolean> updExportFile(String recordId, Integer status, String fileGenerateUrl) {
        boolean bool = exportFileService.endExportFile(recordId, status, fileGenerateUrl);
        return new ReturnData<Boolean>(bool);
    }

    /**
     * 根据用户统计当天计划数量
     *
     * @param userId
     * @param orgId
     * @return
     */
    @Override
    @GetMapping(value = "totalPlanCount")
    public ReturnData<TotalPlanCountVo> totalPlanCount(String userId, Integer orgId) {
        String dateStr = new SimpleDateFormat(DateTimeUtils.DEFAULT_DATE_FORMAT_PATTERN_SHORT).format(new Date());
        com.guiji.dispatch.vo.TotalPlanCountVo totalPlanCountVo = dispatchPlanService.totalPlanCountByUserDate(userId, dateStr, dateStr, orgId);
        TotalPlanCountVo newVo = null;
        if (null != totalPlanCountVo) {
            newVo = new TotalPlanCountVo();
            BeanUtils.copyProperties(totalPlanCountVo, newVo, TotalPlanCountVo.class);
        }
        return new ReturnData<TotalPlanCountVo>(newVo);
    }

    @Override
    public ReturnData<List<AgentGroupStat>> agentGroupStat(Integer authLevel, Integer orgId) {
        Integer today = Integer.valueOf(DateTimeFormatter.BASIC_ISO_DATE.format(LocalDateTime.now()));
        List<AssistGroupVO> assistGroupVOS = dispatchMapper.assistGroupStat(today, getAuthUtil.getOrgIdsByAuthLevel(authLevel, orgId));
        return Result.ok(assistGroupVOS.stream().map(assistGroupVO -> {
            AgentGroupStat agentGroupStat = new AgentGroupStat();
            BeanUtil.copyProperties(assistGroupVO, agentGroupStat);
            return agentGroupStat;
        }).collect(Collectors.toList()));
    }
}
