package com.guiji.botsentence.dao;

import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.guiji.botsentence.dao.entity.BotSentenceTtsBackup;
import com.guiji.botsentence.dao.entity.BotSentenceTtsBackupExample;

public interface BotSentenceTtsBackupMapper {
    int countByExample(BotSentenceTtsBackupExample example);

    int deleteByExample(BotSentenceTtsBackupExample example);

    int deleteByPrimaryKey(String backupId);

    int insert(BotSentenceTtsBackup record);

    int insertSelective(BotSentenceTtsBackup record);

    List<BotSentenceTtsBackup> selectByExample(BotSentenceTtsBackupExample example);

    BotSentenceTtsBackup selectByPrimaryKey(String backupId);

    int updateByExampleSelective(@Param("record") BotSentenceTtsBackup record, @Param("example") BotSentenceTtsBackupExample example);

    int updateByExample(@Param("record") BotSentenceTtsBackup record, @Param("example") BotSentenceTtsBackupExample example);

    int updateByPrimaryKeySelective(BotSentenceTtsBackup record);

    int updateByPrimaryKey(BotSentenceTtsBackup record);
}