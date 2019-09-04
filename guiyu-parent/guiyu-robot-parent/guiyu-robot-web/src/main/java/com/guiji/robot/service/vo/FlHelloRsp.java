package com.guiji.robot.service.vo;

import lombok.Data;

/** 
* @ClassName: FlHelloRsp 
* @Description: 飞龙返回数据
*                原本返回给业务系统的数据是json，但是因为返回字段可能继续变更，所以不太方便在代码中直接操作json，还是定义下属性比较清楚
* @date 2019年3月4日 下午5:08:24 
* @version V1.0  
*/
@Data
public class FlHelloRsp {
	//会话id
	private String seqid;
	//话术模板
	private String cfg_name;
	//子话术模板
	private String sub_cfg_name;
	//当前域
	private String answered_domain;
	//意图级别
	private String accurate_intent;
	//AI回复
	private String answer;
	//客户说的话
	private String sentence;
	//原因
	private String reason;
	//命中场景或者说域
	private String scene_name;
	//是否结束（0表示正常继续，1表示会话结束，网关进行挂断操作，2表示转人工）
	private String end;
	//要播放的录音文件ID
	private String wav_filename;
	//机器人返回状态
	private String status;
	//意图
	private String intent_name;
	//客户信息
	private String userinfo;
}
