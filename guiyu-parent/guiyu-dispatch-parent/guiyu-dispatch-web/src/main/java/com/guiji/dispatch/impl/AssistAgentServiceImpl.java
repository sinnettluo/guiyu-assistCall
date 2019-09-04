package com.guiji.dispatch.impl;

import com.guiji.component.lock.DistributedLockHandler;
import com.guiji.dispatch.dao.DispatchPlanMapper;
import com.guiji.dispatch.service.AssistAgentService;
import com.guiji.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Zhouzl
 * @date 2019年08月14日
 * @since 1.0
 */
@Slf4j
@Service
public class AssistAgentServiceImpl implements AssistAgentService {
    @Autowired
    private DistributedLockHandler lockHandler;
    @Autowired
    private DispatchPlanMapper dispatchPlanMapper;
    @Autowired
    private RedisUtil redisUtil;
    private final String REDIS_TASK_LOCK = "REDIS_TASK_LOCK_";

    @Override
    public void rollbackTasks(Long userId, Integer orgId) {
        //判断当前是否挂起
        boolean taskNotLocked = !existTaskLock(userId);
        if (taskNotLocked) {
            //当前没有挂起,需要先挂锁,再回退任务
            log.info("用户下线,任务回退->挂锁");
            lockTaskLock(userId);
        }
        log.info("用户下线,任务回退->开始");
        //坐席任务回退
        dispatchPlanMapper.rollbackAssistTasks(userId.intValue(), orgId);
        log.info("用户下线,任务回退->结束");
        if (taskNotLocked) {
            //如果任务回退时,没有任务挂起,则释放挂起锁
            releaseTaskLock(userId);
        }
    }

    @Override
    public boolean lockTaskLock(Long userId) {
        return existTaskLock(userId) ? true : redisUtil.set(REDIS_TASK_LOCK + userId, userId);
    }

    @Override
    public boolean existTaskLock(Long userId) {
        return redisUtil.hasKey(REDIS_TASK_LOCK + userId);
    }

    @Override
    public void releaseTaskLock(Long userId) {
        redisUtil.del(REDIS_TASK_LOCK + userId);
    }
}
