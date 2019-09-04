package com.guiji.toagentserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @Auther: 魏驰
 * @Date: 2019/1/21 10:30
 * @Project：guiyu-parent
 * @Description:
 */
@Data
@AllArgsConstructor
public class AgentGroupInfo {
    private String groupId;
    private String groupName;
    private List<Integer> userId;
}
