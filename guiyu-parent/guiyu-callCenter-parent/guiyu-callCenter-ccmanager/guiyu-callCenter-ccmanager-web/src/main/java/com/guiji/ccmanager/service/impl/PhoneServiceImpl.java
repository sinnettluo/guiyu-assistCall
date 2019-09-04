package com.guiji.ccmanager.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.guiji.callcenter.dao.CallOutPlanMapper;
import com.guiji.callcenter.dao.PhoneMapper;
import com.guiji.callcenter.dao.entity.CallOutPlan;
import com.guiji.callcenter.dao.entity.CallOutPlanExample;
import com.guiji.callcenter.dao.entity.Phone;
import com.guiji.ccmanager.service.PhoneService;
import com.guiji.ccmanager.vo.PhoneAreaVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

/**
 * author:liyang
 * Date:2019/6/28 19:22
 * Description:
 */
@Service
@Slf4j
public class PhoneServiceImpl implements PhoneService {


    @Autowired
    CallOutPlanMapper callOutPlanMapper;
    @Autowired
    PhoneMapper phoneMapper;

    @Override
    public PhoneAreaVo getPhoneBySeqId(BigInteger bigInteCallId, Integer orgId) {

        CallOutPlan realCallPlan = findByCallId(bigInteCallId, orgId);
        String phoneNum = realCallPlan.getPhoneNum();

        PhoneAreaVo phoneAreaVo = new PhoneAreaVo();
        phoneAreaVo.setPhone(phoneNum);
        phoneAreaVo.setArea(findLocationByPhone(phoneNum));

        return phoneAreaVo;
    }

    @Override
    public String findLocationByPhone(String mobile) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(mobile), "null mobile");
        Phone phone = phoneMapper.selectByPrimaryKey(mobile.substring(0,7));
        if(phone == null){
            log.warn("未跟进号码[{}]找到归属地信息", mobile);
            return null;
        }

        return phone.getProvince()+phone.getCity();
    }

    @Override
    public CallOutPlan findByCallId(BigInteger callId, Integer orgId) {
        CallOutPlanExample example = new CallOutPlanExample();
        example.createCriteria()
                .andCallIdEqualTo(callId)
                .andOrgIdEqualTo(orgId);
        List<CallOutPlan> list = callOutPlanMapper.selectByExample(example);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }
}
