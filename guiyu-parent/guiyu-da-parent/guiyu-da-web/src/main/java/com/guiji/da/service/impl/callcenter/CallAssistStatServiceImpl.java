package com.guiji.da.service.impl.callcenter;

import com.google.common.collect.Lists;
import com.guiji.botsentence.api.IBotSentenceProcess;
import com.guiji.botsentence.api.entity.BotSentenceProcess;
import com.guiji.calloutserver.api.CallOutStat;
import com.guiji.calloutserver.entity.CallOutStatTemp;
import com.guiji.calloutserver.entity.CalloutStatAgent;
import com.guiji.component.result.Result;
import com.guiji.da.service.callcenter.CallAssistStatService;
import com.guiji.da.service.vo.AgentStatResp;
import com.guiji.da.service.vo.CallAssistStatResp;
import com.guiji.da.util.ExportUtil;
import com.guiji.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Zhouzl
 * @date 2019年07月15日
 * @since 1.0
 */
@Slf4j
@Service
public class CallAssistStatServiceImpl implements CallAssistStatService {
    @Autowired
    private CallOutStat callOutStat;
    @Autowired
    private IBotSentenceProcess iBotSentenceProcess;

    @Override
    public Result.ReturnData<List<CallAssistStatResp>> sysStat(Integer authLevel, Integer orgId, Integer agentId, String tempId, String startTime, String endTime) {
        Long customerId = agentId == null ? null : callOutStat.getCustomerIdByAgentId(agentId.longValue()).getBody();
        agentId = customerId == null ? null : customerId.intValue();
        List<CallOutStatTemp> statTemp = callOutStat.statTemp(authLevel, orgId, agentId, tempId, startTime, endTime).getBody();
        log.info("statTemp>>>>>>>>{}", JsonUtils.bean2Json(statTemp));
        if (null == statTemp || statTemp.isEmpty()) {
            return Result.ok();
        }
        Map<String, List<CallOutStatTemp>> tempMap = statTemp.stream().collect(Collectors.groupingBy(CallOutStatTemp::getTempId));
        List<CallOutStatTemp> statBusy = callOutStat.statBusy(authLevel, orgId, agentId, tempId, startTime, endTime).getBody();
        log.info("statBusy>>>>>>>>{}", JsonUtils.bean2Json(statBusy));
        Map<String, List<CallOutStatTemp>> busyMap;
        if (null == statBusy || statBusy.isEmpty()) {
            busyMap = new HashMap<>(1);
        } else {
            busyMap = statBusy.stream().collect(Collectors.groupingBy(CallOutStatTemp::getTempId));
        }
        final CallAssistStatResp total = new CallAssistStatResp();
        total.setTempName("总计");
        List<CallAssistStatResp> callAssistStatResps = tempMap.entrySet().stream().map(temp -> {
            String tempKey = temp.getKey();
            BotSentenceProcess botSentenceProcess = iBotSentenceProcess.getTemplateById(tempKey).getData().get(0);
            String tempName = tempKey;
            if (botSentenceProcess != null) {
                tempName = botSentenceProcess.getTemplateName();
            }
            CallAssistStatResp data = callAssistStatResp(tempName, temp.getValue(), busyMap.get(tempKey));
            total.addAll(data);
            return data;
        }).sorted(Comparator.comparingInt(CallAssistStatResp::getTotalCalled).thenComparingInt(CallAssistStatResp::totalTime)).collect(Collectors.toList());
        callAssistStatResps.add(total);
        return Result.ok(callAssistStatResps);
    }

    /**
     * 将统计结果转换为响应内容
     *
     * @param tempName 模板名
     * @param tempStat 模板统计结果
     * @param busyStat 占线统计结果
     * @return
     */
    private CallAssistStatResp callAssistStatResp(String tempName, List<CallOutStatTemp> tempStat, List<CallOutStatTemp> busyStat) {
        CallAssistStatResp callAssistStatResp = new CallAssistStatResp();
        callAssistStatResp.setTempName(tempName);
        for (CallOutStatTemp temp : tempStat) {
            callAssistStatResp.addTotalTime(temp.getDurations());
            int calls = temp.getTotals();
            String intent = temp.getIntent();
            if ("a".equalsIgnoreCase(intent)) {
                callAssistStatResp.setA(calls);
            } else if ("b".equalsIgnoreCase(intent)) {
                callAssistStatResp.setB(calls);
            } else if ("c".equalsIgnoreCase(intent)) {
                callAssistStatResp.setC(calls);
            } else if ("d".equalsIgnoreCase(intent)) {
                callAssistStatResp.setD(calls);
            } else if ("e".equalsIgnoreCase(intent)) {
                callAssistStatResp.setE(calls);
            }
            callAssistStatResp.addTotalCalled(calls);
        }
        if (null != busyStat && !busyStat.isEmpty()) {
            callAssistStatResp.setBusy(busyStat.get(0).getTotals());
        }
        return callAssistStatResp;
    }

    @Override
    public void sysStatExport(Integer authLevel, Integer orgId, Integer agentId, HttpServletResponse response, String tempId, String startTime, String endTime) {
        String fileName = "项目整体执行情况表.xls";
        try {
            Workbook workbook = ExportUtil.createWorkBook();
            CellStyle cellStyle = ExportUtil.getDefaultStyle(workbook);
            sysStatSheet(workbook, cellStyle, authLevel, orgId, agentId, tempId, startTime, endTime);
            OutputStream outputStream = ExportUtil.openOutputStream(response, ExportUtil.ContentType.EXCEL, fileName);
            ExportUtil.exportExcel(workbook, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Result.ReturnData<List<AgentStatResp>> agentStat(Integer authLevel, Integer orgId, Integer agentId, String tempId, String startTime, String endTime) {
        Long cId = agentId == null ? null : callOutStat.getCustomerIdByAgentId(agentId.longValue()).getBody();
        agentId = cId == null ? null : cId.intValue();
        //统计拨打量\意向客户量\通话时长
        List<CalloutStatAgent> numberStatList = callOutStat.callNumber(authLevel, orgId, agentId, tempId, startTime, endTime).getBody();
        log.info("numberStatList>>>>>>>>{}", JsonUtils.bean2Json(numberStatList));
        //统计接通量
        List<CalloutStatAgent> connectedList = callOutStat.statConnect(authLevel, orgId, agentId, tempId, startTime, endTime).getBody();
        log.info("connectedList>>>>>>>>{}", JsonUtils.bean2Json(connectedList));
        Map<String, AgentStatResp> tmpMap = new HashMap<>();
        for (CalloutStatAgent numberStat : numberStatList) {
            Long customerId = numberStat.getCustomerId();
            String agentName = callOutStat.getAgentNameByCustomerId(customerId).getBody();
            if (agentName == null) {
                continue;
            }
            AgentStatResp agentStatResp = getAgentStatResp(tmpMap, agentName);
            if (null == numberStat.getIntent()) {
                agentStatResp.addTotalCalled(numberStat.getNumber());
            } else {
                switch (numberStat.getIntent().toLowerCase()) {
                    case "a":
                    case "b":
                    case "c":
                    case "d":
                        agentStatResp.addIntentCustomCount(numberStat.getNumber());
                        break;
                    default:
                        agentStatResp.addTotalCalled(numberStat.getNumber());
                }
            }
            agentStatResp.addCalledTime(numberStat.getCallTime());
        }
        for (CalloutStatAgent connected : connectedList) {
            Long customerId = connected.getCustomerId();
            String agentName = callOutStat.getAgentNameByCustomerId(customerId).getBody();
            if (agentName == null) {
                continue;
            }
            AgentStatResp agentStatResp = getAgentStatResp(tmpMap, agentName);
            agentStatResp.setTotalConnected(connected.getNumber());
        }
        return Result.ok(new ArrayList<>(tmpMap.values()));
    }

    private AgentStatResp getAgentStatResp(Map<String, AgentStatResp> tmpMap, String agentName) {
        AgentStatResp agentStatResp = tmpMap.get(agentName);
        if (agentStatResp == null) {
            agentStatResp = new AgentStatResp();
            agentStatResp.setAgentName(agentName);
            tmpMap.put(agentName, agentStatResp);
        }
        return agentStatResp;
    }

    @Override
    public void agentStatExport(Integer authLevel, Integer orgId, Integer agentId, HttpServletResponse response, String tempId, String startTime, String endTime) {
        String fileName = "个人完成指标情况表.xls";
        try {
            Workbook workbook = ExportUtil.createWorkBook();
            CellStyle cellStyle = ExportUtil.getDefaultStyle(workbook);
            agentStatSheet(workbook, cellStyle, authLevel, orgId, agentId, tempId, startTime, endTime);
            OutputStream outputStream = ExportUtil.openOutputStream(response, ExportUtil.ContentType.EXCEL, fileName);
            ExportUtil.exportExcel(workbook, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void export(Integer authLevel, Integer orgId, Integer agentId, HttpServletResponse response, String tempId, String startTime, String endTime) {
        String fileName = "数据指标分析.xls";
        try {
            Workbook workbook = ExportUtil.createWorkBook();
            CellStyle cellStyle = ExportUtil.getDefaultStyle(workbook);
            sysStatSheet(workbook, cellStyle, authLevel, orgId, agentId, tempId, startTime, endTime);
            agentStatSheet(workbook, cellStyle, authLevel, orgId, agentId, tempId, startTime, endTime);
            OutputStream outputStream = ExportUtil.openOutputStream(response, ExportUtil.ContentType.EXCEL, fileName);
            ExportUtil.exportExcel(workbook, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sysStatSheet(Workbook workbook, CellStyle cellStyle, Integer authLevel, Integer orgId, Integer agentId, String tempId, String startTime, String endTime) {
        Sheet sheet = workbook.createSheet("项目整体执行情况表");
        String empty = "";
        List<Object> row0 = Lists.newArrayList("项目", "接通", empty, empty, empty, empty, "未接", "占线", "总外呼数", "总时长", "平均通话时长(S)", "接通率");
        List<Object> row1 = Lists.newArrayList(empty, "A", "B", "C", "D", "E", empty, empty, empty, empty, empty, empty);
        List<List<Object>> headList = new ArrayList<>();
        headList.add(row0);
        headList.add(row1);
        ExportUtil.addDataRows(sheet, 0, headList, cellStyle);
        ExportUtil.mergedRegion(sheet, (short) 0, (short) 1, (short) 0, (short) 0);
        ExportUtil.mergedRegion(sheet, (short) 0, (short) 0, (short) 1, (short) 5);
        ExportUtil.mergedRegion(sheet, (short) 0, (short) 1, (short) 6, (short) 6);
        ExportUtil.mergedRegion(sheet, (short) 0, (short) 1, (short) 7, (short) 7);
        ExportUtil.mergedRegion(sheet, (short) 0, (short) 1, (short) 8, (short) 8);
        ExportUtil.mergedRegion(sheet, (short) 0, (short) 1, (short) 9, (short) 9);
        ExportUtil.mergedRegion(sheet, (short) 0, (short) 1, (short) 10, (short) 10);
        ExportUtil.mergedRegion(sheet, (short) 0, (short) 1, (short) 11, (short) 11);
        List<CallAssistStatResp> callAssistStatResps = sysStat(authLevel, orgId, agentId, tempId, startTime, endTime).getBody();
        if (null == callAssistStatResps || callAssistStatResps.isEmpty()) {
            ExportUtil.autoColumnWidth(sheet, 0, 11);
            return;
        }
        List<List<Object>> dataList = callAssistStatResps.stream().map(CallAssistStatResp::toList).collect(Collectors.toList());
        ExportUtil.addDataRows(sheet, 2, dataList, cellStyle);
        ExportUtil.autoColumnWidth(sheet, 0, 11);
    }

    private void agentStatSheet(Workbook workbook, CellStyle cellStyle, Integer authLevel, Integer orgId, Integer agentId, String tempId, String startTime, String endTime) {
        List<String> headList = Lists.newArrayList("序号", "坐席", "智能外呼数", "接通数(接通率)", "通话总时长", "平均通话时长(s)", "意向客户A-D");
        Sheet sheet = workbook.createSheet("个人完成指标情况表");
        ExportUtil.fullHeadRow(sheet.createRow(0), headList, cellStyle);
        List<AgentStatResp> callAssistStatResps = agentStat(authLevel, orgId, agentId, tempId, startTime, endTime).getBody();
        if (null == callAssistStatResps || callAssistStatResps.isEmpty()) {
            ExportUtil.autoColumnWidth(sheet, 0, 6);
            return;
        }
        List<List<Object>> dataList = callAssistStatResps.stream().map(AgentStatResp::toList).collect(Collectors.toList());
        ExportUtil.addDataRows(sheet, 1, dataList, cellStyle, true);
        ExportUtil.autoColumnWidth(sheet, 0, 6);
    }
}
