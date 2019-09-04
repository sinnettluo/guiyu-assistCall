package com.guiji.robot.service.impl;

import java.io.File;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.guiji.robot.model.TemplateInfo;
import com.guiji.robot.model.UserResourceCacheWithVersion;
import com.guiji.utils.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.guiji.robot.constants.RobotConstants;
import com.guiji.robot.dao.entity.UserAiCfgInfo;
import com.guiji.robot.exception.AiErrorEnum;
import com.guiji.robot.exception.RobotException;
import com.guiji.robot.model.UserResourceCache;
import com.guiji.robot.service.IUserAiCfgService;
import com.guiji.robot.service.vo.AiFlowSentenceCache;
import com.guiji.robot.service.vo.AiInuseCache;
import com.guiji.robot.service.vo.CallBusiData;
import com.guiji.robot.service.vo.CallInfo;
import com.guiji.robot.service.vo.HsReplace;
import com.guiji.robot.util.DataLocalCacheUtil;
import com.guiji.robot.util.ListUtil;
import com.guiji.robot.util.ReadTxtUtil;
import com.guiji.robot.util.SystemUtil;
import com.guiji.user.dao.entity.SysUser;
import com.guiji.utils.JsonUtils;
import com.guiji.utils.RedisUtil;
import com.guiji.utils.StrUtils;

/** 
* @ClassName: AiCacheService 
* @Description: AI数据缓存服务
* @date 2018年11月19日 下午5:27:28 
* @version V1.0  
*/
@Service
public class AiCacheService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	RedisUtil redisUtil;
	@Autowired
	IUserAiCfgService iUserAiCfgService;
	@Autowired
	DataLocalCacheUtil dataLocalCacheUtil;
	@Value("${file.hushuDir}")
    private String hushuDir;	//话术模板存放目录

	/************************************用户资源begin******************************************/
	/**
	 * 根据用户id查询用户的资源缓存信息
	 * 如果用户缓存不存在，那么重新查询后设置环境
	 * @param userId
	 * @return
	 */
	public UserResourceCache getUserResource(String userId) {
		Object cacheObj = redisUtil.hget(RobotConstants.ROBOT_USER_RESOURCE, userId);
		if(cacheObj == null) {
			//重新查询
			List<UserAiCfgInfo> list = iUserAiCfgService.queryUserAiCfgListByUserId(userId);
			UserResourceCache userResourceCache = new UserResourceCache();
			int aiNum = 0;
			if(ListUtil.isNotEmpty(list)) {
				for(UserAiCfgInfo cfg : list) {
					if(RobotConstants.USER_CFG_STATUS_S == cfg.getStatus()) {
						//如果账户是正常状态
						aiNum = aiNum + cfg.getAiNum();
					}
				}
			}
			userResourceCache.setUserId(userId);
			userResourceCache.setAiNum(aiNum);
			userResourceCache.setChgStatus(RobotConstants.USER_CHG_STATUS_A); //初始化增加
			//设置按模板拆分的机器人数量
			userResourceCache.setTempAiNumMap(iUserAiCfgService.queryTemplateAi(userId));
			//放入redis
			this.putUserResource(userResourceCache);
			return userResourceCache;
		}else {
			return (UserResourceCache)cacheObj;
		}
	}
	/************************************用户资源begin******************************************/
	/**
	 * 根据用户id查询用户的资源缓存信息
	 * 如果用户缓存不存在，那么重新查询后设置环境
	 * @param userId
	 * @return
	 */
	public UserResourceCacheWithVersion getUserResourceWithVersion(String userId) {

		UserResourceCache cache = getUserResource(userId);

		UserResourceCacheWithVersion cacheWithVersion = new UserResourceCacheWithVersion();

		BeanUtil.copyProperties(cache, cacheWithVersion);

		Map<String, TemplateInfo> map = new HashMap<>();

		cache.getTempAiNumMap().forEach((k, v) -> {
			HsReplace hsReplace = aiCacheService.queyHsReplace(k);

			TemplateInfo templateInfo = new TemplateInfo();
			templateInfo.setTemplateId(k);
			templateInfo.setNum(v);
			templateInfo.setVersion(hsReplace.getVersion());

			map.put(k, templateInfo);
		});

		cacheWithVersion.setTempInfoMap(map);

		return cacheWithVersion;
	}

	@Autowired
	AiCacheService aiCacheService;

	
	/**
	 * 查询所有用户资源--只查缓存中数据
	 * @return
	 */
	public Map<String,UserResourceCache> getAllUserResources(){
		Map<Object, Object> map = redisUtil.hmget(RobotConstants.ROBOT_USER_RESOURCE);
		if(map != null) {
			Map<String,UserResourceCache> userMap = new HashMap<String,UserResourceCache>();
			for(Map.Entry<Object, Object> entry : map.entrySet()) {
				userMap.put((String)entry.getKey(), (UserResourceCache)entry.getValue());
			}
			return userMap;
		}
		return null;
	}
	
	
	/**
	 * 新增/更新一个用户的缓存资源
	 * @param userResourceCache
	 */
	public void putUserResource(UserResourceCache userResourceCache){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put(userResourceCache.getUserId(), userResourceCache);
		//提交到redis
		redisUtil.hmset(RobotConstants.ROBOT_USER_RESOURCE, map);
	}
	
	/**
	 * 删除用户资源缓存
	 * @param userId
	 */
	public void delUserResource(String userId) {
		redisUtil.hdel(RobotConstants.ROBOT_USER_RESOURCE,userId);
	}
	/************************************end******************************************/
	
	
	/************************************机器人资源池begin******************************************/
	/**
	 * 初始化AI资源池
	 * @param list
	 */
	public void initAiPool(List<AiInuseCache> list){
		if(ListUtil.isNotEmpty(list)) {
			Map<String,Object> map = new HashMap<String,Object>();
			for(AiInuseCache aiInuse : list) {
				map.put(aiInuse.getAiNo(), aiInuse);
			}
			redisUtil.hmset(RobotConstants.ROBOT_POOL_AI, map);
		}
	}
	
	/**
	 * 更新AI资源池AI数据
	 * @param aiInuse
	 * @return
	 */
	public void changeAiPool(AiInuseCache aiInuse) {
		redisUtil.hset(RobotConstants.ROBOT_POOL_AI, aiInuse.getAiNo(), aiInuse);
	}
	
	/**
	 * 清空AI资源池缓存
	 */
	public void delAiPools() {
		redisUtil.del(RobotConstants.ROBOT_POOL_AI);
	}
	
	/**
	 * 查询ai资源池中所有机器人
	 * @return
	 */
	public List<AiInuseCache> queryAiPools(){
		Map<Object,Object> allMap = redisUtil.hmget(RobotConstants.ROBOT_POOL_AI);
		if(allMap != null && !allMap.isEmpty()) {
			List<AiInuseCache> list = new ArrayList<AiInuseCache>();
			for (Map.Entry<Object,Object> aiEntry : allMap.entrySet()) { 
				AiInuseCache aiInuse = (AiInuseCache) aiEntry.getValue();
				list.add(aiInuse);
			}
			return list;
		}
		return null;
	}
	/************************************end******************************************/
	
	
	/************************************用户分配的机器人begin******************************************/
	/**
	 * 将分配好的机器人放入缓存
	 * userid{aiNo-aiInuse}
	 * @param userId
	 * @param list
	 * @return
	 */
	public void cacheUserAiAssign(String userId,List<AiInuseCache> list){
		if(StrUtils.isNotEmpty(userId) && ListUtil.isNotEmpty(list)) {
			Map<String,Object> map = new HashMap<String,Object>();
			for(AiInuseCache aiInuse : list) {
				map.put(aiInuse.getAiNo(), aiInuse);
			}
			redisUtil.hmset(RobotConstants.ROBOT_ASSIGN_AI+userId, map);
		}
	}
	
	/**
	 * 查询用户现在已分配的机器人
	 * @param userId
	 * @return
	 */
	public List<AiInuseCache> queryUserAiInUseList(String userId){
		Map<Object,Object> allMap = redisUtil.hmget(RobotConstants.ROBOT_ASSIGN_AI+userId);
		if(allMap != null && !allMap.isEmpty()) {
			List<AiInuseCache> list = new ArrayList<AiInuseCache>();
			for (Map.Entry<Object,Object> aiEntry : allMap.entrySet()) { 
				AiInuseCache aiInuse = (AiInuseCache) aiEntry.getValue();
				list.add(aiInuse);
			}
			return list;
		}
		return null;
	}
	
	
	/**
	 * 删除某个用户分配机器人缓存数据
	 * @param userId
	 */
	public void delUserAis(String userId) {
		redisUtil.del(RobotConstants.ROBOT_ASSIGN_AI+userId);
	}
	
	
	/**
	 * 删除某个用户某个机器人的缓存数据
	 * @param userId
	 */
	public void delUserAi(String userId,String aiNo) {
		redisUtil.hdel(RobotConstants.ROBOT_ASSIGN_AI+userId,aiNo);
	}
	
	/**
	 * 查询缓存中所有已分配的机器人
	 * @return
	 */
	public Map<String,List<AiInuseCache>> queryAllAiInUse(){
		Map<String,List<AiInuseCache>> allAiMap = new HashMap<String,List<AiInuseCache>>();
		//查询所有在缓存中的用户数据
		Set<String> userSet = redisUtil.getAllKeyMatch(RobotConstants.ROBOT_ASSIGN_AI);
		if(userSet != null && !userSet.isEmpty()) {
			for(String key:userSet) {
				String userId = key.substring(key.indexOf(RobotConstants.ROBOT_ASSIGN_AI)+14);	//将key中的userid截取出来
				List<AiInuseCache> list = this.queryUserAiInUseList(userId);
				allAiMap.put(userId, list);
			}
		}
		return allAiMap;
	}
	
	/**
	 * 根据用户ID和机器人编号查询某个在使用的机器人信息
	 * @param userId
	 * @param aiNo
	 * @return
	 */
	public AiInuseCache queryAiInuse(String userId,String aiNo) {
		//查询某个ai的值
		Object cacheObj = redisUtil.hget(RobotConstants.ROBOT_ASSIGN_AI+userId, aiNo);
		if(cacheObj != null) {
			return (AiInuseCache) cacheObj;
		}
		return null;
	}
	
	/**
	 * 更新缓存数据状态
	 * @param aiInuse
	 * @return
	 */
	public void changeAiInUse(AiInuseCache aiInuse) {
		redisUtil.hset(RobotConstants.ROBOT_ASSIGN_AI+aiInuse.getUserId(), aiInuse.getAiNo(), aiInuse);
	}
	/************************************end******************************************/
	
	
	/************************************话术模板begin******************************************/
	/**
	 * 查询话术模板数据
	 */
	public HsReplace queyHsReplace(String templateId){
		if(StrUtils.isNotEmpty(templateId)) {
			Object cacheObj = redisUtil.hget(RobotConstants.ROBOT_TEMPLATE_RESOURCE, templateId);
			if(cacheObj == null) {
				//重新查询
				//查询common.json
				HsReplace commonHsReplace = queryCommonJsonObj(templateId);
				//查询replace.json
				HsReplace replaceHsReplace = queryReplaceJsonObj(templateId);
				replaceHsReplace.setTemplateName(commonHsReplace.getTemplateName());
				replaceHsReplace.setAgent(commonHsReplace.isAgent());
				replaceHsReplace.setTemplateId(commonHsReplace.getTemplateId());
				replaceHsReplace.setTrade(commonHsReplace.getTrade());
				//设置打断配置opinion.json
				this.initTempFlowCfg(replaceHsReplace);
				//设置话术模板版本-老sellbot版本还是飞龙版本
				replaceHsReplace.setVersion(this.getHsVersion(templateId));
				if(RobotConstants.HS_VERSION_FL==replaceHsReplace.getVersion()) {
					//如果是飞龙版本，那么将新类型的参数替换为老sellbot的参数形式，tts合成时屏蔽新老差异
					this.unDiffTtsParams(replaceHsReplace);
				}
				//提交到redis
				Map<String,Object> map = new HashMap<String,Object>();
				map.put(templateId, replaceHsReplace);
				redisUtil.hmset(RobotConstants.ROBOT_TEMPLATE_RESOURCE, map);
				return replaceHsReplace;
			}else {
				//如果查到了，直接返回
				return (HsReplace) cacheObj;
			}
		}else {
			return null;
		}
	}
	
	/**
	 * 从缓存中将模板清除掉后重新读取
	 * @param templateId
	 */
	public void updateHsReplace(String templateId) {
		if(StrUtils.isNotEmpty(templateId)) {
			Object cacheObj = redisUtil.hget(RobotConstants.ROBOT_TEMPLATE_RESOURCE, templateId);
			if(cacheObj != null) {
				//缓存中有，需要删除掉，重新拉取
				logger.info("模板{}存在缓存中，需要更新缓存",templateId);
				//删除缓存
				redisUtil.hdel(RobotConstants.ROBOT_TEMPLATE_RESOURCE,templateId);
				//重新获取redis
				this.queyHsReplace(templateId);
				
			}else {
				logger.info("模板{}没有缓存，不需要处理",templateId);
			}
		}
	}
	/************************************end******************************************/
	
	
	/************************************消息流begin******************************************/
	/**
	 * 新增消息流数据
	 * @param userResourceCache
	 */
	public void putFlowSentence(AiFlowSentenceCache aiFlowSentenceCache){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put(String.valueOf(aiFlowSentenceCache.getTimestamp()), aiFlowSentenceCache);
		//提交到redis（通话流数据，缓存5分钟即可）
		redisUtil.hset(RobotConstants.ROBOT_SENTENCE_RESOURCE+aiFlowSentenceCache.getSeqId(), String.valueOf(aiFlowSentenceCache.getTimestamp()), aiFlowSentenceCache,5*60L);
	}
	
	/**
	 * 根据会话Id查询消息流数据
	 * @param userId
	 * @return
	 */
	public Map<Long,AiFlowSentenceCache> queryFlowSentence(String seqId){
		Map<Object,Object> allMap = redisUtil.hmget(RobotConstants.ROBOT_SENTENCE_RESOURCE+seqId);
		if(allMap != null && !allMap.isEmpty()) {
			Map<Long,AiFlowSentenceCache> rtnMap = new HashMap<Long,AiFlowSentenceCache>();
			for (Map.Entry<Object,Object> aiEntry : allMap.entrySet()) { 
				rtnMap.put(Long.valueOf((String)aiEntry.getKey()), (AiFlowSentenceCache)aiEntry.getValue());
			}
			return rtnMap;
		}
		return null;
	}
	
	/**
	 * 删除某个会话ID下的消息流
	 * @param seqId 会话id
	 */
	public void delAllSentenceBySeqId(String seqId) {
		redisUtil.del(RobotConstants.ROBOT_SENTENCE_RESOURCE+seqId);
	}
	
	/**
	 * 删除某个某个会话下某个时间点的消息流
	 * @param userId
	 */
	public void delSentence(String seqId,Long timestamp) {
		redisUtil.hdel(RobotConstants.ROBOT_SENTENCE_RESOURCE+seqId,String.valueOf(timestamp));
	}
	/************************************end******************************************/
	
	/************************************电话通话内容begin******************************************/
	/**
	 * 为用户新增一通电话
	 * userid{seqId-callInfo}
	 * @param userId
	 * @param callInfo
	 * @return
	 */
	public void cacheUserCalls(String userId,CallInfo callInfo){
		if(StrUtils.isNotEmpty(userId) && callInfo!=null) {
			callInfo.setExpire(System.currentTimeMillis());
			SysUser sysUser = dataLocalCacheUtil.queryUser(userId);
			Map<String,Object> map = new HashMap<String,Object>();
			map.put(callInfo.getSeqId(), callInfo);
			redisUtil.hmset(RobotConstants.ROBOT_USER_CALL+sysUser.getOrgCode()+"_"+userId, map, 5*60L);	//放入缓存，5分钟
		}
	}
	/**
	 * 根据用户ID和会话ID查询某通正在进行的通话
	 * @param userId
	 * @param seqId
	 * @return
	 */
	public CallInfo queryUserCall(String userId,String seqId) {
		SysUser sysUser = dataLocalCacheUtil.queryUser(userId);
		//查询某通电话
		String key = RobotConstants.ROBOT_USER_CALL+sysUser.getOrgCode()+"_"+userId;
		Object cacheObj = redisUtil.hget(key, seqId);
		if(cacheObj != null) {
			CallInfo callInfo = (CallInfo) cacheObj;
			if(System.currentTimeMillis()-callInfo.getExpire()>=5*60*1000) {
				//超时5分钟删除
				redisUtil.hdel(key,seqId);
				return null;
			}
			return callInfo;
		}
		return null;
	}
	/**
	 * 查询用户现在正在进行的通话数据
	 * @param userId
	 * @return
	 */
	public List<CallInfo> queryUserCallList(String userId){
		SysUser sysUser = dataLocalCacheUtil.queryUser(userId);
		String key = RobotConstants.ROBOT_USER_CALL+sysUser.getOrgCode()+"_"+userId;
		Map<Object,Object> allMap = redisUtil.hmget(key);
		if(allMap != null && !allMap.isEmpty()) {
			List<CallInfo> list = new ArrayList<CallInfo>();
			for (Map.Entry<Object,Object> aiEntry : allMap.entrySet()) { 
				CallInfo callInfo = (CallInfo) aiEntry.getValue();
				if(System.currentTimeMillis()-callInfo.getExpire()>=5*60*1000) {
					//超时5分钟删除
					redisUtil.hdel(key,callInfo.getSeqId());
					continue;
				}
				list.add(callInfo);
			}
			return list;
		}
		return null;
	}
	/**
	 * 查询企业下所有用户通话列表
	 * @param orgCode
	 * @return
	 */
	public Map<String,List<CallInfo>> queryCallsByOrg(String orgCode){
		Map<String,List<CallInfo>> allUserCallMap = new HashMap<String,List<CallInfo>>();
		//查询所有在缓存中的用户数据
		Set<String> userSet = redisUtil.getAllKeyMatch(RobotConstants.ROBOT_USER_CALL+orgCode+"_");
		if(userSet != null && !userSet.isEmpty()) {
			for(String key:userSet) {
				String userId = key.substring(key.lastIndexOf("_")+1);	//将key中的userid截取出来
				List<CallInfo> list = this.queryUserCallList(userId);
				allUserCallMap.put(userId, list);
			}
		}
		return allUserCallMap;
	}
	/**
	 * 删除某个用户的所有通话
	 * @param userId
	 */
	public void delUserCalls(String userId) {
		SysUser sysUser = dataLocalCacheUtil.queryUser(userId);
		redisUtil.del(RobotConstants.ROBOT_USER_CALL+sysUser.getOrgCode()+"_"+userId);
	}
	/**
	 * 删除某个用户某通电话
	 * @param userId
	 */
	public void delUserCall(String userId,String seqId) {
		SysUser sysUser = dataLocalCacheUtil.queryUser(userId);
		redisUtil.hdel(RobotConstants.ROBOT_USER_CALL+sysUser.getOrgCode()+"_"+userId,seqId);
	}
	/**************************************end****************************************/
	
	/************************************电话业务数据资源******************************************/
	/**
	 * 查询某通电话的业务数据
	 * @param phoneNo
	 * @return
	 */
	public CallBusiData queryCallBusiData(String phoneNo) {
		Object cacheObj = redisUtil.hget(RobotConstants.ROBOT_BUSI_DATA, phoneNo);
		if(cacheObj != null) {
			return (CallBusiData)cacheObj;
		}
		return null;
	}
	
	/**
	 * 新增/更新业务数据
	 * @param callBusiData
	 */
	public void putCallBusiData(CallBusiData callBusiData){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put(callBusiData.getPhoneNo(), callBusiData);
		//提交到redis
		redisUtil.hmset(RobotConstants.ROBOT_BUSI_DATA, map ,5*60L);  //缓存5分钟
 	}
	
	/**
	 * 删除电话业务数据
	 * @param userId
	 */
	public void delCallBusiData(String phoneNo) {
		redisUtil.hdel(RobotConstants.ROBOT_BUSI_DATA,phoneNo);
	}
	/************************************end******************************************/
	
	
	
	
	/**
	 * 获取该话术模板版本：1-老sellbot版本；2-飞龙版本
	 * 判断依据根据飞龙版本要求，根据scene.json文件判断，只有飞龙版本有这个文件
	 * @param templateId
	 * @return
	 */
	private int getHsVersion(String templateId) {
		String hushuDirPath = SystemUtil.getRootPath()+hushuDir + "/" + templateId + "/" + templateId + "/"; //话术模板存放目录
		if(!new File(hushuDirPath).exists()) {
			//话术模板检查
			logger.error("话术模板{}在路径{}不存在。。。",templateId,hushuDirPath);
			throw new RobotException(AiErrorEnum.AI00060026.getErrorCode(),AiErrorEnum.AI00060026.getErrorMsg());
		}
		String policyPath = hushuDirPath + "scene.json";
		if(!new File(policyPath).exists()) {
			logger.info("话术模板{},scene.json文件{}不存在，判断为老sellbot模板",templateId,policyPath);
			return RobotConstants.HS_VERSION_SB;
		}else {
			return RobotConstants.HS_VERSION_FL;
		}
	}
	
	/**
	 * 获取replace.json对象
	 * @param templateId
	 * @return
	 */
	private HsReplace queryCommonJsonObj(String templateId) {
		//获取话术模板json文件
		String replaceFilePath = this.getHsCommonJsonPath(templateId);
		if(!new File(replaceFilePath).exists()) {
			logger.info("话术模板{},common.json文件{}不存在",templateId,replaceFilePath);
			HsReplace hsReplace = new HsReplace();
			return new HsReplace();
		}
		//读取本地话术模板文件
		String json = ReadTxtUtil.readTxtFile(replaceFilePath);
		//读取json文件获取数据
		HsReplace hsReplace = JsonUtils.json2Bean(json, HsReplace.class);
		return hsReplace;
	}
	
	/**
	 * 获取replace.json对象
	 * @param templateId
	 * @return
	 */
	private HsReplace queryReplaceJsonObj(String templateId) {
		//获取话术模板json文件
		String replaceFilePath = this.getHsJsonPath(templateId);
		if(!new File(replaceFilePath).exists()) {
			logger.info("话术模板{},replace.json文件{}不存在，不需要tts合成",templateId,replaceFilePath);
			//如果replace.json不存在，那么本话术模板不需要合成TTS
			HsReplace hsReplace = new HsReplace();
			hsReplace.setTemplate_tts_flag(false);
			return hsReplace;
		}
		//读取本地话术模板文件
		String json = ReadTxtUtil.readTxtFile(replaceFilePath);
		//读取json文件获取数据
		HsReplace hsReplace = JsonUtils.json2Bean(json, HsReplace.class);
		return hsReplace;
	}
	
	/**
	 * 设置话术模板中配置的打断配置 options.json
	 * @param hsReplace
	 * @return
	 */
	private void initTempFlowCfg(HsReplace hsReplace) {
		String opinionFile = SystemUtil.getRootPath()+hushuDir + "/" + hsReplace.getTemplateId() + "/" + hsReplace.getTemplateId() + "/options.json"; //模板配置信息
		if(!new File(opinionFile).exists()) {
			logger.info("话术模板{},options.json文件{}不存在，使用默认打断参数",hsReplace.getTemplateId(),opinionFile);
		}else {
			//options.json文件存在，读取文件设置打断策略
			//读取本地话术模板文件
			String json = ReadTxtUtil.readTxtFile(opinionFile);
			if(StrUtils.isNotEmpty(json)) {
				JSONObject jsonObject;
				try {
					jsonObject = JSON.parseObject(json);
					if(jsonObject!=null) {
						//静音的次数
						int silence_wait_secs = jsonObject.getIntValue("silence_wait_secs");
						if(silence_wait_secs>0) {
							hsReplace.setSilence_wait_secs(silence_wait_secs);
						}
						//静音等待时间
						int silence_wait_time = jsonObject.getIntValue("silence_wait_time");
						if(silence_wait_time>0) {
							hsReplace.setSilence_wait_time(silence_wait_time);
						}
						//不可打断的域配置，多域 空格分隔
						String non_interruptables = jsonObject.getString("non_interruptable");
						if(StrUtils.isNotEmpty(non_interruptables)) {
							hsReplace.setNon_interruptable(non_interruptables.split(" "));
						}
						JSONObject ic = (JSONObject) jsonObject.get("interruption_config");
						if(ic!=null) {
							//打断间隔时间
							int interrupt_min_interval = ic.getIntValue("interrupt_min_interval");
							if(interrupt_min_interval>0) {
								hsReplace.setInterrupt_min_interval(interrupt_min_interval);
							}
							//打断字数
							int interrupt_words_num = ic.getIntValue("interrupt_words_num");
							if(interrupt_words_num>0) {
								hsReplace.setInterrupt_words_num(interrupt_words_num);
							}
						}
					}
				} catch (Exception e) {
					//抛出异常，但是不停止，使用默认打断配置继续
					logger.error("解析话术模板"+hsReplace.getTemplateId()+"打断配置options.json异常",e);
				}
			}
		}
	}
	
	
	/**
	 * 获取话术模板replace.json文件路径
	 * @param hushuDirPath
	 * @param ttsVoiceReq
	 * @return
	 */
	private String getHsJsonPath(String templateId) {
		String hushuDirPath = SystemUtil.getRootPath()+hushuDir + "/" + templateId + "/" + templateId + "/"; //话术模板存放目录
		if(!new File(hushuDirPath).exists()) {
			//话术模板检查
			logger.error("话术模板{}在路径{}不存在。。。",templateId,hushuDirPath);
			throw new RobotException(AiErrorEnum.AI00060026.getErrorCode(),AiErrorEnum.AI00060026.getErrorMsg());
		}
		return hushuDirPath + "replace.json";
	}
	
	
	/**
	 * 获取话术模板common.json文件路径
	 * @param hushuDirPath
	 * @param ttsVoiceReq
	 * @return
	 */
	private String getHsCommonJsonPath(String templateId) {
		String hushuDirPath = SystemUtil.getRootPath()+hushuDir + "/" + templateId + "/" + templateId + "/"; //话术模板存放目录
		if(!new File(hushuDirPath).exists()) {
			//话术模板检查
			logger.error("话术模板{}在路径{}不存在。。。",templateId,hushuDirPath);
			throw new RobotException(AiErrorEnum.AI00060026.getErrorCode(),AiErrorEnum.AI00060026.getErrorMsg());
		}
		return hushuDirPath + "common.json";
	}
	
	/**
	 * 如果是飞龙版本，tts_pos需要替换的参数类似： 
	 * "1_2":"请问您是[客户姓名]吗"
	 * 如果是sellbot版本，tts_pos需要替换的参数类似:
	 * "1_2":"请问您是$1111吗"
	 * 需要在这里将飞龙参数方式替换为老的方式，简化后续合成处理
	 * @param hsReplace
	 */
	private void unDiffTtsParams(HsReplace hsReplace) {
		Map<String,String> tts_pos = hsReplace.getTts_pos();	//待替换的文本
		Map<String,String> replace_map_relationship = hsReplace.getReplace_map_relationship();	//参数新老映射
		if(tts_pos!=null && replace_map_relationship!=null) {
			for (Map.Entry<String, String> entry : tts_pos.entrySet()) {
				Iterator<Entry<String, String>> keyIterator = replace_map_relationship.entrySet().iterator();
				String ttsPosStr = entry.getValue();	//待替换文本
				while(keyIterator.hasNext()) {
					Entry<String, String> keyNext = keyIterator.next();
					ttsPosStr = ttsPosStr.replaceAll("\\["+keyNext.getValue()+"\\]", keyNext.getKey()); //将"[客户名称]"替换为0000
				}
				entry.setValue(ttsPosStr);
			}
		}
	}
	
}
