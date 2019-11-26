package srdm.cloud.commonService.domain.service.setting;

import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmGeneralException;

public interface RspjService {

	String getRspStatus(String sessionId);
	String getEnableRsp(String sessionId) throws SrdmDataAccessException;
	void setEnableRsp(String sessionId, String rspEnableStatus) throws SrdmDataAccessException, SrdmGeneralException;
}
