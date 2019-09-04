package com.guiji.dispatch.impl;

import com.guiji.component.result.Result;
import com.guiji.dispatch.dao.DispatchPlanMapper;
import com.guiji.dispatch.dao.entity.DispatchPlanExample;
import com.guiji.dispatch.service.AssistStatService;
import com.guiji.robot.service.vo.AssistStatResp;
import com.guiji.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author Zhouzl
 * @date 2019年08月19日
 * @since 1.0
 */
@Service
public class AssistStatServiceImpl implements AssistStatService {
    @Autowired
    private DispatchPlanMapper dispatchPlanMapper;

    @Override
    public Result.ReturnData<AssistStatResp> stat(Long userId, Integer orgId) {
        Date now = DateUtil.getNowDate();
        Date monthStart = DateUtil.getStartDayOfMonth(now);
        Date dayStart = DateUtil.getTodayZeroHour();
        DispatchPlanExample example = new DispatchPlanExample();
        example.createCriteria().andGmtCreateBetween(monthStart, now).andSeatIdEqualTo(userId.intValue())
                .andStatusPlanEqualTo(2).andOrgIdEqualTo(orgId);
        int monthCount = dispatchPlanMapper.countByExample(example);
        example = new DispatchPlanExample();
        example.createCriteria().andGmtCreateBetween(dayStart, now).andSeatIdEqualTo(userId.intValue())
                .andStatusPlanEqualTo(2).andOrgIdEqualTo(orgId);
        int dayCount = dispatchPlanMapper.countByExample(example);
        return Result.ok(new AssistStatResp(monthCount, dayCount));
    }
}
