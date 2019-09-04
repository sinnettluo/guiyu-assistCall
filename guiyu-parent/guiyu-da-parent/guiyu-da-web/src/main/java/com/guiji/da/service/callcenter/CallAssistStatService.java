package com.guiji.da.service.callcenter;

import com.guiji.component.result.Result;
import com.guiji.da.service.vo.AgentStatResp;
import com.guiji.da.service.vo.CallAssistStatResp;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author Zhouzl
 * @date 2019年07月15日
 * @since 1.0
 */
public interface CallAssistStatService {
    /**
     * 项目整体执行情况
     *
     * @param authLevel 权限级别
     * @param orgId     组织ID
     * @param agentId   坐席ID
     * @param tempId    模板ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    Result.ReturnData<List<CallAssistStatResp>> sysStat(Integer authLevel, Integer orgId, Integer agentId, String tempId, String startTime, String endTime);

    /**
     * 个人(坐席)完成指标情况
     */
    Result.ReturnData<List<AgentStatResp>> agentStat(Integer authLevel, Integer orgId, Integer agentId, String tempId, String startTime, String endTime);

    /**
     * 项目整体执行情况结果导出
     */
    void sysStatExport(Integer authLevel, Integer orgId, Integer agentId, HttpServletResponse response, String tempId, String startTime, String endTime);

    /**
     * 个人(坐席)完成指标情况结果导出
     */
    void agentStatExport(Integer authLevel, Integer orgId, Integer agentId, HttpServletResponse response, String tempId, String startTime, String endTime);

    /**
     * 统计结果导出至一个文件里
     */
    void export(Integer authLevel, Integer orgId, Integer agentId, HttpServletResponse response, String tempId, String startTime, String endTime);
}
