package com.guiji.calloutserver.service.impl;

import com.guiji.callcenter.dao.AgentMapper;
import com.guiji.callcenter.dao.CallOutPlanMapper;
import com.guiji.callcenter.dao.TierMapper;
import com.guiji.callcenter.dao.entity.Agent;
import com.guiji.callcenter.dao.entity.AgentExample;
import com.guiji.callcenter.dao.entity.Tier;
import com.guiji.callcenter.dao.entity.TierExample;
import com.guiji.calloutserver.entity.CallOutStatTemp;
import com.guiji.calloutserver.entity.CalloutStatAgent;
import com.guiji.calloutserver.service.AuthService;
import com.guiji.calloutserver.service.CallOutStatService;
import com.guiji.component.result.Result;
import com.guiji.utils.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Zhouzl
 * @date 2019年07月16日
 * @since 1.0
 */
@Service
public class CallOutStatServiceImpl implements CallOutStatService {
    @Autowired
    private CallOutPlanMapper callOutPlanMapper;
    @Autowired
    private AgentMapper agentMapper;
    @Autowired
    private TierMapper tierMapper;
    @Autowired
    private AuthService authService;

    @Override
    public Result.ReturnData<List<CallOutStatTemp>> statTemp(Integer authLevel, Integer orgId, Integer agentId, String tempId, String startTime, String endTime) {
        List<Integer> orgIds = authService.getOrgIdsByAuthlevel(authLevel, orgId);
        return Result.ok(callOutPlanMapper.statTemp(agentId, tempId, startTime, endTime, orgIds).stream().map(statTemp -> {
            CallOutStatTemp res = new CallOutStatTemp();
            BeanUtil.copyProperties(statTemp, res);
            return res;
        }).collect(Collectors.toList()));
    }

    @Override
    public Result.ReturnData<List<CallOutStatTemp>> statBusy(Integer authLevel, Integer orgId, Integer agentId, String tempId, String startTime, String endTime) {
        List<Integer> orgIds = authService.getOrgIdsByAuthlevel(authLevel, orgId);
        return Result.ok(callOutPlanMapper.statBusy(agentId, tempId, startTime, endTime, orgIds).stream().map(statTemp -> {
            CallOutStatTemp res = new CallOutStatTemp();
            BeanUtil.copyProperties(statTemp, res);
            return res;
        }).collect(Collectors.toList()));
    }

    @Override
    public Result.ReturnData<String> getAgentNameById(Long agentId) {
        Agent agent = agentMapper.selectByPrimaryKey(agentId);
        if (agent == null) {
            return Result.ok();
        }
        return Result.ok(agent.getUserName());
    }

    @Override
    public Result.ReturnData<String> getAgentNameByCustomerId(Long customerId) {
        AgentExample example = new AgentExample();
        example.createCriteria().andCustomerIdEqualTo(customerId);
        List<Agent> agents = agentMapper.selectByExample(example);
        if(agents == null || agents.isEmpty()){
            return null;
        }
        return Result.ok(agents.get(0).getUserName());
    }

    @Override
    public Result.ReturnData<Long> getCustomerIdByAgentId(Long agentId) {
        AgentExample example = new AgentExample();
        example.createCriteria().andUserIdEqualTo(agentId);
        List<Agent> agents = agentMapper.selectByExample(example);
        if(agents == null || agents.isEmpty()){
            return null;
        }
        return Result.ok(agents.get(0).getCustomerId());
    }

    @Override
    public Result.ReturnData<String> getAgentNameByQueueId(Long queueId) {
        TierExample example = new TierExample();
        example.createCriteria().andQueueIdEqualTo(queueId);
        List<Tier> tier = tierMapper.selectByExample(example);
        if (tier == null || tier.isEmpty()) {
            return Result.ok();
        }
        return getAgentNameById(tier.get(0).getUserId());
    }

    @Override
    public Result.ReturnData<List<CalloutStatAgent>> callNumber(Integer authLevel, Integer orgId, Integer agentId, String tempId, String startTime, String endTime) {
        List<Integer> orgIds = authService.getOrgIdsByAuthlevel(authLevel, orgId);
        return Result.ok(callOutPlanMapper.statNumber(agentId, tempId, startTime, endTime, orgIds).stream().map(statAgent -> {
            CalloutStatAgent calloutStatAgent = new CalloutStatAgent();
            BeanUtil.copyProperties(statAgent, calloutStatAgent);
            return calloutStatAgent;
        }).collect(Collectors.toList()));
    }

    @Override
    public Result.ReturnData<List<CalloutStatAgent>> callTime(Integer authLevel, Integer orgId, String tempId, String startTime, String endTime) {
        List<Integer> orgIds = authService.getOrgIdsByAuthlevel(authLevel, orgId);
        return Result.ok(callOutPlanMapper.statTime(tempId, startTime, endTime, orgIds).stream().map(statAgent -> {
            CalloutStatAgent calloutStatAgent = new CalloutStatAgent();
            BeanUtil.copyProperties(statAgent, calloutStatAgent);
            return calloutStatAgent;
        }).collect(Collectors.toList()));
    }

    @Override
    public Result.ReturnData<List<CalloutStatAgent>> statConnect(Integer authLevel, Integer orgId, Integer agentId, String tempId, String startTime, String endTime) {
        List<Integer> orgIds = authService.getOrgIdsByAuthlevel(authLevel, orgId);
        return Result.ok(callOutPlanMapper.statConnect(agentId, tempId, startTime, endTime, orgIds).stream().map(statAgent -> {
            CalloutStatAgent calloutStatAgent = new CalloutStatAgent();
            BeanUtil.copyProperties(statAgent, calloutStatAgent);
            return calloutStatAgent;
        }).collect(Collectors.toList()));
    }
}
