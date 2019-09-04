package com.guiji.dispatch.controller;

import com.guiji.component.result.Result;
import com.guiji.dispatch.api.AssistAgentApi;
import com.guiji.dispatch.service.AssistAgentService;
import com.guiji.dispatch.service.AssistDispatchService;
import com.guiji.dispatch.service.AssistStatService;
import com.guiji.robot.service.vo.AssistStatResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 协呼坐席接口,包括
 * <p>
 * 1.坐席挂起<br>
 * 2.坐席下线任务回退<br>
 * 3.统计数据
 * </p>
 *
 * @author Zhouzl
 * @date 2019年08月14日
 * @since 1.0
 */
@RestController
public class AssistAgentController implements AssistAgentApi {
    @Autowired
    private AssistAgentService assistAgentService;
    @Autowired
    private AssistStatService assistStatService;
    @Autowired
    private AssistDispatchService assistDispatchService;

    @Override
    public Result.ReturnData<Boolean> onLine(@RequestParam("userId") Long userId, @RequestParam("orgId") Integer orgId) {
        //确保任务调度,如果存在挂起锁,则解锁
        assistAgentService.releaseTaskLock(userId);
        return Result.ok(true);
    }

    @Override
    public Result.ReturnData<Boolean> offLine(@RequestParam("userId") Long userId, @RequestParam("orgId") Integer orgId) {
        //回退协呼任务
        assistAgentService.rollbackTasks(userId, orgId);
        return Result.ok(true);
    }

    @Override
    public Result.ReturnData<Boolean> hangUp(@RequestParam("userId") Long userId) {
        //锁住挂起锁
        return Result.ok(assistAgentService.lockTaskLock(userId));
    }

    @Override
    public Result.ReturnData<Boolean> hangDown(@RequestParam("userId") Long userId) {
        //释放挂起锁,并触发一次调度
        assistAgentService.releaseTaskLock(userId);
        assistDispatchService.dispatch(userId);
        return Result.ok(true);
    }

    @Override
    public Result.ReturnData dispatch(Long userId) {
        return assistDispatchService.dispatch(userId);
    }

    @Override
    public void broken(Long userId) {
        assistDispatchService.agentBroken(userId);
    }

    @Override
    public void reconnect(Long userId) {
        assistDispatchService.agentReconnected(userId);
    }

    /**
     * 获取坐席是否处于挂起状态
     *
     * @param userId
     * @return
     */
    @GetMapping("dispatch/assist/isHangUp")
    public Result.ReturnData<Boolean> isHangUp(@RequestHeader("userId") Long userId) {
        return Result.ok(assistAgentService.existTaskLock(userId));
    }

    /**
     * 获取坐席协呼统计数据
     *
     * @param userId
     * @param orgId
     * @return
     */
    @GetMapping("dispatch/assist/stat")
    public Result.ReturnData<AssistStatResp> stat(@RequestHeader("userId") Long userId, @RequestHeader("orgId") Integer orgId) {
        return assistStatService.stat(userId, orgId);
    }
}
