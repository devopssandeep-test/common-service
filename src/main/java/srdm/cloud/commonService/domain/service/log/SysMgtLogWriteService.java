package srdm.cloud.commonService.domain.service.log;

import java.util.List;

import srdm.cloud.commonService.domain.model.LogItem;

public interface SysMgtLogWriteService {

	void writeSystemManagementLog(final String sessionId, final String operation, final long code);
	void writeSystemManagementLog(final String sessionId, final String operation, final long code, final List<LogItem> itemList);
}
