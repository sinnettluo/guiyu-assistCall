package com.guiji.robot.dao.entity;

import java.io.Serializable;
import java.util.Date;

public class UserAiCfgHisInfo implements Serializable {
    private Integer id;

    private Integer busiId;

    private String userId;

    private Integer aiNum;

    private Integer assignLevel;

    private String templateId;

    private String openDate;

    private String invalidDate;

    private Integer status;

    private String invalidPolicy;

    private Integer handleType;

    private Date crtTime;

    private String crtUser;

    private Date updateTime;

    private String updateUser;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBusiId() {
        return busiId;
    }

    public void setBusiId(Integer busiId) {
        this.busiId = busiId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    public Integer getAiNum() {
        return aiNum;
    }

    public void setAiNum(Integer aiNum) {
        this.aiNum = aiNum;
    }

    public Integer getAssignLevel() {
        return assignLevel;
    }

    public void setAssignLevel(Integer assignLevel) {
        this.assignLevel = assignLevel;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId == null ? null : templateId.trim();
    }

    public String getOpenDate() {
        return openDate;
    }

    public void setOpenDate(String openDate) {
        this.openDate = openDate == null ? null : openDate.trim();
    }

    public String getInvalidDate() {
        return invalidDate;
    }

    public void setInvalidDate(String invalidDate) {
        this.invalidDate = invalidDate == null ? null : invalidDate.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getInvalidPolicy() {
        return invalidPolicy;
    }

    public void setInvalidPolicy(String invalidPolicy) {
        this.invalidPolicy = invalidPolicy == null ? null : invalidPolicy.trim();
    }

    public Integer getHandleType() {
        return handleType;
    }

    public void setHandleType(Integer handleType) {
        this.handleType = handleType;
    }

    public Date getCrtTime() {
        return crtTime;
    }

    public void setCrtTime(Date crtTime) {
        this.crtTime = crtTime;
    }

    public String getCrtUser() {
        return crtUser;
    }

    public void setCrtUser(String crtUser) {
        this.crtUser = crtUser == null ? null : crtUser.trim();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser == null ? null : updateUser.trim();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", busiId=").append(busiId);
        sb.append(", userId=").append(userId);
        sb.append(", aiNum=").append(aiNum);
        sb.append(", assignLevel=").append(assignLevel);
        sb.append(", templateId=").append(templateId);
        sb.append(", openDate=").append(openDate);
        sb.append(", invalidDate=").append(invalidDate);
        sb.append(", status=").append(status);
        sb.append(", invalidPolicy=").append(invalidPolicy);
        sb.append(", handleType=").append(handleType);
        sb.append(", crtTime=").append(crtTime);
        sb.append(", crtUser=").append(crtUser);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", updateUser=").append(updateUser);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}