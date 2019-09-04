package com.guiji.robot.service.vo;

import lombok.Data;

/**
 * @author Zhouzl
 * @date 2019年08月16日
 * @since 1.0
 */
@Data
public class AssistStatResp {
    private int monthCount;
    private int dayCount;

    public AssistStatResp() {
    }

    public AssistStatResp(int monthCount, int dayCount) {
        this.monthCount = monthCount;
        this.dayCount = dayCount;
    }
}
