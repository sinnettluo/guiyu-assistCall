package com.guiji.dispatch.dao.entity;

import java.io.Serializable;
import java.util.Date;

public class PlanBatch implements Serializable {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column dispatch_plan_batch.id
     *
     * @mbggenerated
     */
    private Integer id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column dispatch_plan_batch.user_id
     *
     * @mbggenerated
     */
    private Integer userId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column dispatch_plan_batch.name
     *
     * @mbggenerated
     */
    private String name;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column dispatch_plan_batch.status_show
     *
     * @mbggenerated
     */
    private Byte statusShow;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column dispatch_plan_batch.status_notify
     *
     * @mbggenerated
     */
    private Byte statusNotify;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column dispatch_plan_batch.times
     *
     * @mbggenerated
     */
    private Byte times;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column dispatch_plan_batch.gmt_create
     *
     * @mbggenerated
     */
    private Date gmtCreate;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column dispatch_plan_batch.gmt_modified
     *
     * @mbggenerated
     */
    private Date gmtModified;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table dispatch_plan_batch
     *
     * @mbggenerated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column dispatch_plan_batch.id
     *
     * @return the value of dispatch_plan_batch.id
     *
     * @mbggenerated
     */
    public Integer getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column dispatch_plan_batch.id
     *
     * @param id the value for dispatch_plan_batch.id
     *
     * @mbggenerated
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column dispatch_plan_batch.user_id
     *
     * @return the value of dispatch_plan_batch.user_id
     *
     * @mbggenerated
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column dispatch_plan_batch.user_id
     *
     * @param userId the value for dispatch_plan_batch.user_id
     *
     * @mbggenerated
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column dispatch_plan_batch.name
     *
     * @return the value of dispatch_plan_batch.name
     *
     * @mbggenerated
     */
    public String getName() {
        return name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column dispatch_plan_batch.name
     *
     * @param name the value for dispatch_plan_batch.name
     *
     * @mbggenerated
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column dispatch_plan_batch.status_show
     *
     * @return the value of dispatch_plan_batch.status_show
     *
     * @mbggenerated
     */
    public Byte getStatusShow() {
        return statusShow;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column dispatch_plan_batch.status_show
     *
     * @param statusShow the value for dispatch_plan_batch.status_show
     *
     * @mbggenerated
     */
    public void setStatusShow(Byte statusShow) {
        this.statusShow = statusShow;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column dispatch_plan_batch.status_notify
     *
     * @return the value of dispatch_plan_batch.status_notify
     *
     * @mbggenerated
     */
    public Byte getStatusNotify() {
        return statusNotify;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column dispatch_plan_batch.status_notify
     *
     * @param statusNotify the value for dispatch_plan_batch.status_notify
     *
     * @mbggenerated
     */
    public void setStatusNotify(Byte statusNotify) {
        this.statusNotify = statusNotify;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column dispatch_plan_batch.times
     *
     * @return the value of dispatch_plan_batch.times
     *
     * @mbggenerated
     */
    public Byte getTimes() {
        return times;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column dispatch_plan_batch.times
     *
     * @param times the value for dispatch_plan_batch.times
     *
     * @mbggenerated
     */
    public void setTimes(Byte times) {
        this.times = times;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column dispatch_plan_batch.gmt_create
     *
     * @return the value of dispatch_plan_batch.gmt_create
     *
     * @mbggenerated
     */
    public Date getGmtCreate() {
        return gmtCreate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column dispatch_plan_batch.gmt_create
     *
     * @param gmtCreate the value for dispatch_plan_batch.gmt_create
     *
     * @mbggenerated
     */
    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column dispatch_plan_batch.gmt_modified
     *
     * @return the value of dispatch_plan_batch.gmt_modified
     *
     * @mbggenerated
     */
    public Date getGmtModified() {
        return gmtModified;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column dispatch_plan_batch.gmt_modified
     *
     * @param gmtModified the value for dispatch_plan_batch.gmt_modified
     *
     * @mbggenerated
     */
    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dispatch_plan_batch
     *
     * @mbggenerated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", name=").append(name);
        sb.append(", statusShow=").append(statusShow);
        sb.append(", statusNotify=").append(statusNotify);
        sb.append(", times=").append(times);
        sb.append(", gmtCreate=").append(gmtCreate);
        sb.append(", gmtModified=").append(gmtModified);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}