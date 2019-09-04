package com.guiji.dispatch.model;

import lombok.Data;

/**
 * @author Zhouzl
 * @date 2019年07月16日
 * @since 1.0
 */
@Data
public class AgentGroupStat {
    /**
     * 坐席组
     */
    private Long agentGroupId;
    /**
     * 状态
     */
    private String status;
    /**
     * 数量
     */
    private int number;
}
