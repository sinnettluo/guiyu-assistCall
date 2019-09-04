package com.guiji.dispatch.service;

/**
 * @author Zhouzl
 * @date 2019年08月14日
 * @since 1.0
 */
public interface AssistAgentService {
    /**
     * 回退用户任务
     *
     * @param userId
     */
    void rollbackTasks(Long userId, Integer orgId);

    /**
     * 添加任务锁,停止调度(即挂起状态)
     *
     * @param userId
     * @return
     */
    boolean lockTaskLock(Long userId);

    /**
     * 判断是否存在任务锁
     *
     * @param userId
     * @return
     */
    boolean existTaskLock(Long userId);

    /**
     * 释放任务锁(解除挂起状态)
     *
     * @param userId
     */
    void releaseTaskLock(Long userId);
}
