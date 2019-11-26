package srdm.cloud.commonService.domain.repository.log;

import java.util.List;

import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.LogForView;
import srdm.cloud.commonService.domain.model.LogItem;
import srdm.cloud.commonService.domain.model.OperationLog;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

public interface OperationLogRepository {

	OperationLog finedOne(List<String> domainIdList, String logId) throws SrdmDataAccessException, SrdmDataNotFoundException;
	List<LogForView> findAllWithPagable(GetListReq getListReq) throws SrdmDataAccessException;

	void add(OperationLog log) throws SrdmDataAccessException;
	/**update and addItem methods are not used.Hence commented.
	 */
	//void update(OperationLog log) throws SrdmDataAccessException;
	//void addItem(String logId, List<LogItem> itemList) throws SrdmDataAccessException;
	void deleteByDomainIds(List<String> domainIds) throws SrdmDataAccessException;
	void deleteByTimestamp(long timestamp) throws SrdmDataAccessException;

	Long count(GetListReq getListReq) throws SrdmDataAccessException, SrdmDataNotFoundException;

	List<String> export(List<String> domainIdList, long nowTime, long startIndex, long endIndex) throws SrdmDataAccessException;
}
