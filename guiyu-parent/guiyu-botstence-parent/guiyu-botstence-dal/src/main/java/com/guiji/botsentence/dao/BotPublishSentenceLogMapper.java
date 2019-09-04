package com.guiji.botsentence.dao;

import com.guiji.botsentence.dao.entity.BotAvailableTemplate;
import com.guiji.botsentence.dao.entity.BotPublishSentenceLog;
import com.guiji.botsentence.dao.entity.BotPublishSentenceLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BotPublishSentenceLogMapper {
    int countByExample(BotPublishSentenceLogExample example);

    int deleteByExample(BotPublishSentenceLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(BotPublishSentenceLog record);

    int insertSelective(BotPublishSentenceLog record);

    List<BotPublishSentenceLog> selectByExample(BotPublishSentenceLogExample example);

    BotPublishSentenceLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") BotPublishSentenceLog record, @Param("example") BotPublishSentenceLogExample example);

    int updateByExample(@Param("record") BotPublishSentenceLog record, @Param("example") BotPublishSentenceLogExample example);

    int updateByPrimaryKeySelective(BotPublishSentenceLog record);

    int updateByPrimaryKey(BotPublishSentenceLog record);
    
    Long getLastPublishSentence(String tempId);
    
    int insertAvailableTemplate(BotAvailableTemplate template);
    
    void deleteAvailableTemplate(BotAvailableTemplate template);
}