package srdm.cloud.commonService.domain.service.account;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import srdm.cloud.commonService.domain.model.Account;
import srdm.cloud.commonService.domain.model.Domain;
import srdm.cloud.commonService.domain.model.LogItem;
import srdm.cloud.commonService.domain.model.Role;
import srdm.cloud.commonService.domain.model.UserDetail;
import srdm.cloud.commonService.domain.model.UsernamePasswordAuthentication;
import srdm.cloud.commonService.domain.model.VirtualRole;
import srdm.cloud.commonService.domain.repository.account.AccountRepository;
import srdm.cloud.commonService.domain.repository.account.LoginRepository;
import srdm.cloud.commonService.domain.repository.domain.DomainRepository;
import srdm.cloud.commonService.domain.repository.domain.RoleRepository;
import srdm.cloud.commonService.domain.repository.session.SrdmSessionRepository;
import srdm.cloud.commonService.domain.service.log.OpeLogWriteService;
import srdm.cloud.commonService.util.SrdmPasswordEncoder;
import srdm.common.constant.SrdmConstants;
import srdm.common.constant.SrdmLogConstants;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;
import srdm.common.exception.SrdmGeneralException;

@Service
public class LoginServiceImpl implements LoginService {

	private static final Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);

	@Autowired
	DomainRepository domainRepository;

	@Autowired
	AccountRepository accountRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	SrdmSessionRepository srdmSessionRepository;

	@Autowired
	LoginRepository loginRepository;

	@Autowired
	SrdmPasswordEncoder passwordEncoder;

	@Autowired
	OpeLogWriteService opelogWriteService;

	@Value("${srdm.login.timeout}")
	private long timeout;

	// アカウントロック時間（単位：分）
	@Value("${srdm.auth.lockedTime}")
	private long lockedTime;

	/**
	 * ユーザ認証
	 * 認証OKの場合、sessionIdを返す。
	 * 認証NGの場合、空文字("")を返す。
	 */
	@Override
	public String login(UsernamePasswordAuthentication authInfo) throws SrdmDataAccessException {

		String sessionId = null;

		// 呼出し元
		String operation;
		if(authInfo.getLoginMethod().equals(SrdmConstants.LOGIN_FROM_AGENT)) {
			operation = SrdmLogConstants.OPELOG_OPERATION_LOGIN_AGENT;
		} else {
			operation = SrdmLogConstants.OPELOG_OPERATION_LOGIN;
		}
		List<LogItem> itemList = new ArrayList<LogItem>();

		// ドメインの存在チェック
		try {
			Domain domain = domainRepository.findOne(authInfo.getDomainId());
			logger.debug("Domain exists. domainId[{}], domainName[{}]", authInfo.getDomainId(), domain.getDomainName());
		} catch (SrdmDataNotFoundException e2) {
			// ドメイン無し
			logger.error("Domain not found. domainId[{}], accountName[{}]", authInfo.getDomainId(), authInfo.getAccountName());
			return "";
		}
		// アカウント名からaccountIdを取得
		Account account;
		try {
			account = accountRepository.findOneByName(authInfo.getDomainId(), authInfo.getAccountName());
		} catch (SrdmDataNotFoundException e1) {
			// アカウント無し（accountStatusが"deleted"も含む）
			logger.error("Account not found. domainId[{}], accountName[{}]",authInfo.getDomainId(), authInfo.getAccountName());
			return "";
		}

		// アカウントがlockedの場合、ロック時間を延長
		if(account.getAccountStatus().equals(SrdmConstants.ACCOUNT_STATUS_LOCKED)) {
			// アカウントロック状態
			if((account.getLatestErrorTimestamp() + (lockedTime * 60 * 1000)) > System.currentTimeMillis()) {
				// ロック時間の延長
				logger.warn("account locked. domainId[{}], accountName[{}]", authInfo.getDomainId(), authInfo.getAccountName());
				accountRepository.updateLatestErrorTimestamp(account.getAccountId());
				// 操作ログ記録（error=アカウントロック状態）
				opelogWriteService.writeOperationLog(
						account.getDomainId(),
						account.getAccountId(),
						operation,
						SrdmLogConstants.OPELOG_CODE_LOGIN_LOCKED,
						itemList);
				return "";
			} else {
				// アカウントロック状態クリア
				logger.info("Unlock account. domainId[{}], accountName[{}]", authInfo.getDomainId(), authInfo.getAccountName());
				accountRepository.clearAuthErrorInfo(Arrays.asList(account.getAccountId()));
			}} else if (account.getAccountStatus().equals(SrdmConstants.ACCOUNT_STATUS_MANUAL_LOCKED)) {
				// アカウントがmanualLockedの場合はログインしない
				logger.warn("account locked by user operation. domainId[{}], accountName[{}]", authInfo.getDomainId(), authInfo.getAccountName());
				accountRepository.updateLatestErrorTimestamp(account.getAccountId());
				// 操作ログ記録（error=アカウントロック状態）
				opelogWriteService.writeOperationLog(
						account.getDomainId(),
						account.getAccountId(),
						operation,
						SrdmLogConstants.OPELOG_CODE_LOGIN_LOCKED,
						itemList);
				return "";

		}

		// パスワードチェック
		boolean checkResult = false;
		try {
			checkResult = accountRepository.checkAuth(account.getAccountId(), authInfo.getRawPassword());
		} catch (SrdmDataNotFoundException e1) {
			logger.warn("account info not found. domainId[{}], accountName[{}]", authInfo.getDomainId(), authInfo.getAccountName());
			// 操作ログ記録（error=システムエラー（このタイミングで「アカウントなし」はあり得ないので。）
			opelogWriteService.writeOperationLog(
					account.getDomainId(),
					account.getAccountId(),
					operation,
					SrdmLogConstants.OPELOG_CODE_LOGIN_FAILD,
					itemList);
			return "";
		}
		if(checkResult == true) {
			// ユーザ認証OK
			// エラー情報クリア
			accountRepository.clearAuthErrorInfo(Arrays.asList(account.getAccountId()));

			// sessionId登録
			UserDetail userDetail = new UserDetail();
			if(StringUtils.isEmpty(authInfo.getSessionId())) {
				userDetail.setSessionId("");
			} else {
				userDetail.setSessionId(authInfo.getSessionId());
			}
			userDetail.setAccountId(account.getAccountId());
			userDetail.setDomainId(account.getDomainId());
			userDetail.setGroupId(account.getHomeGroupId());

			List<String> permList = new ArrayList<String>();
			Role role;
			try {
				role = roleRepository.findOne(account.getDomainId(), account.getRoleId());
			} catch (SrdmDataNotFoundException e) {
				// アカウントに紐付くロールが存在しない場合は、全ての権限を持たない仮想的なロールを参照してるものとする。
				logger.warn("Apply virtual role.");
				role = new VirtualRole();
				role.setSessionTimeout(timeout);
			}

			/**
			 * Lamda式での記述
			 * PermissionListの要素をひとつずつ取得し、permListに追加する。
			 */
			role.getPermissionList().stream()
			.forEach(permission -> permList.add(permission.getPermissionName()));
			userDetail.setPermissionList(permList);

			userDetail.setSessionTimeout(role.getSessionTimeout());

			sessionId = srdmSessionRepository.addSession(userDetail);
			// 操作ログ記録（正常）
			opelogWriteService.writeOperationLog(
					account.getDomainId(),
					account.getAccountId(),
					operation,
					SrdmLogConstants.OPELOG_CODE_NORMAL,
					itemList);
		} else {
			// ユーザ認証NG（エラー回数カウントアップ。5回以上認証NGの場合、statusをlockedに。）
			logger.warn("auth error. domainId[{}], accountName[{}]", authInfo.getDomainId(), authInfo.getAccountName());
			accountRepository.setAuthErrorInfo(account.getAccountId());
			// 操作ログ記録（error=パスワード誤り）
			opelogWriteService.writeOperationLog(
					account.getDomainId(),
					account.getAccountId(),
					operation,
					SrdmLogConstants.OPELOG_CODE_LOGIN_PASSWORD_MISTAKE,
					itemList);
			// アカウントロック状態確認
			try {
				Account acc = accountRepository.findOne(account.getAccountId());
				if(acc.getAccountStatus().equals(SrdmConstants.ACCOUNT_STATUS_LOCKED)) {
					// 操作ログ記録（アカウントロック状態変更）
					itemList = new ArrayList<LogItem>();
					itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_ID, account.getDomainId()));
					try {

						Domain domain = domainRepository.findOne(account.getDomainId());
						itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_NAME, domain.getDomainName()));
					} catch (SrdmDataNotFoundException | SrdmDataAccessException e) {
						// 操作ログ記録時のdomainName取得エラー
						logger.error("[Operation Log write] domain name get error. domainId[{}]", account.getDomainId(), e);
					}
					itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_ACCOUNT_NAME, account.getAccountName()));
					itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_LOCK_STATE, SrdmLogConstants.OPELOG_ITEM_VALUE_LOCK));
					itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_CHANGE_FACT, SrdmLogConstants.OPELOG_ITEM_VALUE_PASSWORD_MISTAKE));
					opelogWriteService.writeOperationLog(
							account.getDomainId(),
							account.getAccountId(),
							SrdmLogConstants.OPELOG_OPERATION_CHANGE_ACCOUNT_LOCK_STATE,
							SrdmLogConstants.OPELOG_CODE_NORMAL,
							itemList);
				}
			} catch (SrdmDataNotFoundException e) {
				logger.error("[Operation Log write] account not found. accountId[{}]", account.getAccountId(), e);
			}

			sessionId = "";
		}
		return sessionId;
	}

	/**
	 * ログイン状態取得
	 */
	@Override
	public boolean getLoginStatus(String sessionId) {

		return srdmSessionRepository.checkSession(sessionId);
	}

	/**
	 * ログアウト
	 */
	@Override
	public void logout(String sessionId, String caller) throws SrdmDataNotFoundException, SrdmDataAccessException {

		// 呼出し元
		String operation;
		if(caller.equals(SrdmConstants.LOGIN_FROM_AGENT)) {
			operation = SrdmLogConstants.OPELOG_OPERATION_LOGOUT_AGENT;
		} else {
			operation = SrdmLogConstants.OPELOG_OPERATION_LOGOUT;
		}

		// 操作ログ記録（正常）
		List<LogItem> itemList = new ArrayList<LogItem>();
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_LOGOUT_FACTOR, SrdmLogConstants.OPELOG_ITEM_VALUE_OPERATION));
		opelogWriteService.writeOperationLog(
				sessionId,
				operation,
				SrdmLogConstants.OPELOG_CODE_NORMAL,
				itemList);

		// sessionId無効化
		srdmSessionRepository.invalidateSession(sessionId);
	}

	/**
	 * ユーザ認証（Schedule Service用）
	 * 認証OKの場合、sessionIdを返す。
	 * 認証NGの場合、空文字("")を返す。
	 */
	@Override
	public String loginForSchedule(UsernamePasswordAuthentication authInfo) throws SrdmDataAccessException {

		String sessionId = null;

		// ドメインの存在チェック
		try {
			Domain domain = domainRepository.findOne(authInfo.getDomainId());
			logger.debug("Domain exists. domainId[{}], domainName[{}]", authInfo.getDomainId(), domain.getDomainName());
		} catch (SrdmDataNotFoundException e2) {
			// ドメイン無し
			logger.error("Domain not found. domainId[{}], accountId[{}]", authInfo.getDomainId(), authInfo.getAccountId());
			return "";
		}
		// アカウントを取得
		Account account;
		try {
			account = accountRepository.findOne(authInfo.getAccountId());
		} catch (SrdmDataNotFoundException e1) {
			// アカウント無し（accountStatusが"deleted"も含む）
			logger.error("Account not found. domainId[{}], accountId[{}]", authInfo.getDomainId(), authInfo.getAccountId());
			return "";
		}

		// 指定されたdomainIdとaccountが属するdomainIdが等しいかをチェック
		if(authInfo.getDomainId().equals(account.getDomainId()) == false) {
			// 指定されたaccountがdomain内に存在しない。
			logger.error("Account not found. domainId[{}], accountId[{}]", authInfo.getDomainId(), authInfo.getAccountId());
			return "";
		}

		// ユーザ認証OK（アカウントが有効（deletedでなければOKとする）
		// sessionId登録
		UserDetail userDetail = new UserDetail();
		if(StringUtils.isEmpty(authInfo.getSessionId())) {
			userDetail.setSessionId("");
		} else {
			userDetail.setSessionId(authInfo.getSessionId());
		}
		userDetail.setAccountId(account.getAccountId());
		userDetail.setDomainId(account.getDomainId());
		userDetail.setGroupId(account.getHomeGroupId());

		List<String> permList = new ArrayList<String>();
		Role role;
		try {
			role = roleRepository.findOne(account.getDomainId(), account.getRoleId());
		} catch (SrdmDataNotFoundException e) {
			// アカウントに紐付くロールが存在しない場合は、全ての権限を持たない仮想的なロールを参照してるものとする。
			logger.warn("Apply virtual role.");
			role = new VirtualRole();
			role.setSessionTimeout(timeout);
		}

		/**
		 * Lamda式での記述
		 * PermissionListの要素をひとつずつ取得し、permListに追加する。
		 */
		role.getPermissionList().stream()
		.forEach(permission -> permList.add(permission.getPermissionName()));
		userDetail.setPermissionList(permList);

		userDetail.setSessionTimeout(role.getSessionTimeout());

		sessionId = srdmSessionRepository.addSession(userDetail);
		return sessionId;
	}

	/**
	 * ログアウト（ScheduleService用）
	 */
	@Override
	public void logoutForSchedule(String sessionId) throws SrdmDataNotFoundException, SrdmDataAccessException {

		srdmSessionRepository.invalidateSession(sessionId);

		/**
		 * ScheduleService用のlogout処理のため、操作ログの記録なし
		 */
	}

	/**
	 * オンプレミスエージェントのログイン
	 */
	@Override
	public String loginForOnPremisesAgent(String loginId) throws SrdmGeneralException {

		String sessionId = "";

		boolean authResult = loginRepository.checkLoginId(loginId);
		// 認証失敗(loginId不一致)
		if (authResult == false) {
			logger.warn("LoginId not found.");
			return sessionId;
		}

		// sessionId登録
		UserDetail userDetail = new UserDetail();
		// ないと例外を起こすので、MasterDeviceGroupのIDをセットしておく
		userDetail.setGroupId("0");
		// 以下は空でも動作に影響ない

		userDetail.setSessionId("");
		userDetail.setAccountId("");
		userDetail.setDomainId("");
		List<String> permissionList = new ArrayList<String>();
		permissionList.add(SrdmConstants.PERM_NAME_AGENTINSTALL);
		permissionList.add(SrdmConstants.PERM_NAME_GROUP);
		userDetail.setPermissionList(permissionList);
		userDetail.setSessionTimeout(timeout);

		sessionId = srdmSessionRepository.addSession(userDetail);
		if (sessionId == null) {
			throw new SrdmGeneralException(SrdmConstants.ERROR_E0025, SrdmConstants.ERROR_MESSAGE_E0025);
		}

		return sessionId;
	}

}
