package com.guiji.dispatch.api;

import com.guiji.component.result.Result;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Zhouzl
 * @date 2019年08月14日
 * @since 1.0
 */
@FeignClient("GUIYU-DISPATCH-WEB")
public interface AssistAgentApi {
    /**
     * 下线通知,用于坐席关闭协呼,或者WebSocket连接断开时间大于5分钟则视为坐席下线,回退坐席已领取任务并释放挂起锁
     *
     * @param userId
     * @return
     */
    @GetMapping("dispatch/assist/offline")
    Result.ReturnData<Boolean> offLine(@RequestParam("userId") Long userId, @RequestParam("orgId") Integer orgId);

    /**
     * 上线通知,用于坐席开启协呼,建立WebSocket连接之后,开始进行任务调度
     *
     * @param userId
     * @param orgId
     * @return
     */
    @GetMapping("dispatch/assist/online")
    Result.ReturnData<Boolean> onLine(@RequestParam("userId") Long userId, @RequestParam("orgId") Integer orgId);

    /**
     * 坐席挂起,添加挂起锁,防止任务调度
     *
     * @param userId
     * @return
     */
    @GetMapping("dispatch/assist/hangUp")
    Result.ReturnData<Boolean> hangUp(@RequestParam("userId") Long userId);

    /**
     * 坐席挂起状态解除,删除挂起锁,继续进行任务调度
     *
     * @param userId
     * @return
     */
    @GetMapping("dispatch/assist/hangDown")
    Result.ReturnData<Boolean> hangDown(@RequestParam("userId") Long userId);

    @GetMapping("dispatch/assist/dispatch")
    Result.ReturnData dispatch(@RequestParam("userId") Long userId);

    @GetMapping("dispatch/assist/broken")
    void broken(@RequestParam("userId") Long userId);

    @GetMapping("dispatch/assist/reconnect")
    void reconnect(@RequestParam("userId") Long userId);
}
