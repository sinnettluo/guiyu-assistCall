package com.guiji.ccmanager.service;

import com.guiji.callcenter.dao.entity.CallOutPlan;
import com.guiji.ccmanager.vo.PhoneAreaVo;

import java.math.BigInteger;

/**
 * author:liyang
 * Date:2019/6/28 19:23
 * Description:
 */
public interface PhoneService {
    PhoneAreaVo getPhoneBySeqId(BigInteger bigInteCallId, Integer orgId);

    String findLocationByPhone(String mobile);

    CallOutPlan findByCallId(BigInteger callId, Integer orgId);
}
