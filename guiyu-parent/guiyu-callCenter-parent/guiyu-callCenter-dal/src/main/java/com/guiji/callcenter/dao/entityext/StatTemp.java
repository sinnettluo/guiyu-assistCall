package com.guiji.callcenter.dao.entityext;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Zhouzl
 * @date 2019年07月15日
 * @since 1.0
 */
@Data
public class StatTemp implements Serializable {
    private static final long serialVersionUID = -8493739447808279360L;
    private String tempId;
    private String intent;
    private int durations;
    private int totals;
}
