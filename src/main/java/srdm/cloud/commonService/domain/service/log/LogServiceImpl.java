package srdm.cloud.commonService.domain.service.log;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import srdm.cloud.commonService.asynchronous.AsyncDeleteLog;
import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.GetListRes;
import srdm.cloud.commonService.domain.model.LogForView;
import srdm.cloud.commonService.domain.model.OperationLog;
import srdm.cloud.commonService.domain.model.SystemManagementLog;
import srdm.cloud.commonService.domain.repository.domain.DomainRepository;
import srdm.cloud.commonService.domain.repository.log.OperationLogRepository;
import srdm.cloud.commonService.domain.repository.log.SystemManagementLogRepository;
import srdm.cloud.commonService.domain.repository.session.SrdmSessionRepository;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

@Service
public class LogServiceImpl implements LogService {

	private static final Logger logger = LoggerFactory.getLogger(LogServiceImpl.class);

	@Autowired
	AsyncDeleteLog asyncDeleteLog;

	@Autowired
	DomainRepository domainRepository;

	@Autowired
	OperationLogRepository operationLogRepository;

	@Autowired
	SystemManagementLogRepository systemManagementLogRepository;

	@Autowired
	SrdmSessionRepository srdmSessionRepository;

	/**
	 * 操作ログの取得（複数）
	 */
	@Override
	public GetListRes getOperationLogs(String sessionId, GetListReq getListReq)
			throws SrdmDataAccessException, SrdmDataNotFoundException {

		GetListRes getListRes = new GetListRes();

		// 指定ドメインが配下にあるのかのチェック
		String loginDomainId = srdmSessionRepository.getDomainId(sessionId);
		if(loginDomainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "Unable to get the operation log.");
		}
		boolean isUnderDomain = domainRepository.isUnderDomain(loginDomainId, getListReq.getKeyMap().get("domainId"));
		if(isUnderDomain == false) {
			logger.warn("domain is not included. domainId[{}]", getListReq.getKeyMap().get("domainId"));
			throw new SrdmDataNotFoundException("domainId", "", "Unable to get the operation log.");
		}

		// 指定されたドメイン配下のdomainIdを取得
		List<String> domainIdList = domainRepository.findUnderDomainId(getListReq.getKeyMap().get("domainId"));
		domainIdList.add(getListReq.getKeyMap().get("domainId"));
		domainIdList = domainIdList.stream().distinct().collect(Collectors.toList());
		getListReq.getKeyMap().remove("domainId");
		getListReq.getKeyListMap().put("domainIdList", domainIdList);

		// 指定データ取得
		List<LogForView> logList;
		logList = operationLogRepository.findAllWithPagable(getListReq);
		getListRes.setList(logList);
		getListRes.setResultCount(logList.size());

		// Total件数取得
		Long total;
		try {
			total = operationLogRepository.count(getListReq);
		} catch (SrdmDataNotFoundException e) {
			// Total件数 0件
			total = 0L;
		}
		getListRes.setTotalCount(total);

		return getListRes;
	}

	/**
	 * システム管理ログの取得（複数）
	 * システム管理ログは、全件対象。（domain/account等でのフィルタリング無し）
	 */
	@Override
	public GetListRes getSystemLogs(String sessionId, GetListReq getListReq)
			throws SrdmDataAccessException, SrdmDataNotFoundException {

		GetListRes getListRes = new GetListRes();

		// 指定データ取得
		List<LogForView> logList;
		logList = systemManagementLogRepository.findAllWithPagable(getListReq);
		getListRes.setList(logList);
		getListRes.setResultCount(logList.size());

		// Total件数取得
		Long total;
		try {
			total = systemManagementLogRepository.count(getListReq);
		} catch (SrdmDataNotFoundException e) {
			// Total件数 0件
			total = 0L;
		}
		getListRes.setTotalCount(total);

		return getListRes;
	}

	/**
	 * 操作ログ削除（指定ドメインのデータを全件）
	 */
	@Override
	public void deleteOperationLog(String sessionId, String domainId)
			throws SrdmDataAccessException, SrdmDataNotFoundException {

		// 指定ドメインが配下にあるのかのチェック
		String loginDomainId = srdmSessionRepository.getDomainId(sessionId);
		if(loginDomainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "Unable to delete operation log.");
		}
		boolean isUnderDomain = domainRepository.isUnderDomain(loginDomainId, domainId);
		if(isUnderDomain == false) {
			logger.warn("domain is not included. domainId[{}]", domainId);
			throw new SrdmDataNotFoundException("domainId", "", "Unable to delete operation log.");
		}

		// 指定されたドメイン配下のdomainIdを取得
		List<String> domainIdList = domainRepository.findUnderDomainId(domainId);
		domainIdList.add(domainId);
		domainIdList = domainIdList.stream().distinct().collect(Collectors.toList());

		operationLogRepository.deleteByDomainIds(domainIdList);

		/**
		 * ログ削除の操作ログは、残さないことに決定。
		 * 代わりにシステムログ（commonServiceのログ）に残す。
		 */
		{
			String accountId = srdmSessionRepository.getAccountId(sessionId);
			logger.info("== Operation Log deleted. domainId[{}], accountId[{}] ==", loginDomainId, accountId);
		}
	}

	/**
	 * システム管理ログ削除（全件）
	 */
	@Override
	public void deleteSystemLog(String sessionId) throws SrdmDataAccessException {

		systemManagementLogRepository.deleteAll();

		/**
		 * システム管理ログ削除のログは、残さないことに決定。
		 * 代わりにシステムログ（commonServiceのログ）に残す。
		 */
		{
			String accountId = srdmSessionRepository.getAccountId(sessionId);
			String domainId = srdmSessionRepository.getDomainId(sessionId);
			logger.info("== System Management Log deleted. domainId[{}], accountId[{}] ==", domainId, accountId);
		}
	}

	/**
	 * 定期ログ削除（操作ログ）
	 */
	@Override
	public void deleteOperationLogSchedule(String sessionId, long period) {

		String accountId = srdmSessionRepository.getAccountId(sessionId);
		String domainId = srdmSessionRepository.getDomainId(sessionId);

		// スレッドの管理をFrameworkで行うように変更
		asyncDeleteLog.deleteOperationLog(domainId, accountId, period);
	}

	/**
	 * 定期ログ削除（システム管理ログ）
	 */
	@Override
	public void deleteSystemLogSchedule(String sessionId, long period) {

		String accountId = srdmSessionRepository.getAccountId(sessionId);
		String domainId = srdmSessionRepository.getDomainId(sessionId);

		// スレッドの管理をFrameworkで行うように変更
		asyncDeleteLog.deleteSystemLog(domainId, accountId, period);
	}

	/**
	 * 操作ログ取得（単一）
	 */
	@Override
	public OperationLog getOperationLog(String sessionId, String domainId, String logId)
			throws SrdmDataAccessException, SrdmDataNotFoundException {

		// 指定ドメインが配下にあるのかのチェック
		String loginDomainId = srdmSessionRepository.getDomainId(sessionId);
		if(loginDomainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "Unable to get the operation log.");
		}
		boolean isUnderDomain = domainRepository.isUnderDomain(loginDomainId, domainId);
		if(isUnderDomain == false) {
			logger.warn("domain is not included. domainId[{}]", domainId);
			throw new SrdmDataNotFoundException("domainId", "", "Unable to get the operation log.");
		}

		List<String> domainIdList = domainRepository.findUnderDomainId(domainId);
		domainIdList.add(domainId);
		domainIdList = domainIdList.stream().distinct().collect(Collectors.toList());

		// 指定データ取得
		OperationLog log = operationLogRepository.finedOne(domainIdList, logId);

		return log;
	}

	/**
	 * システム管理ログ取得（単一）
	 * システム管理ログは、全件対象。（domain/account等でのフィルタリング無し）
	 */
	@Override
	public SystemManagementLog getSystemLog(String sessionId, String logId)
			throws SrdmDataAccessException, SrdmDataNotFoundException {

		// 指定データ取得
		SystemManagementLog log = systemManagementLogRepository.finedOne(logId);
		return log;
	}
}
