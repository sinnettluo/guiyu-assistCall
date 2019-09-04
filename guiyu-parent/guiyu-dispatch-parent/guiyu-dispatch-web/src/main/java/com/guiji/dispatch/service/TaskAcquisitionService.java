package com.guiji.dispatch.service;

import com.guiji.component.result.Result;
import com.guiji.dispatch.dao.entity.TaskAcquisitionRules;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * @Classname TaskAcquisitionService
 * @Description TODO
 * @Date 2019/8/15 14:43
 * @Created by xiexiang
 */
public interface TaskAcquisitionService {

    List<TaskAcquisitionRules> getObjByOrgCode(String orgCode);

    void saveOrUpdate(Integer userId,String orgCode,TaskAcquisitionRules taskAcquisitionRules);

    Result.ReturnData<Object> oneClickPickup(Integer userId, String orgCode ,String robotname);

    Result.ReturnData<List<String>> getAvailbleTemplateIds(Integer userId);
}
