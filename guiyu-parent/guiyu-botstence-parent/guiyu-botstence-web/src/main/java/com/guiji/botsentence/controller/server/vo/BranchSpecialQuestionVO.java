package com.guiji.botsentence.controller.server.vo;

/**
 * 一般问题的 special
 * @Description:
 * @author liyang  
 * @date 2018年8月22日  
 *
 */
public class BranchSpecialQuestionVO extends BranchNegativeVO{

	private String user_ask;
	private boolean end_to_enter;
	private boolean is_special_limit_free;
    
	public String getUser_ask() {
		return user_ask;
	}
	public void setUser_ask(String user_ask) {
		this.user_ask = user_ask;
	}
	public boolean getEnd_to_enter() {
		return end_to_enter;
	}
	public void setEnd_to_enter(boolean end_to_enter) {
		this.end_to_enter = end_to_enter;
	}
	public boolean getIs_special_limit_free() {
		return is_special_limit_free;
	}
	public void setIs_special_limit_free(boolean is_special_limit_free) {
		this.is_special_limit_free = is_special_limit_free;
	}
	
	
}
