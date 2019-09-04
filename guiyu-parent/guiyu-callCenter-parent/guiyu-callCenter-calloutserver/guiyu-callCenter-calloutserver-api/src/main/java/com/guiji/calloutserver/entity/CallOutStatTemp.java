package com.guiji.calloutserver.entity;

import lombok.Data;

/**
 * @author Zhouzl
 * @date 2019年07月15日
 * @since 1.0
 */
@Data
public class CallOutStatTemp {
    private String tempId;
    private String intent;
    private int durations;
    private int totals;
}
