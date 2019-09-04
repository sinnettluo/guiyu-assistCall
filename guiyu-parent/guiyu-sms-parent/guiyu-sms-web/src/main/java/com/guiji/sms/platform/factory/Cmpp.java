package com.guiji.sms.platform.factory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.IOUtils;
import com.guiji.sms.common.ExceptionEnum;
import com.guiji.sms.common.SmsException;
import com.guiji.sms.dao.entity.SmsSendDetail;
import com.guiji.sms.platform.ISendMessage;
import com.guiji.sms.queue.SendDetailQueue;
import com.guiji.sms.utils.SetDetailParamsUtil;

/**
 * 短信平台-Cmpp
 */
@Component
public class Cmpp implements ISendMessage
{
	private static final Logger log = LoggerFactory.getLogger(Cmpp.class);
	
	@SuppressWarnings("rawtypes")
	@Override
	public void sendMessage(JSONObject params, List<String> phoneList)
	{
		try{
			ExecutorService executorService = Executors.newFixedThreadPool(5);
			CompletableFuture[] cfs = phoneList.parallelStream().map( phone -> 
					CompletableFuture.supplyAsync( () -> 
							send(params, phone), executorService) // 多线程执行
					.whenComplete((record,exception) -> {
						SendDetailQueue.add(record);  // 结果回调
					})).toArray(CompletableFuture[]::new);
			CompletableFuture.allOf(cfs).join(); // 等待所有子线程执行完毕
		} catch (Exception e){
			log.error(e.getMessage());
			throw new SmsException(ExceptionEnum.ERROR_REQUEST_SMS);
		}
	}

	private SmsSendDetail send(JSONObject params, String phone)
	{
		SmsSendDetail record = null;
		String cmppServiceUrl = params.getString("cmppServiceUrl");
		JSONObject json = new JSONObject();
		json.put("id", params.getString("id"));
		json.put("phone", phone);
		json.put("msgContent", params.getString("smsContent"));
		json.put("companyCode", params.getString("companyCode"));
		json.put("spCode", params.getString("spCode"));
		String result = doPost(cmppServiceUrl,json.toJSONString()); // 发送请求
		record = handleResult(result, params); // 处理结果
		record.setPhone(phone);
		return record;
	}

	// 发送请求
	private String doPost(String url, String jsonStr)
	{
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		String result = "";
		try {
			HttpPost httpPost = new HttpPost(url);
			StringEntity entity = new StringEntity(jsonStr, ContentType.APPLICATION_JSON);
			httpPost.setEntity(entity);
			response = httpClient.execute(httpPost); // 执行请求
			HttpEntity responseEntity = response.getEntity();
			result = EntityUtils.toString(responseEntity, "utf-8");
			EntityUtils.consume(responseEntity);
		} 
		catch (Exception e){
			log.error("调用短信平台商服务异常", e);
			result = "{\"code\":\"404\",\"msg\":\"调用短信平台商服务异常\"}";
		}
		finally {
			IOUtils.close(response);
			IOUtils.close(httpClient);
		}
		return result;
	}

	// 处理结果
	private SmsSendDetail handleResult(String result, JSONObject params)
	{
		SmsSendDetail record = new SmsSendDetail();
		JSONObject returnData = JSONObject.parseObject(result);
		// 返回参数
		String code = returnData.getString("code");
		String msg = returnData.getString("msg");
		String data = returnData.getString("data");
		if("0".equals(code)){
			log.info("发送成功:code:{},msg:{},data:{}", code, msg, data);
			record.setSendStatus(1);
		}else{
			log.info("发送失败:code:{},msg:{},data:{}", code, msg, data);
			record.setSendStatus(0);
			record.setFailReason(msg);
		}
		SetDetailParamsUtil.setParams(record,params); // 设置结果（发送详情）参数
		return record;
	}
	
}
