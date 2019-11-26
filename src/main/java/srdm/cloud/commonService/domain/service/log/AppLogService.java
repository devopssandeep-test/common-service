package srdm.cloud.commonService.domain.service.log;

import java.io.OutputStream;

import srdm.common.exception.SrdmDataNotFoundException;
import srdm.common.exception.SrdmGeneralException;

public interface AppLogService {

	long makeDownloadLogFile(String sessionId, String logType) throws SrdmDataNotFoundException, SrdmGeneralException;
	String getDownloadFileName(String sessionId, Long requestId) throws SrdmDataNotFoundException;
	void downloadAppLog(String sessionId, Long requestId, OutputStream os) throws SrdmGeneralException, SrdmDataNotFoundException;
}
