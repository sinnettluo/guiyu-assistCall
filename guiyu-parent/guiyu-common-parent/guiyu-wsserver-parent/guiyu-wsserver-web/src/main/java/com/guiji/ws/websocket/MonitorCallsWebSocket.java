package com.guiji.ws.websocket;

import com.guiji.ws.model.OnlineUser;
import com.guiji.ws.model.WebSocketConnect;
import com.guiji.ws.service.WebSocketConnectService;
import com.guiji.ws.service.WsUserService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @version V1.0
 * @ClassName: RobotWebSocket
 * @Description: 监控实时通话信息ws长链接
 * @date 2019年2月21日 下午7:21:13
 */
@ServerEndpoint("/webSocket/robot/{sence}/{userId}")
@Component
@Slf4j
@Data
public class MonitorCallsWebSocket {
    private int waitTime = 5;
    //连接的唯一编号
    private String uuid;
    private String orgCode;
    //连接场景
    private String sence;
    private String userId;
    private Session session;
    //是否协呼人员
    private boolean assistCallUser;
    //是否在线
    private boolean onLine = true;
    private boolean readyToOffLine = false;
    private ScheduledExecutorService scheduledExecutorService;
    //concurrent包的线程安全Set，用来存放每个客户端对应的RobotWebSocket对象
    private static CopyOnWriteArraySet<MonitorCallsWebSocket> clients = new CopyOnWriteArraySet<>();
    //spring 容器
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
    public void onOpen(@PathParam("sence") String sence, @PathParam("userId") String userId, Session session) {
        try {
            this.userId = userId;
            this.sence = sence;
            this.session = session;
            String waitTime = applicationContext.getEnvironment().getProperty("broken.waitTime");
            try {
                if (waitTime != null) {
                    this.waitTime = Integer.valueOf(waitTime);
                }
            } catch (NumberFormatException e) {
                log.error("配置断线等待时间错误:{}", waitTime);
            }
            WebSocketConnectService webSocketConnectService = (WebSocketConnectService) applicationContext.getBean("webSocketConnectService"); //实时连接服务
            WsUserService monitorUserService = (WsUserService) applicationContext.getBean("wsUserService"); //实时监控用户服务
            if (isReOnline(webSocketConnectService)) {
                monitorUserService.wsUserReconnected(userId);
                return;
            }
            clients.add(this);
            log.info("有新连接加入！场景：{},加入用户：{}", sence, userId);
            //监控用户上线
            OnlineUser user = monitorUserService.wsUserOnLine(sence, userId);
            this.orgCode = user.getOrgCode();
            this.assistCallUser = user.isAssistCallUser();
            this.uuid = user.getUuid();
            webSocketConnectService.openWs(this);
        } catch (Exception e) {
            log.error("WS OPEN异常", e);
        }
    }

    /**
     * 连接关闭调用的方法,连接进入倒计时5分钟,如果超时,通知调度中心用户下线,回退坐席任务
     */
    @OnClose
    public void onClose() {
        this.onLine = false;
        log.info("有一连接关闭！场景：{},用户编号：{}", this.sence, this.userId);
        WsUserService monitorUserService = (WsUserService) applicationContext.getBean("wsUserService"); //实时监控用户服务
        WebSocketConnectService webSocketConnectService = (WebSocketConnectService) applicationContext.getBean("webSocketConnectService"); //实时连接服务
        webSocketConnectService.closeWs(this);
        if (readyToOffLine) {
            log.info("坐席下线！场景：{},用户编号：{}", this.sence, this.userId);
            clients.remove(this);
            monitorUserService.wsUserOffLine(sence, userId);
        } else {
            //监控用户任务挂起,执行下线等待(5分钟)
            startSchedule(monitorUserService);
            monitorUserService.wsUserBrokenLine(userId);
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("来自客户端的消息:" + message);
        if ("offLine".equals(message)) {
            readyToOffLine = true;
        }
        //原样返回，保持心跳
        session.getAsyncRemote().sendText(message);
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误", error);
        this.onClose();
    }

    /**
     * 往客户端发送消息
     *
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        if (this.session.isOpen()) {
            this.session.getAsyncRemote().sendText(message);
        } else {
            log.error("session已关闭,无法发送消息:{}", message);
        }
    }

    /**
     * 群发自定义消息
     */
    public static void sendInfo(String message) throws IOException {
        for (MonitorCallsWebSocket item : clients) {
            try {
                if (item.isOnLine()) {
                    item.sendMessage(message);
                }
            } catch (IOException e) {
            }
        }
    }


    /**
     * 查询websocket
     *
     * @param connect
     * @return
     */
    public static MonitorCallsWebSocket queryWsSocketBy(WebSocketConnect connect) {
        if (connect != null) {
            String connectUUid = connect.getUuid();
            if (clients != null) {
                for (MonitorCallsWebSocket socket : clients) {
                    if (connectUUid.equals(socket.getUuid()) && socket.isOnLine()) {
                        return socket;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 查询场景下用户连接
     *
     * @param sence
     * @param userId
     * @return
     */
    public static MonitorCallsWebSocket findByUserId(String sence, String userId) {
        Optional<MonitorCallsWebSocket> optional = clients.stream()
                .filter(monitorCallsWebSocket -> monitorCallsWebSocket.getUserId().equals(userId) && monitorCallsWebSocket.getSence().equals(sence))
                .findAny();
        return optional.orElse(null);
    }

    /**
     * 如果本坐席连接存在,则本连接转接过去
     */
    public boolean isReOnline(WebSocketConnectService webSocketConnectService) {
        MonitorCallsWebSocket monitorCallsWebSocket = findByUserId(sence, userId);
        if (monitorCallsWebSocket != null) {
            //如果坐席WebSocket连接异常中断(5分钟内),需要终止下线等待
            monitorCallsWebSocket.stopSchedule();
            if (monitorCallsWebSocket.getSession().isOpen()) {
                try {
                    monitorCallsWebSocket.sendMessage("{\"message\":\"坐席账号重复上线,连接已转接\"}");
                    monitorCallsWebSocket.getSession().close();
                    monitorCallsWebSocket.stopSchedule();
                } catch (IOException e) {
                    //如通知不到,毋需处理
                }
            }
            this.orgCode = monitorCallsWebSocket.getOrgCode();
            this.assistCallUser = monitorCallsWebSocket.isAssistCallUser();
            this.uuid = monitorCallsWebSocket.getUuid();
            clients.remove(monitorCallsWebSocket);
            clients.add(this);
            webSocketConnectService.openWs(this);
            log.info("有坐席重连！场景：{},加入用户：{}", sence, userId);
            return true;
        }
        return false;
    }

    /**
     * 连接中断,开始下线等待(5分钟)
     *
     * @param wsUserService
     */
    public void startSchedule(WsUserService wsUserService) {
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        this.scheduledExecutorService.schedule(() -> {
            //连接断开超过5分钟,执行坐席下线,移除本条连接
            if (!isOnLine()) {
                clients.remove(this);
                log.info("业务场景场景{}由于连接断开时间超过{}分钟,用户{}下线!", sence, waitTime, userId);
                wsUserService.wsUserOffLine(sence, userId);
            }
        }, waitTime, TimeUnit.MINUTES);
    }

    /**
     * 连接重建,停止下线等待
     */
    public void stopSchedule() {
        if (this.scheduledExecutorService != null) {
            this.scheduledExecutorService.shutdownNow();
            this.scheduledExecutorService = null;
        }
    }
}
