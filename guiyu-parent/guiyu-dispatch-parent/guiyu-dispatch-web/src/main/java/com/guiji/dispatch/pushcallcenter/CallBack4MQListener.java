package com.guiji.dispatch.pushcallcenter;

import com.guiji.dispatch.bean.MQSuccPhoneDto;
import com.guiji.dispatch.constant.RedisConstant;
import com.guiji.dispatch.dao.DispatchPlanMapper;
import com.guiji.dispatch.dao.PushRecordsMapper;
import com.guiji.dispatch.dao.entity.DispatchPlan;
import com.guiji.dispatch.dao.entity.PushRecords;
import com.guiji.dispatch.dao.entity.PushRecordsExample;
import com.guiji.dispatch.enums.GateWayLineStatusEnum;
import com.guiji.dispatch.service.GetApiService;
import com.guiji.dispatch.util.Constant;
import com.guiji.dispatch.util.DateTimeUtils;
import com.guiji.dispatch.vo.GateWayLineOccupyVo;
import com.guiji.utils.JsonUtils;
import com.guiji.utils.RedisUtil;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * 呼叫中心回调记录
 * 
 * @author Administrator
 *
 */
@Component
@RabbitListener(queues = "dispatch.CallBackEvent")
public class CallBack4MQListener {

	private Logger logger = LoggerFactory.getLogger(CallBack4MQListener.class);

	@Autowired
	private PushRecordsMapper recordMapper;
	@Autowired
	private RedisUtil redisUtil;

	@RabbitHandler
	public void process(String message, Channel channel, Message message2) {
		if(null == message || "".equals(message)){
			return;
		}
		// 呼叫中心回调之后去获取最新的并发数量和呼叫中心的负载情况推送对应数量的号码
		MQSuccPhoneDto mqSuccPhoneDto = JsonUtils.json2Bean(message, MQSuccPhoneDto.class);
		if(null == mqSuccPhoneDto){
			return;
		}
		String time = DateTimeUtils.getCurrentDateString(DateTimeUtils.DEFAULT_DATE_FORMAT_PATTERN_FULL);
		logger.info("呼叫中心回调数据:{},回调时间:{}", message, time);
		logger.warn("呼叫中心回调数据，号码:{}, 时间:{}, 模块:{}, 操作:{}, 内容:{}", mqSuccPhoneDto.getPlanuuid(), time,
				"guiyu_dispatch", "呼叫中心回调CallBack4MQListener.process", message);
		/********判断处理网关SIM卡线路占用释放	begin*************************************/
		this.leisureGateWayLine(mqSuccPhoneDto);
		/********判断处理网关SIM卡线路是否可用,不可用则重新推入队列	begin*****************/
		String planUuid = mqSuccPhoneDto.getPlanuuid();
		boolean bool = true;
		//SIM线路拨打限制
		if(null != mqSuccPhoneDto.getSimLimitFlag() && !mqSuccPhoneDto.getSimLimitFlag()) {
			this.checkSimLineLimit(mqSuccPhoneDto);//限制拨打判断是否放回拨打队列
			bool = false;
		}else{
			redisUtil.del(RedisConstant.RedisConstantKey.SIM_LINE_LIMIT + planUuid);//删除SIM限制拨打key
		}

		//判断线路是否可用、SIM卡线路不可用
		if(bool && null != mqSuccPhoneDto.getSimLineIsOk() && !mqSuccPhoneDto.getSimLineIsOk()) {
			this.checkSimLineDisabled(mqSuccPhoneDto);//线路不可用，则放回队列
			bool = false;
		}

		//正常流程
		if(bool){
			PushRecordsExample ex = new PushRecordsExample();
			ex.createCriteria().andPlanuuidEqualTo(mqSuccPhoneDto.getPlanuuid())
					.andCallbackStatusEqualTo(Constant.NOCALLBACK);
			PushRecords re = new PushRecords();
			// 设置已经回调的状态
			re.setCallbackStatus(Constant.CALLBACKED);
			recordMapper.updateByExampleSelective(re, ex);
		}
	}

	@Autowired
	GetApiService getApiService;

	@Autowired
	DispatchPlanMapper planMapper;

	/**
	 * 判断限制拨打，重新放入队列中
	 * @param mqSuccPhoneDto
	 */
	private void checkSimLineLimit(MQSuccPhoneDto mqSuccPhoneDto){
		try{
			//从redis中获取之前从拨打队列获取的任务数据
			String planUuid = mqSuccPhoneDto.getPlanuuid();
			String lineLimitKey = RedisConstant.RedisConstantKey.SIM_LINE_LIMIT + planUuid;
			Object obj = redisUtil.get(lineLimitKey);
		//	logger.info("lineLimitKey:{},限制拨打数据:{}", lineLimitKey, obj);
			if (null != obj) {
		//		logger.info("lineLimitKey:{},限制拨打数据:{}", lineLimitKey, JsonUtils.bean2Json(obj));
				//队列
				String planQueue = RedisConstant.RedisConstantKey.REDIS_PLAN_QUEUE_USER_LINE_ROBOT
						+ mqSuccPhoneDto.getUserId() + "_" + mqSuccPhoneDto.getTempId();
				DispatchPlan dispatchRedis = (DispatchPlan) obj;
				//拨打限制，重新推入队列
            //    redisUtil.del(lineLimitKey);
				boolean bool = redisUtil.leftPush(planQueue, dispatchRedis);
		//		logger.info("lineLimitKey:{},重新推入队列{}", lineLimitKey, bool?"成功":"失败");
			}
		}catch(Exception e){
			logger.error("判断SIM卡线路是否限制拨打,重新推入推列异常", e);
		}
	}

	/**
	 * 判断处理网关SIM卡线路是否可用,不可用则重新推入队列
	 * @param mqSuccPhoneDto
	 */
	private void checkSimLineDisabled(MQSuccPhoneDto mqSuccPhoneDto){
		try {
			//判断SIM卡线路是否可用
			if (null != mqSuccPhoneDto
					&& null != mqSuccPhoneDto.getSimLineIsOk() && !mqSuccPhoneDto.getSimLineIsOk()) {
				//从redis中获取之前从队列获取的任务数据
				String planUuid = mqSuccPhoneDto.getPlanuuid();
				String lineDisabledKey = RedisConstant.RedisConstantKey.LINE_DISABLED + planUuid;
				Object obj = redisUtil.get(lineDisabledKey);
				if (null != obj) {
					//队列
					String planQueue = RedisConstant.RedisConstantKey.REDIS_PLAN_QUEUE_USER_LINE_ROBOT
							+ mqSuccPhoneDto.getUserId() + "_" + mqSuccPhoneDto.getTempId();
					DispatchPlan dispatchRedis = (DispatchPlan) obj;
					//不可用，重新推入队列
                    redisUtil.del(lineDisabledKey);
					boolean bool = redisUtil.leftPush(planQueue, dispatchRedis);
				}
			}
		}catch(Exception e){
			logger.error("判断SIM卡线路是否可用,重新推入推列异常", e);
		}
	}

	/**
	 * 处理网关线路释放
	 * @param mqSuccPhoneDto
	 */
	private void leisureGateWayLine(MQSuccPhoneDto mqSuccPhoneDto){
		try {
			if (null != mqSuccPhoneDto) {
				//获取网关线路对象
				Integer lineId = mqSuccPhoneDto.getLineId();
			//	logger.info("释放网关线路ID:{}", lineId);
				if(null != lineId) {

                    if(redisUtil.get(RedisConstant.RedisConstantKey.gatewayLineKeyTmp+lineId) != null)
                    {
                        redisUtil.set(RedisConstant.RedisConstantKey.gatewayLineKeyTmp+lineId, 0, 10);
                    }

					String gateWayLineKey = RedisConstant.RedisConstantKey.gatewayLineKey + lineId;
					GateWayLineOccupyVo gateWayLine = (GateWayLineOccupyVo) redisUtil.get(gateWayLineKey);
			//		logger.info("释放网关线路ID:{},对象:{}", lineId, JsonUtils.bean2Json(gateWayLine));
					if (null != gateWayLine) {
							//释放网关路线
							this.releaseGateWay(gateWayLine, gateWayLineKey);
					}

				}else{
					//如果返回lineId，则模糊匹配查询出所有网关路线
					Set<String> gatewayLineKeySet = redisUtil.getAllKeyMatch(RedisConstant.RedisConstantKey.gatewayLineKey);
					if(null != gatewayLineKeySet){
						Iterator<String> iter =gatewayLineKeySet.iterator();
						while(iter.hasNext()){
							//获取网关线路
							String gateWayLineKey = iter.next();
							GateWayLineOccupyVo gateWayLine = (GateWayLineOccupyVo)redisUtil.get(gateWayLineKey);
								//释放网关路线
								this.releaseGateWay(gateWayLine, gateWayLineKey);
						}
					}
				}
			}
		}catch(Exception e){
			logger.error("处理网关线路占用释放异常:" + ((null != mqSuccPhoneDto)?JsonUtils.bean2Json(mqSuccPhoneDto):null), e);
		}

	}

	/**
	 * 释放网关路线占用
	 * @param gateWayLine
	 * @param gateWayLineKey
	 */
	private void releaseGateWay(GateWayLineOccupyVo gateWayLine, String gateWayLineKey){
		//释放网关线路状态
		gateWayLine.setStatus(GateWayLineStatusEnum.LEISURE.getState());
		gateWayLine.setReleaseTime(DateTimeUtils.getDateString(new Date(),DateTimeUtils.DEFAULT_DATE_FORMAT_PATTERN_FULL));
		redisUtil.set(gateWayLineKey, gateWayLine);
	}
}
