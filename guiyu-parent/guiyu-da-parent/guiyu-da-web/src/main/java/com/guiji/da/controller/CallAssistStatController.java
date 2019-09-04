package com.guiji.da.controller;

import com.guiji.component.result.Result;
import com.guiji.da.service.callcenter.CallAssistStatService;
import com.guiji.da.service.vo.AgentStatResp;
import com.guiji.da.service.vo.CallAssistStatReq;
import com.guiji.da.service.vo.CallAssistStatResp;
import com.guiji.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author Zhouzl
 * @date 2019年07月15日
 * @since 1.0
 */
@RestController
@Slf4j
@RequestMapping("callAssistStat")
public class CallAssistStatController {
    @Autowired
    private CallAssistStatService callAssistStatService;

    @PostMapping("system")
    public Result.ReturnData<List<CallAssistStatResp>> sysStat(@RequestHeader("authLevel") Integer authLevel,@RequestHeader("orgId") Integer orgId, @RequestBody CallAssistStatReq callAssistStatReq) {
        log.info("sysStat>>>>>>>>{}>>>{}>>>{}", authLevel, orgId, JsonUtils.bean2Json(callAssistStatReq));
        return callAssistStatService.sysStat(authLevel, orgId, callAssistStatReq.getAgentId(), callAssistStatReq.getTempId(), callAssistStatReq.getStartTime(), callAssistStatReq.getEndTime());
    }

    @PostMapping("agent")
    public Result.ReturnData<List<AgentStatResp>> agentStat(@RequestHeader("authLevel") Integer authLevel, @RequestHeader("orgId") Integer orgId, @RequestBody CallAssistStatReq callAssistStatReq) {
        log.info("agentStat>>>>>>>>{}>>>{}>>>{}", authLevel, orgId, JsonUtils.bean2Json(callAssistStatReq));
        return callAssistStatService.agentStat(authLevel, orgId, callAssistStatReq.getAgentId(), callAssistStatReq.getTempId(), callAssistStatReq.getStartTime(), callAssistStatReq.getEndTime());
    }

    @PostMapping("system/export")
    public void sysStatExport(@RequestHeader("authLevel") Integer authLevel,@RequestHeader("orgId") Integer orgId, HttpServletResponse response, @RequestBody CallAssistStatReq callAssistStatReq) {
        callAssistStatService.sysStatExport(authLevel, orgId, callAssistStatReq.getAgentId(), response, callAssistStatReq.getTempId(), callAssistStatReq.getStartTime(), callAssistStatReq.getEndTime());
    }

    @PostMapping("agent/export")
    public void agentStatExport(@RequestHeader("authLevel") Integer authLevel,@RequestHeader("orgId") Integer orgId, HttpServletResponse response, @RequestBody CallAssistStatReq callAssistStatReq) {
        callAssistStatService.agentStatExport(authLevel, orgId, callAssistStatReq.getAgentId(), response, callAssistStatReq.getTempId(), callAssistStatReq.getStartTime(), callAssistStatReq.getEndTime());
    }

    @PostMapping("all/export")
    public void export(@RequestHeader("authLevel") Integer authLevel,@RequestHeader("orgId") Integer orgId, HttpServletResponse response, @RequestBody CallAssistStatReq callAssistStatReq){
        callAssistStatService.export(authLevel, orgId, callAssistStatReq.getAgentId(), response, callAssistStatReq.getTempId(), callAssistStatReq.getStartTime(), callAssistStatReq.getEndTime());
    }
}
