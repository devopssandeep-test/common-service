package srdm.cloud.commonService.domain.repository.rspj;

import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmGeneralException;

public interface RspjRepository {

	String getRspStatus();
	String getEnableRsp() throws SrdmDataAccessException;
	void setEnableRsp(String rspEnableStatus) throws SrdmDataAccessException, SrdmGeneralException;
	void loadNetworkSetting() throws SrdmDataAccessException;
}
