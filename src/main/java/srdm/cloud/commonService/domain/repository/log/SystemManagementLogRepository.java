package srdm.cloud.commonService.domain.repository.log;

import java.util.List;

import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.LogForView;
import srdm.cloud.commonService.domain.model.LogItem;
import srdm.cloud.commonService.domain.model.SystemManagementLog;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

public interface SystemManagementLogRepository {

	SystemManagementLog finedOne(String logId) throws SrdmDataAccessException, SrdmDataNotFoundException;
	List<LogForView> findAllWithPagable(GetListReq getListReq) throws SrdmDataAccessException;

	void add(SystemManagementLog log) throws SrdmDataAccessException;
	void deleteAll() throws SrdmDataAccessException;
	void deleteByTimestamp(long timestamp) throws SrdmDataAccessException;
	void deleteByDomainIds(List<String> domainIds) throws SrdmDataAccessException;

	Long count(GetListReq getListReq) throws SrdmDataAccessException, SrdmDataNotFoundException;

	List<String> export(long nowTime, long startIndex, long endIndex) throws SrdmDataAccessException;
}
