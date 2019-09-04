package com.guiji.da.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.guiji.component.result.Result;
import com.guiji.da.constants.DaConstants;
import com.guiji.da.exception.DaErrorEnum;
import com.guiji.da.exception.DaException;
import com.guiji.da.service.robot.IRobotCallProcessStatService;
import com.guiji.da.service.robot.ISellbotCallbackService;
import com.guiji.da.service.vo.RobotCallProcessStatCache;
import com.guiji.da.service.vo.RobotCallProcessStatQueryCondition;
import com.guiji.da.service.vo.RobotCallProcessStatVO;
import com.guiji.da.service.vo.RobotCallProcessStatView;
import com.guiji.da.service.vo.RotbotFdCallback;
import com.guiji.utils.BeanUtil;
import com.guiji.utils.JsonUtils;
import com.guiji.utils.StrUtils;

/** 
* @ClassName: RobotDaController 
* @Description: 机器人量化分析统计服务
* @date 2018年12月7日 下午12:02:27 
* @version V1.0  
*/
@RestController
@RequestMapping(value = "/robot")
public class RobotDaController {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	IRobotCallProcessStatService iRobotCallProcessStatService;
	@Autowired
	ISellbotCallbackService iSellbotCallbackService;
	
	/**
	 * 接收SELLBOT通话分析回调
	 * @param sellbotJson
	 * @return
	 */
	@RequestMapping(value = "/receiveSellbotCallback", method = RequestMethod.POST)
	public Result.ReturnData receiveSellbotCallback(@RequestBody String sellbotJson){
		if(StrUtils.isNotEmpty(sellbotJson)) {
			iSellbotCallbackService.receiveSellbotCallback(sellbotJson);
			return Result.ok();
		}else {
			throw new DaException(DaErrorEnum.DA00060001.getErrorCode(),DaErrorEnum.DA00060001.getErrorMsg());
		}
	}
	
	/**
	 * 接收飞龙通话信息回调
	 * @param sellbotJson
	 * @return
	 */
	@RequestMapping(value = "/receiveFdCallback", method = RequestMethod.POST)
	public Result.ReturnData receiveFdCallback(@RequestBody String flJson){
		if(StrUtils.isNotEmpty(flJson)) {
			//飞龙返回的seqid对应我们的属性是seqId转一下,intent对应da的intentLevel
			flJson = flJson.replaceAll("seqid", "seqId");
			flJson = flJson.replaceAll("intent", "intentLevel");
			flJson = flJson.replaceAll("phone", "phoneNo");
			//飞龙给过来的数据是有下划线_的，我们把这个转为驼峰式
			flJson = BeanUtil.lineToHump(flJson);
			RotbotFdCallback rotbotFdCallback = JsonUtils.json2Bean(flJson, RotbotFdCallback.class);
			//开始处理回调数据
			iSellbotCallbackService.receiveFdCallback(rotbotFdCallback);
			return Result.ok();
		}else {
			throw new DaException(DaErrorEnum.DA00060001.getErrorCode(),DaErrorEnum.DA00060001.getErrorMsg());
		}
	}
	
	
	/**
	 * 查询robot量化分析数据
	 * @param condition
	 * @return
	 */
	@RequestMapping(value = "/queryRobotCallStat", method = RequestMethod.POST)
	public Result.ReturnData<RobotCallProcessStatView> queryRobotCallStat(
			@RequestBody RobotCallProcessStatQueryCondition condition,
			@RequestHeader String orgCode,
			@RequestHeader Boolean isSuperAdmin){
		logger.info("查询机器人通话流程分析：{}",condition);
		if(condition == null || StrUtils.isEmpty(condition.getTemplateId())) {
			throw new DaException(DaErrorEnum.DA00060001.getErrorCode(),DaErrorEnum.DA00060001.getErrorMsg());
		}
		if(StrUtils.isEmpty(condition.getUserId()) && !isSuperAdmin) {
			//如果请求参数中没有userId(没有固定按某人查询),且不是超级管理员，那就按机构查
			condition.setOrgCode(orgCode);
		}
		List<RobotCallProcessStatCache> list = iRobotCallProcessStatService.queryRobotCallProcessStatByCondition(condition);
		if(list != null && !list.isEmpty()) {
			RobotCallProcessStatView robotCallProcessStatView = new RobotCallProcessStatView();
			//主流程AI话术统计列表
			List<RobotCallProcessStatVO> majorStatList = new ArrayList<RobotCallProcessStatVO>();
			//一般AI话术统计列表
			List<RobotCallProcessStatVO> commonStatList = new ArrayList<RobotCallProcessStatVO>();
			for(RobotCallProcessStatCache cache : list) {
				RobotCallProcessStatVO vo = new RobotCallProcessStatVO();
				BeanUtil.copyProperties(cache, vo);
				//设置拒绝数、挂断数
				vo.setRefusedNum(cache.getRefusedStatMap()==null?0:(cache.getRefusedStatMap().get(DaConstants.REFUSED_YES)==null?0:cache.getRefusedStatMap().get(DaConstants.REFUSED_YES)));	//用户拒绝数
				vo.setHangupNum(cache.getHangupStatMap()==null?0:(cache.getHangupStatMap().get(DaConstants.HANGUP_YES)==null?0:cache.getHangupStatMap().get(DaConstants.HANGUP_YES))); //挂断数
				if(DaConstants.DOMAIN_TYPE_MAIN.equals(cache.getDomainType())) {
					//主流程
					majorStatList.add(vo);
				}else if(DaConstants.DOMAIN_TYPE_COMMON.equals(cache.getDomainType())) {
					//一般问题
					commonStatList.add(vo);
				}
			}
			robotCallProcessStatView.setMajorStatList(majorStatList);
			robotCallProcessStatView.setCommonStatList(commonStatList);
			return Result.ok(robotCallProcessStatView);
		}
		return Result.ok();
	}
}
