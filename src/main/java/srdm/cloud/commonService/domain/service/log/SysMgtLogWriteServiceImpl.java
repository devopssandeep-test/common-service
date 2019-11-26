package srdm.cloud.commonService.domain.service.log;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import srdm.cloud.commonService.domain.model.Account;
import srdm.cloud.commonService.domain.model.Domain;
import srdm.cloud.commonService.domain.model.LogItem;
import srdm.cloud.commonService.domain.model.SystemManagementLog;
import srdm.cloud.commonService.domain.repository.account.AccountRepository;
import srdm.cloud.commonService.domain.repository.domain.DomainRepository;
import srdm.cloud.commonService.domain.repository.log.SystemManagementLogRepository;
import srdm.cloud.commonService.domain.repository.session.SrdmSessionRepository;
import srdm.common.constant.SrdmLogConstants;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

@Service
public class SysMgtLogWriteServiceImpl implements SysMgtLogWriteService {

	private static final Logger logger = LoggerFactory.getLogger(SysMgtLogWriteServiceImpl.class);

	@Autowired
	SrdmSessionRepository srdmSessionRepository;

	@Autowired
	SystemManagementLogRepository systemManagementLogRepository;

	@Autowired
	DomainRepository domainRepository;

	@Autowired
	AccountRepository accountRepository;

	@Override
	public void writeSystemManagementLog(String sessionId, String operation, long code) {

		List<LogItem> itemList = new ArrayList<LogItem>();
		writeSystemManagementLog(sessionId, operation, code, itemList);
	}

	@Override
	public void writeSystemManagementLog(String sessionId, String operation, long code, List<LogItem> itemList) {

		String accountId = srdmSessionRepository.getAccountId(sessionId);
		String domainId = srdmSessionRepository.getDomainId(sessionId);
		if(accountId.isEmpty() || domainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("accountId or domainId not found. sessionId[{}]", sessionId);
		}

		SystemManagementLog sysLog = new SystemManagementLog();
		sysLog.setOperation(operation);
		if (code == 0) {
			sysLog.setKind(SrdmLogConstants.SYSMGT_KIND_INFO);
		} else {
			sysLog.setKind(SrdmLogConstants.SYSMGT_KIND_ERROR);
		}
		sysLog.setCode(String.format("%04d", code));
		sysLog.setDomainId(domainId);
		sysLog.setAccountId(accountId);
		sysLog.setItemList(itemList);

		// domainName取得
		try {
			Domain domain = domainRepository.findOne(domainId);
			sysLog.setDomainName(domain.getDomainName());
		} catch (SrdmDataNotFoundException | SrdmDataAccessException e) {
			logger.warn("domain not found. domainId[{}]", domainId, e);
			sysLog.setDomainName(SrdmLogConstants.DOMAIN_NAME_NONE);
		}

		// accountName取得
		try {
			Account account = accountRepository.findOne(accountId);
			sysLog.setAccountName(account.getAccountName());
		} catch (SrdmDataAccessException | SrdmDataNotFoundException e) {
			logger.warn("account not found. accountId[{}]", accountId, e);
			sysLog.setAccountName(SrdmLogConstants.ACCOUNT_NAME_NONE);
		}

		try {
			systemManagementLogRepository.add(sysLog);
		} catch (SrdmDataAccessException e) {
			logger.error("writeOperationLog(): operation=[{}] code=[{}]", operation, code, e);
		}
	}

}
