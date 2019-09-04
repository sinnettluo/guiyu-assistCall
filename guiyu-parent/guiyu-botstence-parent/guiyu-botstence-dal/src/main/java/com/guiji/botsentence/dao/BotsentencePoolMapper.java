package com.guiji.botsentence.dao;

import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.guiji.botsentence.dao.entity.BotsentencePool;
import com.guiji.botsentence.dao.entity.BotsentencePoolExample;

public interface BotsentencePoolMapper {
    int countByExample(BotsentencePoolExample example);

    int deleteByExample(BotsentencePoolExample example);

    int deleteByPrimaryKey(String botsentenceId);

    int insert(BotsentencePool record);

    int insertSelective(BotsentencePool record);

    List<BotsentencePool> selectByExample(BotsentencePoolExample example);

    BotsentencePool selectByPrimaryKey(String botsentenceId);

    int updateByExampleSelective(@Param("record") BotsentencePool record, @Param("example") BotsentencePoolExample example);

    int updateByExample(@Param("record") BotsentencePool record, @Param("example") BotsentencePoolExample example);

    int updateByPrimaryKeySelective(BotsentencePool record);

    int updateByPrimaryKey(BotsentencePool record);
}