package com.guiji.robot.service.job;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.guiji.component.lock.DistributedLockHandler;
import com.guiji.component.lock.Lock;
import com.guiji.component.result.Result.ReturnData;
import com.guiji.robot.model.AiHangupReq;
import com.guiji.robot.service.impl.AiCacheService;
import com.guiji.robot.service.impl.MonitorUserService;
import com.guiji.robot.service.vo.CallInfo;
import com.guiji.robot.service.vo.MonitorCallData;
import com.guiji.utils.JsonUtils;
import com.guiji.utils.RedisUtil;
import com.guiji.utils.StrUtils;
import com.guiji.ws.api.WsConnectApi;
import com.guiji.ws.api.WsOnlineUserApi;
import com.guiji.ws.model.WebSocketConnect;
import com.guiji.ws.model.WsMsg;
import com.guiji.ws.model.WsSenceEnum;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @version V1.0
 * @ClassName: MonitorCallsJobTimer
 * @Description: 通话实时监控服务
 * @date 2019年2月25日 下午10:02:34
 */
@Component
@JobHandler(value = "monitorCallsJobTimer")
@Slf4j
public class MonitorCallsJobTimer extends IJobHandler {

    public static final String WS_MSG_PREFIX = "robot_send_ws_msg_prefix_";
    public static final String WS_MSG_USER_PREFIX = "robot_send_ws_user_prefix_";
    public static final String WS_MSG_LOCK = "robot_send_ws_msg_lock_";

    @Autowired
    DistributedLockHandler lockHandler;
    @Autowired
    WsConnectApi wsConnectApi;
    @Autowired
    WsOnlineUserApi wsOnlineUserApi;
    @Autowired
    MonitorUserService monitorUserService;
    @Autowired
    AiCacheService aiCacheService;
    @Autowired
    RedisUtil redisUtil;

    /**
     * 通话情况实时监控
     */
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        try {
            //获取实时监控 ws连接
            ReturnData<CopyOnWriteArraySet<WebSocketConnect>> listData = wsConnectApi.queryWsConnects(WsSenceEnum.montiorcall.toString());
            CopyOnWriteArraySet<WebSocketConnect> list = listData.getBody();
            if (list != null && !list.isEmpty()) {
                Map<String, List<CallInfo>> orgUserCallMap = new HashMap<>();
                Map<String, List<WsMsg>> webSocketMap = new HashMap<>();

                List<WebSocketConnect> connects = new ArrayList<>();

                list.forEach(obj -> {
                    connects.add(obj);
                });

                for (WebSocketConnect socket : list) {
                    WsMsg wsMsg = new WsMsg();
                    wsMsg.setConnect(socket);
                    String orgCode = socket.getOrgCode();

                    //将websocket信息根据组织code进行分组
                    if (webSocketMap.containsKey(orgCode)) {
                        webSocketMap.get(orgCode).add(wsMsg);
                    } else {
                        List<WsMsg> msgs = new ArrayList<>();
                        msgs.add(wsMsg);
                        webSocketMap.put(orgCode, msgs);
                    }

                    String userId = socket.getUserId();
                    boolean isAssignUser = socket.isAssistCallUser();    //是否协呼人员
                    if (StrUtils.isNotEmpty(userId)) {
                        //查询该用户需要监控的实时通话
                        List<CallInfo> myCallList = null;
                        if (isAssignUser) {
                            //协呼人员，先查询缓存数据中是否已经查过了
                            if (orgUserCallMap.get(orgCode) == null) {
                                MonitorCallData data = monitorUserService.queryMoniorCallList(userId);
                                if (data != null && CollectionUtils.isNotEmpty(data.getOneUserCallList())) {
                                    //获取企业下所有用户的数据
                                    myCallList = data.getOneUserCallList();

                                    orgUserCallMap.put(orgCode, myCallList);
                                }
                            }
                        }
                    }
                }
                sendWsMsg(webSocketMap, orgUserCallMap);
            }
            //1S循环一次
            Thread.sleep(1000);
        } catch (Exception e) {
            //throw e;
            XxlJobLogger.log("循环监听实时通话服务发生异常!!!: {}", e.getMessage());
            Thread.sleep(2000);
        }
        return SUCCESS;
    }

    /**
     * 发送websocket消息
     *
     * @param websocketMap
     * @param orgUserCallMap
     */
    private void sendWsMsg(Map<String, List<WsMsg>> websocketMap, Map<String, List<CallInfo>> orgUserCallMap) {

        websocketMap.forEach((orgCode, list) -> {

            if (orgUserCallMap.containsKey(orgCode)) {
                sendByOrgCode(list, orgUserCallMap.get(orgCode));
            } else {
                sendEmpty(list);
            }

        });
    }

    /**
     * 根据组织code进行通话记录分配
     *
     * @param list
     * @param callInfos
     */
    private void sendByOrgCode(List<WsMsg> list, List<CallInfo> callInfos) {

        //把某个通话记录发送给指定的userId的所有客户端，发送过消息的，将从list中清除
        callInfos.forEach(obj -> {
            sendToAllUserClient(list, obj);
        });

        //依然存在的websocket，发送空消息
        if (CollectionUtils.isNotEmpty(list)) {
            sendEmpty(list);
        }

    }

    /**
     * 发送空消息
     *
     * @param wsMsgs
     */
    private void sendEmpty(List<WsMsg> wsMsgs) {
//        wsMsgs.forEach(obj -> {
//            obj.setMessage("");
//            wsConnectApi.asyncSendMsg(obj);
//        });
        //do nothing
    }

    /**
     * 发送消息给同userId下的所有websocket连接
     * 注意消息锁定，即一个通话记录只能分给一个用户，一个用户最多指定一条通话记录
     *
     * @param list
     * @param callInfo
     */
    private void sendToAllUserClient(List<WsMsg> list, CallInfo callInfo) {

        Iterator<WsMsg> iterator = list.iterator();

        while (iterator.hasNext()) {

            WsMsg wsMsg = iterator.next();

            String uuid = wsMsg.getConnect().getUuid();
            String userId = wsMsg.getConnect().getUserId();

            if(!userId.equals(callInfo.getAgentUserId().toString())) {
                continue;
            }

            //说明该流水号已被其他用户占用，无需进行后续操作
            if (redisUtil.get(WS_MSG_USER_PREFIX + callInfo.getSeqId()) != null && !userId.equals(redisUtil.get(WS_MSG_USER_PREFIX + callInfo.getSeqId()))) {
                continue;
            }

            //若uuid对应值为空，则进行uuid与seqid绑定操作，并设置流水号绑定了哪个用户
            if (redisUtil.get(WS_MSG_PREFIX + uuid) == null) {
                Lock lock = new Lock(WS_MSG_LOCK + uuid, "1");
                if (lockHandler.tryLock(lock)) {
                    try {
                        if (redisUtil.get(WS_MSG_PREFIX + uuid) == null) {
                            if (redisUtil.get(WS_MSG_USER_PREFIX + callInfo.getSeqId()) == null) {
                                redisUtil.set(WS_MSG_USER_PREFIX + callInfo.getSeqId(), userId, 10 * 60);
                            }
                            redisUtil.set(WS_MSG_PREFIX + uuid, callInfo.getSeqId(), 10 * 60);
                        }
                    } catch (Exception ex) {
                        log.info("推送通话记录时，获取锁失败，error: {}", ex.getMessage());
                    } finally {
                        lockHandler.releaseLock(lock);
                    }
                }
            }

            //如果流水号一致，并且流水号对应的用户一致，则发送消息
            if (callInfo.getSeqId().equals(redisUtil.get(WS_MSG_PREFIX + uuid))
                    && userId.equals(redisUtil.get(WS_MSG_USER_PREFIX + callInfo.getSeqId()))) {

                List<CallInfo> callInfos = new ArrayList<>();
                callInfos.add(callInfo);
                wsMsg.setMessage(JsonUtils.bean2Json(callInfos));
                wsConnectApi.asyncSendMsg(wsMsg);
                //发送过消息的，从队列中清除
                iterator.remove();
            }
        }
    }

    /**
     * 挂断通知
     *
     * @param req
     */
    @EventListener
    @Async
    public void notifyHangUp(AiHangupReq req) {

        ReturnData<CopyOnWriteArraySet<WebSocketConnect>> listData = wsConnectApi.queryWsConnects(WsSenceEnum.montiorcall.toString());
        CopyOnWriteArraySet<WebSocketConnect> list = listData.getBody();
        if (list != null && !list.isEmpty()) {
            for (WebSocketConnect socket : list) {
                WsMsg wsMsg = new WsMsg();
                wsMsg.setConnect(socket);

                String seqId = (String) redisUtil.get(WS_MSG_PREFIX + socket.getUuid());

                if (req.getSeqId().equals(seqId)) {

                    JSONArray jsonArray = new JSONArray();
                    JSONObject obj = new JSONObject();

                    obj.put("hangUpType", req.getHangUpType());
                    obj.put("seqId", req.getSeqId());

                    jsonArray.add(obj);

                    wsMsg.setMessage(JsonUtils.bean2Json(jsonArray));

                    wsConnectApi.asyncSendMsg(wsMsg);
                    //break;
                    redisUtil.del(WS_MSG_PREFIX + socket.getUuid());
                    redisUtil.del(WS_MSG_USER_PREFIX + seqId);
                }
            }
        }
    }

}
