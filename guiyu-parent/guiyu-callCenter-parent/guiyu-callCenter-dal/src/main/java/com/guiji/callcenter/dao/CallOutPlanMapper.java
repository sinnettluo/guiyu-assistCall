package com.guiji.callcenter.dao;

import com.guiji.callcenter.dao.entity.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import com.guiji.callcenter.dao.entityext.CallIdRecordUrl;
import com.guiji.callcenter.dao.entityext.StatAgent;
import com.guiji.callcenter.dao.entityext.StatTemp;
import org.apache.ibatis.annotations.Param;

public interface CallOutPlanMapper {
    int countByExample(CallOutPlanExample example);

    int deleteByExample(CallOutPlanExample example);

    int deleteByPrimaryKey(BigInteger callId);

    int insert(CallOutPlan record);

    int insertSelective(CallOutPlan record);

    List<CallOutPlan> selectByExample(CallOutPlanExample example);
    List<CallOutPlan> selectByExample4Encrypt(CallOutPlanExample example);
    List<BigInteger> selectCallIds4Encrypt(CallOutPlanExample example);
    List<BigInteger> selectCallPlanRecordIds4Encrypt(Map map);
    //    List<Map> selectCallPlanRecord4Encrypt(@Param(value = "idList") List<BigInteger> idList,@Param(value = "isDesensitization") Integer isDesensitization);
    int countCallRecordList(Map map);

    CallOutPlan selectByPrimaryKey(BigInteger callId);

    int updateByExampleSelective(@Param("record") CallOutPlan record, @Param("example") CallOutPlanExample example);
    int updateCallStateIntentReason(@Param("record") CallOutPlan record, @Param("example") CallOutPlanExample example);

    int updateByExample(@Param("record") CallOutPlan record, @Param("example") CallOutPlanExample example);

    int updateByPrimaryKeySelective(CallOutPlan record);

    int updateByPrimaryKey(CallOutPlan record);

    void updateNotOverWriteIntent(CallOutPlan callPlan);

    List<CallIdRecordUrl> selectCallIdRecordUrl(@Param("callId") BigInteger callId, @Param("orgIdList") List<Integer> orgIdList);
    List<CallIdRecordUrl> selectDetailIdRecordUrl(@Param("callDetailId") BigInteger callDetailId, @Param("orgIdList") List<Integer> orgIdList);
    List<CallOutRecord> getUnuploadCall(@Param("startTime") String startTime, @Param("endTime") String endTime,
                                        @Param("serverId") String serverId, @Param("orgIdList") List<Integer> orgIdList);
    List<CallOutDetailRecord> getUnuploadDetailByCallId(@Param("startTime") String startTime, @Param("endTime") String endTime,
                                                        @Param("serverId") String serverId, @Param("orgIdList") List<Integer> orgIdList);

    void updateNotOverWriteCallState(CallOutPlan callPlan);

    List<StatTemp> statTemp(@Param("agentId") Integer agentId, @Param("tempId") String tempId, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param("orgIdList") List<Integer> orgIdList);
    List<StatTemp> statBusy(@Param("agentId") Integer agentId, @Param("tempId") String tempId, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param("orgIdList") List<Integer> orgIdList);

    List<StatAgent> statNumber(@Param("agentId") Integer agentId, @Param("tempId") String tempId, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param("orgIdList") List<Integer> orgIdList);
    List<StatAgent> statConnect(@Param("agentId") Integer agentId, @Param("tempId") String tempId, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param("orgIdList") List<Integer> orgIdList);
    List<StatAgent> statTime(@Param("tempId") String tempId, @Param("startTime") String startTime, @Param("endTime") String endTime, @Param("orgIdList") List<Integer> orgIdList);
}