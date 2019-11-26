package srdm.cloud.commonService.domain.service.log;

import java.util.List;

import srdm.cloud.commonService.domain.model.LogItem;

public interface OpeLogWriteService {

	void writeOperationLog(final String sessionId, final String operation, final long code, final List<LogItem> itemList);
	void writeOperationLog(final String domainId, final String accountId, final String operation, final long code, final List<LogItem> itemList);
}
