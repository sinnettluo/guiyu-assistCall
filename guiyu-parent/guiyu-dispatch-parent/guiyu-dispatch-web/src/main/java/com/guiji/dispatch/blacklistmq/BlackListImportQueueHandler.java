package com.guiji.dispatch.blacklistmq;

import com.guiji.dispatch.dao.entity.BlackList;

public interface BlackListImportQueueHandler {
	public void add(BlackList vo);
}
