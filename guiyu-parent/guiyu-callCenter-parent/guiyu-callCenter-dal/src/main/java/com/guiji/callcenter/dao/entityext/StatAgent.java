package com.guiji.callcenter.dao.entityext;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Zhouzl
 * @date 2019年07月16日
 * @since 1.0
 */
@Data
public class StatAgent implements Serializable {
    private static final long serialVersionUID = 477410277133360370L;
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
