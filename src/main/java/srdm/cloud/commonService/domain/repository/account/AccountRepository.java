package srdm.cloud.commonService.domain.repository.account;

import java.util.List;

import srdm.cloud.commonService.domain.model.Account;
import srdm.cloud.commonService.domain.model.EditAccount;
import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.SimpleAccount;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

public interface AccountRepository {

	Account findOne(String accountId) throws SrdmDataAccessException, SrdmDataNotFoundException;
	Account findOneByName(String domainId, String accountName) throws SrdmDataNotFoundException, SrdmDataAccessException;
	List<SimpleAccount> findAllWithPagable(GetListReq getListReq) throws SrdmDataAccessException;
	List<Account> findAllByDomainId(String domainId) throws SrdmDataAccessException;

	String count(GetListReq getListReq) throws SrdmDataNotFoundException, SrdmDataAccessException;

	String create(Account account) throws SrdmDataAccessException;
	void update(EditAccount account) throws SrdmDataAccessException;
	void delete(List<String> accountIdList) throws SrdmDataAccessException;
	void updateAccountStatusToDeleted(List<String> accountIdList) throws SrdmDataAccessException;
	void clearRoleId(String domainId, List<String> roleIdList) throws SrdmDataAccessException;
	List<Account> findAllByRoleId(String roleId) throws SrdmDataAccessException;
	void deleteByDomainId(List<String> domainIdList) throws SrdmDataAccessException;

	boolean isExist(String accountId) throws SrdmDataAccessException;
	boolean isExistAccoutName(String domainId, String accountName, String excludeAccountId) throws SrdmDataAccessException;

	boolean checkAuth(String accountId, String rawPassword) throws SrdmDataNotFoundException, SrdmDataAccessException;
	void setAuthErrorInfo(String accountId) throws SrdmDataAccessException;
	void updateLatestErrorTimestamp(String accountId) throws SrdmDataAccessException;
	void clearAuthErrorInfo(List<String> accountIdList) throws SrdmDataAccessException;
	void updateAccountStatusToManualLock(List<String> accountIdList) throws SrdmDataAccessException;
	void clearManualLock(List<String> accountIdList) throws SrdmDataAccessException;
	
}

