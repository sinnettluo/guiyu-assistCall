package com.guiji.calloutserver.eventbus.event;

import com.google.common.base.Strings;
import lombok.Data;

import java.util.Date;

@Data
public class ChannelAnswerEvent {
    private String uuid;
    private String callerNum;
    private String calledNum;
    private String accessNum;
    Date channelAnsweredTime;

    /**
     * 是否外部呼入到机器人
     * @return
     */
    public boolean isCallToBot(){
        return !Strings.isNullOrEmpty(accessNum);
    }
}
