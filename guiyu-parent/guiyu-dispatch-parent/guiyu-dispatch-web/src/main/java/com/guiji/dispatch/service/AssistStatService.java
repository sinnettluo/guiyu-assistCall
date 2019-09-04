package com.guiji.dispatch.service;

import com.guiji.component.result.Result;
import com.guiji.robot.service.vo.AssistStatResp;

/**
 * 协呼统计服务
 *
 * @author Zhouzl
 * @date 2019年08月19日
 * @since 1.0
 */
public interface AssistStatService {
    /**
     * 统计坐席一个月内和一天内呼叫的数量
     *
     * @param userId
     * @param orgId
     * @return
     */
    Result.ReturnData<AssistStatResp> stat(Long userId, Integer orgId);
}
