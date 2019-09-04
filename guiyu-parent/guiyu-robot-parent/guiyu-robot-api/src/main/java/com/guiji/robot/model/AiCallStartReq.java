package com.guiji.robot.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/** 
* @ClassName: AiCallStartReq 
* @Description: AI电话发起请求信息
* @date 2018年11月15日 下午1:48:26 
* @version V1.0  
*/
//@Data
@ApiModel(value="AiCallStartReq对象",description="发起AI电话拨打请求")
public class AiCallStartReq {
	@ApiModelProperty(value="会话ID-调度中心会话id",required=true)
	private String disSeqId;
	@ApiModelProperty(value="会话ID，该电话整个会话过程中唯一编号",required=true)
	private String seqId;
	@ApiModelProperty(value="机器人编号",required=true)
	private String aiNo;
	@ApiModelProperty(value="用户编号",required=true)
	private String userId;
	@ApiModelProperty(value="话术模板编号",required=true)
	private String templateId;
	@ApiModelProperty(value="号码",required=true)
	private String phoneNo;
	@ApiModelProperty(value="坐席信息")
	private Integer agentUserId;
	//通话开始时间
	private Date aiCallStartTime;

	public String getSeqId() {
		return seqId;
	}

	public void setSeqId(String seqId) {
		this.seqId = seqId;
	}

	public String getAiNo() {
		return aiNo;
	}

	public void setAiNo(String aiNo) {
		this.aiNo = aiNo;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getDisSeqId() {
		return disSeqId;
	}

	public void setDisSeqId(String disSeqId) {
		this.disSeqId = disSeqId;
	}

	public Integer getAgentUserId() {
		return agentUserId;
	}

	public void setAgentUserId(Integer agentUserId) {
		this.agentUserId = agentUserId;
	}

	public Date getAiCallStartTime() {
		return aiCallStartTime;
	}

	public void setAiCallStartTime(Date aiCallStartTime) {
		this.aiCallStartTime = aiCallStartTime;
	}
}
