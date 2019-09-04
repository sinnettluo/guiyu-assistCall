package com.guiji.dispatch.service;

import com.guiji.component.result.Result;

/**
 * 协呼调度服务()
 *
 * @author Zhouzl
 * @date 2019年08月19日
 * @since 1.0
 */
public interface AssistDispatchService {
    Result.ReturnData dispatch(Long userId);

    /**
     * 坐席断线
     *
     * @param userId
     */
    void agentBroken(Long userId);

    /**
     * 坐席重连
     *
     * @param userId
     */
    void agentReconnected(Long userId);
}
