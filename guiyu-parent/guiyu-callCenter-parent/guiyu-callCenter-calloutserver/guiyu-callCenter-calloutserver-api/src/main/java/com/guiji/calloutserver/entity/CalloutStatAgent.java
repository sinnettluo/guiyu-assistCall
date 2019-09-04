package com.guiji.calloutserver.entity;

import lombok.Data;

/**
 * 呼出坐席统计
 *
 * @author Zhouzl
 * @date 2019年07月16日
 * @since 1.0
 */
@Data
public class CalloutStatAgent {
    /**
     * 坐席名
     */
    private Long customerId;
    /**
     * 意向标签
     */
    private String intent;
    /**
     * 数量
     */
    private int number;
    /**
     * 呼叫时长
     */
    private int callTime;
}
