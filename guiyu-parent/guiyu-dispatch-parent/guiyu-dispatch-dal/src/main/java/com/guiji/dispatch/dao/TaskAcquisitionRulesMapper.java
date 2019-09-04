package com.guiji.dispatch.dao;

import com.guiji.dispatch.dao.entity.TaskAcquisitionRules;
import com.guiji.dispatch.dao.entity.TaskAcquisitionRulesExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskAcquisitionRulesMapper {
    int countByExample(TaskAcquisitionRulesExample example);

    int deleteByExample(TaskAcquisitionRulesExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(TaskAcquisitionRules record);

    int insertSelective(TaskAcquisitionRules record);

    List<TaskAcquisitionRules> selectByExample(TaskAcquisitionRulesExample example);

    TaskAcquisitionRules selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") TaskAcquisitionRules record, @Param("example") TaskAcquisitionRulesExample example);

    int updateByExample(@Param("record") TaskAcquisitionRules record, @Param("example") TaskAcquisitionRulesExample example);

    int updateByPrimaryKeySelective(TaskAcquisitionRules record);

    int updateByPrimaryKey(TaskAcquisitionRules record);
}