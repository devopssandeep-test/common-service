package srdm.cloud.commonService.domain.service.log;

import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.GetListRes;
import srdm.cloud.commonService.domain.model.OperationLog;
import srdm.cloud.commonService.domain.model.SystemManagementLog;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

public interface LogService {

	GetListRes getOperationLogs(String sessionId, GetListReq getListReq) throws SrdmDataAccessException, SrdmDataNotFoundException;
	GetListRes getSystemLogs(String sessionId, GetListReq getListReq) throws SrdmDataAccessException, SrdmDataNotFoundException;

	OperationLog getOperationLog(String sessionId, String domainId, String logId) throws SrdmDataAccessException, SrdmDataNotFoundException;
	SystemManagementLog getSystemLog(String sessionId, String logId) throws SrdmDataAccessException, SrdmDataNotFoundException;

	void deleteOperationLog(String sessionId, String domainId) throws SrdmDataAccessException, SrdmDataNotFoundException;
	void deleteSystemLog(String sessionId) throws SrdmDataAccessException;

	void deleteOperationLogSchedule(String sessionId, long period);
	void deleteSystemLogSchedule(String sessionId, long period);
}
