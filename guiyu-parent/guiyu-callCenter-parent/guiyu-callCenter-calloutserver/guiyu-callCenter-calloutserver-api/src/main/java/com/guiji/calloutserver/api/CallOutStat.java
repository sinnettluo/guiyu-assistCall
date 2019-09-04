package com.guiji.calloutserver.api;

import com.guiji.calloutserver.entity.CallOutStatTemp;
import com.guiji.calloutserver.entity.CalloutStatAgent;
import com.guiji.component.result.Result;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author Zhouzl
 * @date 2019年07月15日
 * @since 1.0
 */
@FeignClient("guiyu-callcenter-calloutserver")
public interface CallOutStat {
    /**
     * 统计呼出话术模板情况
     *
     * @param tempId    模板ID(可为null)
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    @GetMapping("calloutplan/statTemp")
    Result.ReturnData<List<CallOutStatTemp>> statTemp(@RequestParam("authLevel") Integer authLevel, @RequestParam("orgId") Integer orgId,
                                                      @RequestParam("agentId") Integer agentId, @RequestParam("tempId") String tempId,
                                                      @RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime);

    /**
     * 统计呼出占线情况
     *
     * @param tempId    模板ID(可为null)
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    @GetMapping("calloutplan/statBusy")
    Result.ReturnData<List<CallOutStatTemp>> statBusy(@RequestParam("authLevel") Integer authLevel, @RequestParam("orgId") Integer orgId,
                                                      @RequestParam("agentId") Integer agentId, @RequestParam("tempId") String tempId,
                                                      @RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime);

    @GetMapping("calloutplan/agent")
    Result.ReturnData<String> getAgentNameById(@RequestParam("agentId") Long agentId);

    @GetMapping("calloutplan/agentNameByTierId")
    Result.ReturnData<String> getAgentNameByQueueId(@RequestParam("queueId") Long queueId);

    @GetMapping("calloutplan/agentNameByCustomerId")
    Result.ReturnData<String> getAgentNameByCustomerId(@RequestParam("customerId") Long customerId);

    @GetMapping("calloutplan/customerIdByAgentId")
    Result.ReturnData<Long> getCustomerIdByAgentId(@RequestParam("agentId") Long agentId);

    @GetMapping("calloutplan/callNumber")
    Result.ReturnData<List<CalloutStatAgent>> callNumber(@RequestParam("authLevel") Integer authLevel, @RequestParam("orgId") Integer orgId,
                                                         @RequestParam("agentId") Integer agentId, @RequestParam("tempId") String tempId,
                                                         @RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime);

    @GetMapping("calloutplan/callTime")
    Result.ReturnData<List<CalloutStatAgent>> callTime(@RequestParam("authLevel") Integer authLevel, @RequestParam("orgId") Integer orgId, @RequestParam("tempId") String tempId, @RequestParam("startTime") String startTime,
                                                       @RequestParam("endTime") String endTime);

    @GetMapping("calloutplan/statConnect")
    Result.ReturnData<List<CalloutStatAgent>> statConnect(@RequestParam("authLevel") Integer authLevel, @RequestParam("orgId") Integer orgId,
                                                          @RequestParam("agentId") Integer agentId, @RequestParam("tempId") String tempId,
                                                          @RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime);
}
