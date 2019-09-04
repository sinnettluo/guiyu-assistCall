package com.guiji.ws.websocket;

import com.guiji.dispatch.api.IDispatchPlanOut;
import com.guiji.utils.JsonUtils;
import com.guiji.ws.cfg.GetHttpHeaderConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @version V1.0
 * @ClassName: TotalPlanCountWebSocket
 * @Description: 首页实时统计更新任务计划数
 * @date 2019年6月21日 下午5:21:13
 */
@ServerEndpoint(value = "/webSocket/totalPlanCount/{userId}/{orgId}", configurator = GetHttpHeaderConfig.class)
@Component
@Slf4j
public class TotalPlanCountWebSocket {
    //concurrent包的线程安全Set，用来存放每个客户端对应的RobotWebSocket对象
    private static CopyOnWriteArraySet<TotalPlanCountWebSocket> webSocketSet = new CopyOnWriteArraySet<TotalPlanCountWebSocket>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    private String userId;
    private Integer orgId;
    private ScheduledExecutorService scheduledExecutorService;
    private volatile boolean toStop = false;
    private static ApplicationContext applicationContext;

    /**
     * 启动类中设置spring容器
     *
     * @param context
     */
    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    @OnOpen
    public void onOpen(@PathParam("userId") String userId, @PathParam("orgId") Integer orgId, Session session, EndpointConfig config) {
        log.info("TotalPlanCountWebSocket有新连接加入！加入用户：{}", userId);
        this.session = session;
        webSocketSet.add(this);
        this.userId = userId;
        this.orgId = orgId;
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        this.scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                IDispatchPlanOut iDispatchPlanOut = (IDispatchPlanOut) applicationContext.getBean("com.guiji.dispatch.api.IDispatchPlanOut");
                sendMessage(JsonUtils.bean2Json(iDispatchPlanOut.totalPlanCount(userId, this.orgId)));
            } catch (IOException e) {
                log.error("IO异常", e);
            }
        }, 0, 5000, TimeUnit.MILLISECONDS);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        try {
            webSocketSet.remove(this);  //从set中删除
            this.scheduledExecutorService.shutdownNow();
        } catch (Exception e) {
            log.error("", e);
        }
        log.info("TotalPlanCountWebSocket链接关闭！用户id：{}", this.userId);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("TotalPlanCountWebSocket接收客户端的消息:" + message);
        //原样返回，保持心跳
        session.getAsyncRemote().sendText(message);
//        //群发消息
//        for (TotalPlanCountWebSocket item : webSocketSet) {
//            try {
//                item.sendMessage(message);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("TotalPlanCountWebSocket发生错误", error);
//        webSocketSet.remove(this);  //从set中删除
        this.onClose();
    }

    /**
     * 往客户端发送消息
     *
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        log.info("TotalPlanCountWebSocket发送消息内容{}", message);
        this.session.getBasicRemote().sendText(message);
    }
}
