package com.guiji.ws.service;

import com.guiji.dispatch.api.AssistAgentApi;
import com.guiji.user.dao.entity.SysOrganization;
import com.guiji.utils.StrUtils;
import com.guiji.ws.model.OnlineUser;
import com.guiji.ws.util.DataLocalCacheUtil;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * @version V1.0
 * @ClassName: MonitorUserService
 * @Description: 监控用户服务
 * @date 2019年2月22日 下午6:24:06
 */
@Slf4j
@Service
public class WsUserService {
    @Autowired
    DataLocalCacheUtil dataLocalCacheUtil;
    @Autowired
    RdCacheService rdCacheService;
    @Autowired
    private AssistAgentApi assistAgentApi;


    /**
     * 某个实时通话监控用户上线
     *
     * @param userId
     */
    public OnlineUser wsUserOnLine(String sence, String userId) {
        if (StrUtils.isNotEmpty(userId) && StrUtils.isNotEmpty(sence)) {
            OnlineUser user = new OnlineUser();
            SysOrganization org = dataLocalCacheUtil.queryUserRealOrg(userId);
            user.setUserId(userId);    //用户编号
            user.setOrgCode(org.getCode());    //用户真实企业
            user.setAssistCallUser(this.hasAssistRole(userId)); //是否协呼
            user.setUuid(UUID.randomUUID().toString());
            //放进缓存
            rdCacheService.cacheOnlineUser(sence, user);
            //通知调度中心,用户上线,准备调度
            assistAgentApi.onLine(Long.valueOf(userId), org.getId());
            return user;
        }
        return null;
    }

    /**
     * 某个实时通话监控的用户下线
     *
     * @param userId
     */
    @Synchronized
    public void wsUserOffLine(String sence, String userId) {
        if (StrUtils.isNotEmpty(sence) && StrUtils.isNotEmpty(userId)) {
            rdCacheService.delOnlineUser(sence, userId);
            assistAgentApi.offLine(Long.valueOf(userId), dataLocalCacheUtil.queryUserRealOrg(userId).getId());
        }
    }

    /**
     * 坐席断线,通知调度中心
     *
     * @param userId
     */
    public void wsUserBrokenLine(String userId) {
        assistAgentApi.broken(Long.valueOf(userId));
    }

    /**
     * 坐席重连,通知调度中心,并且执行一次调度
     *
     * @param userId
     */
    public void wsUserReconnected(String userId) {
        assistAgentApi.reconnect(Long.valueOf(userId));
        assistAgentApi.dispatch(Long.valueOf(userId));
    }

    /**
     * 查询某个场景的websocket在线用户
     *
     * @param sence
     * @return
     */
    public List<OnlineUser> queryOnlineUser(String sence, String orgCode) {
        if (StrUtils.isNotEmpty(sence) && StrUtils.isNotEmpty(orgCode)) {
            return rdCacheService.queryOnlineUserByOrgCode(sence, orgCode);
        }
        return null;
    }


    /**
     * 判断用户是否具有协呼角色
     *
     * @param userId
     * @return
     */
    private boolean hasAssistRole(String userId) {
        return dataLocalCacheUtil.isAgentUser(userId);
    }


}
