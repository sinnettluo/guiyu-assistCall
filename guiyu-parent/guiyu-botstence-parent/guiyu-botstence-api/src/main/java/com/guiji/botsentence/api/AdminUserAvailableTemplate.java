package com.guiji.botsentence.api;

import com.guiji.botsentence.api.entity.BotSentenceProcess;
import com.guiji.botsentence.api.entity.BotSentenceProcessVO;
import com.guiji.component.result.ServerResult;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("guiyu-botstence-web")
public interface AdminUserAvailableTemplate {

	@RequestMapping(value="/available/getAdminUserAvailableTemplate")
	public ServerResult getAdminUserAvailableTemplate(@RequestParam("userId") Long userId);

}
