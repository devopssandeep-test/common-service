package srdm.cloud.commonService.domain.service.log;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import srdm.cloud.commonService.domain.model.Account;
import srdm.cloud.commonService.domain.model.Domain;
import srdm.cloud.commonService.domain.model.LogItem;
import srdm.cloud.commonService.domain.model.OperationLog;
import srdm.cloud.commonService.domain.repository.account.AccountRepository;
import srdm.cloud.commonService.domain.repository.domain.DomainRepository;
import srdm.cloud.commonService.domain.repository.log.OperationLogRepository;
import srdm.cloud.commonService.domain.repository.session.SrdmSessionRepository;
import srdm.common.constant.SrdmLogConstants;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

@Service
public class OpeLogWriteServiceImpl implements OpeLogWriteService {

	private static final Logger logger = LoggerFactory.getLogger(OpeLogWriteServiceImpl.class);

	@Autowired
	OperationLogRepository operationLogRepository;

	@Autowired
	SrdmSessionRepository srdmSessionRepository;

	@Autowired
	DomainRepository domainRepository;

	@Autowired
	AccountRepository accountRepository;

	@Override
	public void writeOperationLog(String sessionId, String operation, long code, List<LogItem> itemList) {

		String accountId = srdmSessionRepository.getAccountId(sessionId);
		String domainId = srdmSessionRepository.getDomainId(sessionId);
		if(accountId.isEmpty() || domainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("accountId or domainId not found. sessionId[{}]", sessionId);
		} else {
			writeOperationLog(domainId, accountId, operation, code, itemList);
		}
	}

	@Override
	public void writeOperationLog(String domainId, String accountId, String operation, long code,
			List<LogItem> itemList) {

		OperationLog opeLog = new OperationLog();
		opeLog.setOperation(operation);
		if (code == 0) {
			opeLog.setKind(SrdmLogConstants.OPELOG_KIND_INFO);
		} else {
			opeLog.setKind(SrdmLogConstants.OPELOG_KIND_ERROR);
		}
		opeLog.setCode(String.format("%04d", code));
		opeLog.setDomainId(domainId);
		opeLog.setAccountId(accountId);
		opeLog.setItemList(itemList);

		/**
		 * targetDomainIdの設定
		 * itemList内のdomainId/parentDomainIdがある場合は、そのdomainIdをtargetDomainIdに設定。
		 * 無い場合は、メソッドのパラメータに指定されているdomainIdを設定。
		 * 注）SRDM2.4.0では、この判断で問題ないが、今後変更が必要となる可能性あり。
		 */
		String targetDomainId = domainId;
		for(LogItem item : itemList) {
			if(item.getName().equals(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_ID) == true) {
				targetDomainId = item.getValue();
				break;
			} else if(item.getName().equals(SrdmLogConstants.OPELOG_ITEM_NAME_PARENT_DOMAIN_ID) == true) {
				targetDomainId = item.getValue();
				break;
			}
		}
		opeLog.setTargetDomainId(targetDomainId);

		// domainName取得
		try {
			Domain domain = domainRepository.findOne(domainId);
			opeLog.setDomainName(domain.getDomainName());
		} catch (SrdmDataNotFoundException | SrdmDataAccessException e) {
			logger.warn("domain not found. domainId[{}]", domainId, e);
			opeLog.setDomainName(SrdmLogConstants.DOMAIN_NAME_NONE);
		}

		// accountName取得
		try {
			Account account = accountRepository.findOne(accountId);
			opeLog.setAccountName(account.getAccountName());
		} catch (SrdmDataAccessException | SrdmDataNotFoundException e) {
			logger.warn("account not found. accountId[{}]", accountId, e);
			opeLog.setAccountName(SrdmLogConstants.ACCOUNT_NAME_NONE);
		}

		try {
			operationLogRepository.add(opeLog);
		} catch (SrdmDataAccessException e) {
			logger.error("writeOperationLog(): operation=[{}] code=[{}]", operation, code, e);
		}
	}

}
