package com.guiji.dispatch.state;

import java.util.List;

import com.guiji.dispatch.dao.entity.DispatchPlan;

public interface IDispatchPlanCleanHandler {
	void excute(List<DispatchPlan> dispatchPlans) throws Exception;
}
