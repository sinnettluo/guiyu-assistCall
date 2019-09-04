package com.guiji.da.service.vo;

import lombok.Data;

/**
 * @author Zhouzl
 * @date 2019年07月15日
 * @since 1.0
 */
@Data
public class CallAssistStatReq {
    private String tempId;
    private String startTime;
    private String endTime;
    private Integer agentId;

    public void setStartTime(String startTime) {
        if(null != startTime && startTime.length() == 10){
            this.startTime = startTime + " 00:00:00.000";
        }
    }

    public void setEndTime(String endTime) {
        if(null != endTime && endTime.length() == 10){
            this.endTime = endTime + " 23:59:59.999";
        }
    }
}
