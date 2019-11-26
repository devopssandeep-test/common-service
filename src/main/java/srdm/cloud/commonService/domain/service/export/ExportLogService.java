package srdm.cloud.commonService.domain.service.export;

import java.io.IOException;
import java.io.OutputStream;

import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;
import srdm.common.exception.SrdmGeneralException;

public interface ExportLogService {

	long exportOperationLog(String sessionId, String domainId, String format) throws SrdmDataAccessException, SrdmDataNotFoundException;
	long exportSystemLog(String sessionId, String format) throws SrdmDataAccessException;
	String getDownloadFileName(String sessionId, Long requestId) throws SrdmDataNotFoundException;
	void downloadExportLog(String sessionId, Long requestId, OutputStream os) throws SrdmGeneralException, SrdmDataNotFoundException, SrdmDataAccessException, IOException;
}
