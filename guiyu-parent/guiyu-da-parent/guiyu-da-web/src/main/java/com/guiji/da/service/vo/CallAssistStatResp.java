package com.guiji.da.service.vo;

import com.google.common.collect.Lists;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Zhouzl
 * @date 2019年07月15日
 * @since 1.0
 */
@Data
public class CallAssistStatResp {
    /**
     * 模板名称
     */
    private String tempName;
    /**
     * 接通a
     */
    private int a;
    /**
     * 接通b
     */
    private int b;
    /**
     * 接通c
     */
    private int c;
    /**
     * 接通d
     */
    private int d;
    /**
     * 接通e
     */
    private int e;
    /**
     * 未接通
     */
    private int notConnect;
    /**
     * 占线
     */
    private int busy;
    /**
     * 总拨打量
     */
    private int totalCalled;
    /**
     * 总时长
     */
    private int totalTime;
    /**
     * 平均通话时长
     */
    private double avgTime;
    /**
     * 接通率
     */
    private double connectPercent;

    public void addTotalCalled(int called) {
        totalCalled += called;
    }

    public void addTotalTime(int time) {
        totalTime += time;
    }

    public void addAll(CallAssistStatResp data){
        addTotalTime(data.totalTime);
        addTotalCalled(data.totalCalled);
        busy += data.busy;
        a += data.a;
        b += data.b;
        c += data.c;
        d += data.d;
        e += data.e;
    }

    /**
     * 计算未连接数(总拨打量-接通量-占线)
     *
     * @return
     */
    public int getNotConnect() {
        return totalCalled - (busy + a + b + c + d + e);
    }

    /**
     * 计算平均拨打时长
     *
     * @return
     */
    public double getAvgTime() {
        return new BigDecimal(totalTime * 1D / totalCalled).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 计算接通率
     *
     * @return
     */
    public String getConnectPercent() {
        return new BigDecimal((a + b + c + d + e) * 100D / totalCalled).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue() + "%";
    }

    /**
     * 返回总时长数值,用于排序
     *
     * @return
     */
    public int totalTime() {
        return totalTime;
    }

    /**
     * 返回格式化总时长
     *
     * @return
     */
    public String getTotalTime() {
        int hour = totalTime / 3600;
        int min = (totalTime - hour * 3600) / 60;
        return hour + "小时" + min + "分";
    }

    public List<Object> toList() {
        return Lists.newArrayList(getTempName(), getA(), getB(), getC(), getD(), getE(), getNotConnect(),
                getBusy(), getTotalCalled(), getTotalTime(), getAvgTime(), getConnectPercent());
    }
}
