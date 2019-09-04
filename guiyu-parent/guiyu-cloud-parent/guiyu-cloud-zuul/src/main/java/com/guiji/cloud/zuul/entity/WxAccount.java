package com.guiji.cloud.zuul.entity;

import java.util.Date;


public class WxAccount {

    private Integer id;
    private Long userId;
    private Boolean isSuperAdmin;
    private String orgCode;
    private Date lastTime;
    private Integer isDesensitization;
	private Integer authLevel;
    private Long orgId;
    private Long roleId;

    public Long getRoleId()
	{
		return roleId;
	}

	public void setRoleId(Long roleId)
	{
		this.roleId = roleId;
	}

	public Long getOrgId()
	{
		return orgId;
	}

	public void setOrgId(Long orgId)
	{
		this.orgId = orgId;
	}

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Boolean getSuperAdmin() {
        return isSuperAdmin;
    }

    public void setSuperAdmin(Boolean superAdmin) {
        isSuperAdmin = superAdmin;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public Date getLastTime() {
        return lastTime;
    }

    public void setLastTime(Date lastTime) {
        this.lastTime = lastTime;
    }

    public Integer getIsDesensitization() {
        return isDesensitization;
    }

    public void setIsDesensitization(Integer isDesensitization) {
        this.isDesensitization = isDesensitization;
    }
	public Integer getAuthLevel() {
		return authLevel;
	}
	public void setAuthLevel(Integer authLevel) {
		this.authLevel = authLevel;
	}
}
