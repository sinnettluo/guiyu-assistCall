package com.guiji.da.service.vo;

import lombok.Data;
import org.assertj.core.util.Lists;

import java.util.List;

/**
 * @author Zhouzl
 * @date 2019年07月16日
 * @since 1.0
 */
@Data
public class AgentStatResp {
    /**
     * 坐席名
     */
    private String agentName;
    /**
     * 总拨打量
     */
    private int totalCalled;
    /**
     * 总接通量
     */
    private int totalConnected;
    /**
     * 呼叫时长
     */
    private int calledTime;
    /**
     * 平均通话时长
     */
    private int avgCalledTime;
    /**
     * 本月意向客户数量
     */
    private int intentCustomCount;

    public void addCalledTime(int calledTime) {
        this.calledTime += calledTime;
    }

    public int getAvgCalledTime() {
        return calledTime / totalCalled;
    }

    public String getCalledTime() {
        int hour = calledTime / 3600;
        int min = (calledTime - hour * 3600) / 60;
        return hour + "小时" + min + "分";
    }

    public String getTotalConnected() {
        return totalConnected + "(" + (totalConnected * 100 / totalCalled) + "%)";
    }

    public String getIntentCustomCount() {
        return intentCustomCount + "(" + String.format("%.1f", intentCustomCount * 100D / totalCalled) + "%)";
    }

    public void addTotalCalled(int called) {
        totalCalled += called;
    }

    public void addIntentCustomCount(int intentCustomCount) {
        this.intentCustomCount += intentCustomCount;
        addTotalCalled(intentCustomCount);
    }

    public List<Object> toList() {
        return Lists.newArrayList(getAgentName(), getTotalCalled(), getTotalConnected(), getCalledTime(), getAvgCalledTime(), getIntentCustomCount());
    }
}
