package com.guiji.calloutserver.controller;

import com.guiji.calloutserver.api.CallOutStat;
import com.guiji.calloutserver.entity.CallOutStatTemp;
import com.guiji.calloutserver.entity.CalloutStatAgent;
import com.guiji.calloutserver.service.CallOutStatService;
import com.guiji.component.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Zhouzl
 * @date 2019年07月15日
 * @since 1.0
 */
@RestController
public class CallOutStatController implements CallOutStat {
    @Autowired
    private CallOutStatService callOutStatService;

    @Override
    public Result.ReturnData<List<CallOutStatTemp>> statTemp(Integer authLevel, Integer orgId, Integer agentId, String tempId, String startTime, String endTime) {
        return callOutStatService.statTemp(authLevel, orgId, agentId, tempId, startTime, endTime);
    }

    @Override
    public Result.ReturnData<List<CallOutStatTemp>> statBusy(Integer authLevel, Integer orgId, Integer agentId, String tempId, String startTime, String endTime) {
        return callOutStatService.statBusy(authLevel, orgId, agentId, tempId, startTime, endTime);
    }

    @Override
    public Result.ReturnData<String> getAgentNameById(Long agentId) {
        return callOutStatService.getAgentNameById(agentId);
    }

    @Override
    public Result.ReturnData<String> getAgentNameByQueueId(Long queueId) {
        return callOutStatService.getAgentNameByQueueId(queueId);
    }

    @Override
    public Result.ReturnData<String> getAgentNameByCustomerId(Long customerId) {
        return callOutStatService.getAgentNameByCustomerId(customerId);
    }

    @Override
    public Result.ReturnData<Long> getCustomerIdByAgentId(Long agentId) {
        return callOutStatService.getCustomerIdByAgentId(agentId);
    }

    @Override
    public Result.ReturnData<List<CalloutStatAgent>> callNumber(Integer authLevel, Integer orgId, Integer agentId, String tempId, String startTime, String endTime) {
        return callOutStatService.callNumber(authLevel, orgId, agentId, tempId, startTime, endTime);
    }

    @Override
    public Result.ReturnData<List<CalloutStatAgent>> callTime(Integer authLevel, Integer orgId, String tempId, String startTime, String endTime) {
        return callOutStatService.callTime(authLevel, orgId, tempId, startTime, endTime);
    }

    @Override
    public Result.ReturnData<List<CalloutStatAgent>> statConnect(Integer authLevel, Integer orgId, Integer agentId, String tempId, String startTime, String endTime) {
        return callOutStatService.statConnect(authLevel, orgId, agentId, tempId, startTime, endTime);
    }
}
