package srdm.cloud.commonService.domain.service.account;

import java.util.List;

import srdm.cloud.commonService.domain.model.AccountInfo;
import srdm.cloud.commonService.domain.model.EditAccount;
import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.GetListRes;
import srdm.cloud.commonService.domain.model.LoginAccountInfo;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;
import srdm.common.exception.SrdmGeneralException;
import srdm.common.exception.SrdmParameterValidationException;

public interface AccountService {

	AccountInfo getAccount(String sessionId, String accountId) throws SrdmDataAccessException, SrdmDataNotFoundException, SrdmGeneralException;
	GetListRes getAccounts(String sessionId, GetListReq getListReq) throws SrdmDataAccessException, SrdmDataNotFoundException;

	String create(String sessionId, AccountInfo account) throws SrdmDataAccessException, SrdmGeneralException, SrdmDataNotFoundException;
	void update(String sessionId, EditAccount account, boolean editPrivateRole) throws SrdmDataAccessException, SrdmDataNotFoundException, SrdmGeneralException, SrdmParameterValidationException;
	void delete(String sessionId, List<String> accountIdList) throws SrdmDataAccessException, SrdmDataNotFoundException, SrdmGeneralException;

	LoginAccountInfo getLoginAccountInfo(String sessionId) throws SrdmDataAccessException, SrdmDataNotFoundException;

	void accountUnlock(String sessionId, List<String> accountIdList) throws SrdmDataAccessException, SrdmDataNotFoundException, SrdmGeneralException;
	void accountLock(String sessionId, List<String> accountIdList) throws SrdmDataAccessException, SrdmDataNotFoundException, SrdmGeneralException;
}
