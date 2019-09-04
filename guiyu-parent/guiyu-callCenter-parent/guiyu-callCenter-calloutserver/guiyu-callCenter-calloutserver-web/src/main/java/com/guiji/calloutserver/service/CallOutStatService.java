package com.guiji.calloutserver.service;

import com.guiji.calloutserver.entity.CallOutStatTemp;
import com.guiji.calloutserver.entity.CalloutStatAgent;
import com.guiji.component.result.Result;

import java.util.List;

/**
 * @author Zhouzl
 * @date 2019年07月16日
 * @since 1.0
 */
public interface CallOutStatService {
    Result.ReturnData<List<CallOutStatTemp>> statTemp(Integer authLevel, Integer orgId, Integer agentId, String tempId, String startTime, String endTime);

    Result.ReturnData<List<CallOutStatTemp>> statBusy(Integer authLevel, Integer orgId, Integer agentId, String tempId, String startTime, String endTime);

    Result.ReturnData<String> getAgentNameById(Long agentId);

    Result.ReturnData<String> getAgentNameByQueueId(Long queueId);

    Result.ReturnData<String> getAgentNameByCustomerId(Long customerId);

    Result.ReturnData<Long> getCustomerIdByAgentId(Long agentId);

    Result.ReturnData<List<CalloutStatAgent>> callNumber(Integer authLevel, Integer orgId, Integer agentId, String tempId, String startTime, String endTime);

    Result.ReturnData<List<CalloutStatAgent>> callTime(Integer authLevel, Integer orgId, String tempId, String startTime, String endTime);

    Result.ReturnData<List<CalloutStatAgent>> statConnect(Integer authLevel, Integer orgId, Integer agentId, String tempId, String startTime, String endTime);
}
