package srdm.cloud.commonService.domain.service.account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import srdm.cloud.commonService.domain.model.Account;
import srdm.cloud.commonService.domain.model.AccountInfo;
import srdm.cloud.commonService.domain.model.Domain;
import srdm.cloud.commonService.domain.model.EditAccount;
import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.GetListRes;
import srdm.cloud.commonService.domain.model.LogItem;
import srdm.cloud.commonService.domain.model.LoginAccountInfo;
import srdm.cloud.commonService.domain.model.MibDomainExtension;
import srdm.cloud.commonService.domain.model.Role;
import srdm.cloud.commonService.domain.model.SimpleAccount;
import srdm.cloud.commonService.domain.model.SimpleGroup;
import srdm.cloud.commonService.domain.model.VirtualRole;
import srdm.cloud.commonService.domain.repository.account.AccountRepository;
import srdm.cloud.commonService.domain.repository.domain.DomainRepository;
import srdm.cloud.commonService.domain.repository.domain.RoleRepository;
import srdm.cloud.commonService.domain.repository.domainExtension.MibDomainExtensionRepository;
import srdm.cloud.commonService.domain.repository.group.GroupRepository;
import srdm.cloud.commonService.domain.repository.session.SrdmSessionRepository;
import srdm.cloud.commonService.domain.service.log.OpeLogWriteService;
import srdm.cloud.commonService.util.SrdmPasswordEncoder;
import srdm.common.constant.SrdmConstants;
import srdm.common.constant.SrdmLogConstants;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;
import srdm.common.exception.SrdmGeneralException;
import srdm.common.exception.SrdmParameterValidationException;

@Service
public class AccountServiceImpl implements AccountService {

	private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

	@Autowired
	AccountRepository accountRepository;

	@Autowired
	DomainRepository domainRepository;

	@Autowired
	SrdmSessionRepository srdmSessionRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	MibDomainExtensionRepository mibDomainExtensionRepository;

	@Autowired
	GroupRepository groupRepository;

	@Autowired
	SrdmPasswordEncoder passwordEncoder;

	@Autowired
	OpeLogWriteService opelogWriteService;

	// アカウントロック時間（単位：分）
	@Value("${srdm.auth.lockedTime}")
	private long lockedTime;

	/**
	 * アカウント取得（単一） 取得できない場合は、nullを返す。
	 *
	 * @throws SrdmDataNotFoundException
	 * @throws SrdmDataAccessException
	 * @throws SrdmGeneralException
	 */
	@Override
	public AccountInfo getAccount(String sessionId, String accountId)
			throws SrdmDataAccessException, SrdmDataNotFoundException, SrdmGeneralException {

		// 指定アカウントのデータ取得
		Account account = accountRepository.findOne(accountId);

		// 取得したデータが指定ドメインが配下にあるのかのチェック
		String loginDomainId = srdmSessionRepository.getDomainId(sessionId);
		if (loginDomainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "Unable to get the account.");
		}
		boolean isUnderDomain = domainRepository.isUnderDomain(loginDomainId, account.getDomainId());
		if (isUnderDomain == false) {
			logger.warn("domain is not included. domainId[{}]", account.getDomainId());
			throw new SrdmDataNotFoundException("domainId", "", "Unable to get the account.");
		}

		// アカウント管理権限チェック
		boolean hasAccount = srdmSessionRepository.hasPermission(sessionId, SrdmConstants.PERM_NAME_ACCOUNT);
		if (hasAccount == false) {
			String loginAccountId = srdmSessionRepository.getAccountId(sessionId);
			// 編集対象がログインアカウント以外
			if (loginAccountId.equals(account.getAccountId()) != true) {
				logger.warn("You do not have sufficient privileges. permission[{}]", SrdmConstants.PERM_NAME_ACCOUNT);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023);
			}
		}
		// 自ドメインアカウント管理権限チェック
		boolean hasAccountOfOwnDomain = srdmSessionRepository.hasPermission(sessionId,
				SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN);
		if (hasAccountOfOwnDomain == false) {
			String loginAccountId = srdmSessionRepository.getAccountId(sessionId);
			// 編集対象がログインドメインのログインアカウント以外
			if ((loginAccountId.equals(account.getAccountId()) != true)
					&& (loginDomainId.equals(account.getDomainId()) == true)) {
				logger.warn("You do not have sufficient privileges. permission[{}]",
						SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023);
			}
		}

		/**
		 * アカウント状態の更新
		 * （アカウントロック後、ロック時間（SRDM2.4.0では、30分）経過していれば、ステータスを'active'に変更して返す）
		 */
		if (account.getAccountStatus().equals(SrdmConstants.ACCOUNT_STATUS_LOCKED) == true) {
			if (account.getLatestErrorTimestamp() + (lockedTime * 60 * 1000) <= System.currentTimeMillis()) {
				// ロック時間を経過している場合は、activeに置き換える
				account.setAccountStatus(SrdmConstants.ACCOUNT_STATUS_ACTIVE);
			}
		}

		SimpleGroup group = groupRepository.findOne(account.getHomeGroupId());

		// ロール削除不可属性の取得
		Role role = null;
		role = roleRepository.getRoleDetails(account.getDomainId(), account.getRoleId());

		AccountInfo accountInfo = new AccountInfo();
		BeanUtils.copyProperties(account, accountInfo);
		accountInfo.setPermanentAccount(account.isPermanentAccount());
		if (role != null && role.getRoleAttribute() != null) {
			accountInfo.setPrivateRole(role.getRoleAttribute().isPrivateRole());
		} else {
			accountInfo.setPrivateRole(false);
		}
			if (!group.getParentGroupId().isEmpty()) {
				accountInfo.setHomeGroupName(group.getGroupName());
			}

		return accountInfo;
	}

	/**
	 * アカウント取得（複数）
	 *
	 * @throws SrdmDataAccessException
	 * @throws SrdmDataNotFoundException
	 */
	@Override
	public GetListRes getAccounts(String sessionId, GetListReq getListReq)
			throws SrdmDataAccessException, SrdmDataNotFoundException {

		GetListRes getListRes = new GetListRes();

		// 指定ドメインが配下にあるのかのチェック
		String loginDomainId = srdmSessionRepository.getDomainId(sessionId);
		if (loginDomainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "Unable to get the account list.");
		}
		boolean isUnderDomain = domainRepository.isUnderDomain(loginDomainId, getListReq.getKeyMap().get("domainId"));
		if (isUnderDomain == false) {
			logger.warn("domain is not included. domainId[{}]", getListReq.getKeyMap().get("domainId"));
			throw new SrdmDataNotFoundException("domainId", "", "Unable to get the account list.");
		}

		boolean hasAccountOfOwnDomain = srdmSessionRepository.hasPermission(sessionId,
				SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN);
		String loginAccountId = srdmSessionRepository.getAccountId(sessionId);
		List<String> accountIdList = new ArrayList<String>();
		// 自ドメインのアカウント管理権限が無い場合、自ドメインの他のアカウントのログを除く
		if (hasAccountOfOwnDomain == false) {
			List<Account> accountList = accountRepository.findAllByDomainId(loginDomainId); // ログインアカウントと同じドメインのaccoutIdを取得
			accountList.stream().forEach(a -> accountIdList.add(a.getAccountId()));
			accountIdList.remove(loginAccountId); // ログインアカウントのaccountIdを除く
		} else {
			accountIdList.add("");
		}
		accountIdList.add("");
		getListReq.getKeyListMap().put("ignoreAccountIdList", accountIdList);

		// 削除可否判定用のaccountIdを設定
		getListReq.getKeyMap().put("loginAccountId", loginAccountId);

		// 指定データ取得
		List<SimpleAccount> accountList;
		accountList = accountRepository.findAllWithPagable(getListReq);
		getListRes.setList(accountList);
		getListRes.setResultCount(accountList.size());

		// Total件数取得
		String total;
		try {
			total = accountRepository.count(getListReq);
		} catch (SrdmDataNotFoundException e) {
			// Total件数 0件
			total = "0";
		}
		getListRes.setTotalCount(Long.parseLong(total));

		return getListRes;
	}

	/**
	 * ログインアカウント情報の取得
	 *
	 * @throws SrdmDataAccessException
	 * @throws SrdmDataNotFoundException
	 */
	@Override
	public LoginAccountInfo getLoginAccountInfo(String sessionId)
			throws SrdmDataAccessException, SrdmDataNotFoundException {

		LoginAccountInfo loginAccountInfo = new LoginAccountInfo();

		String accountId = srdmSessionRepository.getAccountId(sessionId);
		String domainId = srdmSessionRepository.getDomainId(sessionId);
		if (accountId.isEmpty() || domainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("accountId or domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "Account info dose not exist.");
		}

		Account account;
		account = accountRepository.findOne(accountId);
		Domain domain = domainRepository.findOne(domainId);
		String targetGroupId = account.getHomeGroupId();
		SimpleGroup group = groupRepository.findOne(targetGroupId);

		loginAccountInfo.setDomainId(domainId);
		loginAccountInfo.setDomainName(domain.getDomainName());
		loginAccountInfo.setAccountId(accountId);
		loginAccountInfo.setAccountName(account.getAccountName());
		loginAccountInfo.setPermanentAccount(account.isPermanentAccount());
		loginAccountInfo.setLanguage(account.getLanguage());
		loginAccountInfo.setTimeZoneSpecifingType(account.getTimeZoneSpecifingType());
		loginAccountInfo.setDateTimeFormat(account.getDateTimeFormat());
		loginAccountInfo.setTimeZoneId(account.getTimeZone());
		loginAccountInfo.setHomeGroupId(account.getHomeGroupId());
		if (group.getGroupId().isEmpty() ) {
			loginAccountInfo.setTargetGroupState(SrdmConstants.IN_ACTIVE);
		}else{
			loginAccountInfo.setTargetGroupState(SrdmConstants.ACTIVE);
		}

		Role role;
		try {
			role = roleRepository.findOne(domainId, account.getRoleId());
		} catch (SrdmDataNotFoundException e) {
			// 紐付くRoleが存在しない（アカウントに紐付くロールが存在しない場合は、権限なしの仮想ロールを適用）
			logger.warn("Apply virtual role.");
			role = new VirtualRole();
		}
		loginAccountInfo.setPermissionList(role.getPermissionList());

		return loginAccountInfo;
	}

	/**
	 * アカウント作成
	 *
	 * @throws SrdmDataAccessException
	 * @throws SrdmGeneralException
	 * @throws SrdmDataNotFoundException
	 */
	@Override
	public String create(String sessionId, AccountInfo accountInfo)
			throws SrdmDataAccessException, SrdmGeneralException, SrdmDataNotFoundException {

		// 指定ドメインが配下にあるのかのチェック
		String loginDomainId = srdmSessionRepository.getDomainId(sessionId);
		if (loginDomainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "Unable to create account.");
		}
		String createAccountDomainId = accountInfo.getDomainId();
		boolean isUnderDomain = domainRepository.isUnderDomain(loginDomainId, createAccountDomainId);
		if (isUnderDomain == false) {
			logger.warn("domain is not included. domainId[{}]", createAccountDomainId);
			throw new SrdmDataNotFoundException("domainId", "", "Unable to create account.");
		}

		// 操作ログ詳細項目
		List<LogItem> itemList = new ArrayList<LogItem>();
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_ACCOUNT_NAME, accountInfo.getAccountName()));

		// 操作ログ詳細項目追加
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_ID, createAccountDomainId));
		try {

			Domain domain = domainRepository.findOne(createAccountDomainId);
			itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_NAME, domain.getDomainName()));
		} catch (SrdmDataNotFoundException | SrdmDataAccessException e) {
			// 操作ログ記録時のdomainName取得エラー
			logger.error("[Operation Log write] domain name get error. domainId[{}]", createAccountDomainId, e);
		}

		// 自ドメインアカウント管理権限チェック
		boolean hasAccountOfOwnDomain = srdmSessionRepository.hasPermission(sessionId,
				SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN);
		if (hasAccountOfOwnDomain == false) {
			// ログインアカウントのdomainIdと作成先のdomainIdを比較
			if (loginDomainId.equals(createAccountDomainId) == true) {
				logger.warn("You do not have sufficient privileges. permission[{}]",
						SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023);
			}
		}

		// ロールの存在チェック
		boolean isExist = roleRepository.isExist(createAccountDomainId, accountInfo.getRoleId());
		if (!isExist) {
			logger.warn("Role not found. domainId[{}], roleId[{}]", createAccountDomainId, accountInfo.getRoleId());
			// 操作ログ（失敗：指定ロールなし）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_CREATE_ACCOUNT,
					SrdmLogConstants.OPELOG_CODE_CRE_ACNT_UNKNOWN_ROLE, itemList);
			throw new SrdmDataNotFoundException("roleId", "", "Unable to create account.");
		}

		// 同一アカウント名の存在チェック
		isExist = accountRepository.isExistAccoutName(createAccountDomainId, accountInfo.getAccountName(), "");
		if (isExist) {
			logger.warn("Account Name is exist.");
			// 操作ログ（失敗：同一アカウント名あり）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_CREATE_ACCOUNT,
					SrdmLogConstants.OPELOG_CODE_CRE_ACNT_SAME_NAME, itemList);
			throw new SrdmGeneralException(SrdmConstants.ERROR_E0042, SrdmConstants.ERROR_MESSAGE_E0042);
		}
		boolean privateRole = accountInfo.isPrivateRole();
		// 属性のチェック
		if (loginDomainId.equals(createAccountDomainId) == true) {
			boolean permanentAccount = accountInfo.isPermanentAccount();
			if (permanentAccount) {
				logger.warn("In the same domain [true] is invalid.(permanentAccount)");
				// 操作ログ（失敗：permanentAccountへの不正な値の指定）
				opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_CREATE_ACCOUNT,
						SrdmLogConstants.OPELOG_CODE_CRE_ACNT_INVLD_ATTRBT, itemList);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0024, "permanentAccount", String.valueOf(permanentAccount),
						SrdmConstants.ERROR_E0024);
			}
			if (privateRole) {
				logger.warn("In the same domain [true] is invalid.(privateRole)");
				// 操作ログ（失敗:privateRoleへの不正な値の指定）
				opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_CREATE_ACCOUNT,
						SrdmLogConstants.OPELOG_CODE_CRE_ACNT_INVLD_ATTRBT, itemList);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0024, "isPrivateRole", String.valueOf(privateRole),
						SrdmConstants.ERROR_E0024);
			}
		}

		// ロールの紐付けの重複 + 紐付け制限ありを指定
		if (privateRole) {
			List<Account> accountList = accountRepository.findAllByRoleId(accountInfo.getRoleId());
			if (accountList != null && !accountList.isEmpty()) {
				logger.warn("Illegal bind of the role.");
				// 操作ログ（失敗：紐付け不可ロールを指定）
				opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_CREATE_ACCOUNT,
						SrdmLogConstants.OPELOG_CODE_CRE_ACNT_ILLEGAL_BINDING, itemList);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0027, SrdmConstants.ERROR_E0027);
			}
		}

		if (accountInfo.getTimeZoneSpecifingType().equals(SrdmConstants.TIMEZONE_SPECIFING_TYPE_AUTO)) {
			// タイムゾーン指定方式が"auto"の場合、timeZoneに空文字をセット
			accountInfo.setTimeZone("");
		}

		/**
		 * HomeGroup（iPAUDomainExtensionDBから取得） SRDM2.4.0ではアカウントのHomeGroup =
		 * domainの管理対象グループの為、 iPAUDomainExtensionDBから取得し、設定。 今後、アカウント毎にHome
		 * Groupを設定可能とする場合、createAccountのリクエストパラメータに追加？
		 * また、その場合、Filter情報やMailAlert情報等の参照先、保存先のgroupIdの取得方法を見直す必要あり。
		 * 今は、sessionIdに紐付くgroupIdを使用。これを、sessionIdに紐付くdomainIdを元に
		 * iPAUDomainExtensionDBから管理対象グループIDを取得するように変更必要。
		 */
		// homeGroupIdの存在チェック
		MibDomainExtension domainExtension = mibDomainExtensionRepository.findOne(createAccountDomainId);
		String homeGroupId = accountInfo.getHomeGroupId();
		boolean isUnderGroup = groupRepository.isUnderGroup(domainExtension.getGroupId(), homeGroupId);
		if (isUnderGroup == false) {
			logger.warn("group is not included. homeGroupIs[{}]", homeGroupId);
			// 操作ログ記録（失敗：管理対象グループなし）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_CREATE_ACCOUNT,
					SrdmLogConstants.OPELOG_CODE_CRE_ACNT_UNKNOWN_GRP, itemList);
			throw new SrdmGeneralException(SrdmConstants.ERROR_E0018, "homeGroupId", "", "Unable to create account.");
		}

		// account type(現状は、local固定）
		accountInfo.setAccountType(SrdmConstants.ACCOUNT_TYPE_LOCAL);

		// account作成
		String id;
		try {
			Account account = new Account();
			BeanUtils.copyProperties(accountInfo, account);
			account.setPermanentAccount(accountInfo.isPermanentAccount());
			id = accountRepository.create(account);
			roleRepository.updatePrivateRoleAttribute(accountInfo.getDomainId(), accountInfo.getRoleId(), privateRole);
			// 操作ログ記録（成功）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_CREATE_ACCOUNT,
					SrdmLogConstants.OPELOG_CODE_NORMAL, itemList);
		} catch (SrdmDataAccessException e) {
			// 操作ログ記録（失敗：アクセスエラー）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_CREATE_ACCOUNT,
					SrdmLogConstants.OPELOG_CODE_CRE_ACNT_FAILD, itemList);
			throw e;
		}

		return id;
	}

	/**
	 * アカウント編集
	 *
	 * @throws SrdmDataAccessException
	 * @throws SrdmDataNotFoundException
	 * @throws SrdmGeneralException
	 * @throws SrdmParameterValidationException
	 */
	@Override
	public void update(String sessionId, EditAccount editAccount, boolean editPrivateRole)
			throws SrdmDataAccessException, SrdmDataNotFoundException, SrdmGeneralException,
			SrdmParameterValidationException {

		// アカウントの存在チェック
		Account account;
		try {
			account = accountRepository.findOne(editAccount.getAccountId());
		} catch (SrdmDataAccessException | SrdmDataNotFoundException e) {
			// 操作ログ記録（失敗：指定アカウントなし）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_EDIT_ACCOUNT,
					SrdmLogConstants.OPELOG_CODE_EDT_ACNT_NOT_FOUND, new ArrayList<LogItem>());
			throw e;
		}

		// 操作ログ用の情報
		List<LogItem> itemList = new ArrayList<LogItem>();
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_ID, account.getDomainId()));
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_ACCOUNT_NAME, account.getAccountName()));
		try {

			Domain domain = domainRepository.findOne(account.getDomainId());
			itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_NAME, domain.getDomainName()));
		} catch (SrdmDataNotFoundException | SrdmDataAccessException e) {
			// 操作ログ記録時のdomainName取得エラー
			logger.error("[Operation Log write] domain name get error. domainId[{}]", account.getDomainId(), e);
		}
		String changeItem;
		if (editAccount.isChangePasswordFlag() == true) {
			changeItem = SrdmLogConstants.OPELOG_ITEM_VALUE_PASSWORD;
		} else {
			changeItem = SrdmLogConstants.OPELOG_ITEM_VALUE_OTHER;
		}
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_CHANGE_ITEM, changeItem));

		// 取得したデータが指定ドメインが配下にあるのかのチェック
		String loginDomainId = srdmSessionRepository.getDomainId(sessionId);
		if (loginDomainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "Unable to update the account.");
		}
		boolean isUnderDomain = domainRepository.isUnderDomain(loginDomainId, account.getDomainId());
		if (isUnderDomain == false) {
			logger.warn("domain is not included. domainId[{}]", account.getDomainId());
			throw new SrdmDataNotFoundException("domainId", "", "Unable to update the account.");
		}

		// アカウント管理権限チェック
		boolean hasAccount = srdmSessionRepository.hasPermission(sessionId, SrdmConstants.PERM_NAME_ACCOUNT);
		if (hasAccount == false) {
			String loginAccountId = srdmSessionRepository.getAccountId(sessionId);
			// 編集対象がログインアカウント以外
			if (loginAccountId.equals(editAccount.getAccountId()) != true) {
				logger.warn("You do not have sufficient privileges. permission[{}]", SrdmConstants.PERM_NAME_ACCOUNT);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023);
			}
		}
		// 自ドメインアカウント管理権限チェック
		boolean hasAccountOfOwnDomain = srdmSessionRepository.hasPermission(sessionId,
				SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN);
		if (hasAccountOfOwnDomain == false) {
			String loginAccountId = srdmSessionRepository.getAccountId(sessionId);
			// 編集対象がログインドメインのアカウントで且つ、ログインアカウント以外
			if ((loginAccountId.equals(editAccount.getAccountId()) == false)
					&& (loginDomainId.equals(account.getDomainId()) == true)) {
				logger.warn("You do not have sufficient privileges. permission[{}]",
						SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023);
			}
		}

		// ロールIDの存在チェック
		boolean isExist = roleRepository.isExist(account.getDomainId(), editAccount.getRoleId());
		if (isExist == false) {
			logger.warn("role not found. roleId[{}]", editAccount.getRoleId());
			// 操作ログ（失敗：指定ロールなし）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_EDIT_ACCOUNT,
					SrdmLogConstants.OPELOG_CODE_EDT_ACNT_UNKNOWN_ROLE, itemList);
			throw new SrdmDataNotFoundException("roleId", "", "Unable to update the account.");
		}

		// ログインアカウントの場合、roleIdの変更がないかをチェック
		String loginAccountId = srdmSessionRepository.getAccountId(sessionId);
		if (loginAccountId.equals(editAccount.getAccountId()) == true) {
			Account loginAccount = accountRepository.findOne(loginAccountId);
			if (editAccount.getRoleId().equals(loginAccount.getRoleId()) == false) {
				logger.warn("Access improper. can not change roleId.");
				// 操作ログ（失敗：ログインアカウントのロール変更指定）
				opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_EDIT_ACCOUNT,
						SrdmLogConstants.OPELOG_CODE_EDT_ACNT_OWN_ROLEID, itemList);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023);
			}
		}

		// アカウント名のチェック（変更後のアカウント名と同一のアカウント名が無いか？）
		isExist = accountRepository.isExistAccoutName(account.getDomainId(), editAccount.getAccountName(),
				editAccount.getAccountId());
		if (isExist == true) {
			logger.warn("Account Name is exist.");
			// 操作ログ（失敗：同一アカウント名あり）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_EDIT_ACCOUNT,
					SrdmLogConstants.OPELOG_CODE_EDT_ACNT_SAME_NAME, itemList);
			throw new SrdmGeneralException(SrdmConstants.ERROR_E0042, SrdmConstants.ERROR_MESSAGE_E0042);
		}

		// 属性のチェック
		if (loginDomainId.equals(account.getDomainId()) && !account.getRoleId().isEmpty()) {
			// 同一ドメインの場合、属性の変更不可
			boolean editPermanentAccount = editAccount.isPermanentAccount();
			if (account.isPermanentAccount() != editPermanentAccount) {
				logger.warn("Invalid the permanentAccount change.");
				// 操作ログ（失敗：permanentAccountの変更禁止）
				opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_EDIT_ACCOUNT,
						SrdmLogConstants.OPELOG_CODE_EDT_ACNT_INVLD_ATTRBT, itemList);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0024, "isPermanentAccount", String.valueOf(editPermanentAccount),
						SrdmConstants.ERROR_E0024);
			}
			Role role = roleRepository.findOne(account.getDomainId(), account.getRoleId());
			if (role.getRoleAttribute().isPrivateRole() != editPrivateRole) {
				logger.warn("Invalid the privateRole change.");
				// 操作ログ（失敗：privateROleの変更禁止）
				opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_EDIT_ACCOUNT,
						SrdmLogConstants.OPELOG_CODE_EDT_ACNT_INVLD_ATTRBT, itemList);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0024, "isPrivateRole", String.valueOf(editPrivateRole),
						SrdmConstants.ERROR_E0024);
			}
		}
		// ロールの紐付けの重複 + 紐付け制限ありを指定
		if (editPrivateRole) {
			List<Account> accountList = accountRepository.findAllByRoleId(editAccount.getRoleId());
			if (accountList != null && !accountList.isEmpty()) {
				for (Account checkAccount : accountList) {
					// logger.info("editAccountId=" + editAccount.getAccountId()
					// + "; checkAccountId=" + checkAccount.getAccountId());
					if (checkAccount.getAccountId().equals(editAccount.getAccountId()) == false) {
						logger.warn("Role specification is invalid.");
						// 操作ログ（失敗:複数アカウントに紐付くロール属性を紐付け制限ありに指定）
						opelogWriteService.writeOperationLog(sessionId,
								SrdmLogConstants.OPELOG_OPERATION_EDIT_ACCOUNT,
								SrdmLogConstants.OPELOG_CODE_EDT_ACNT_ILLEGAL_BINDING, itemList);
						throw new SrdmGeneralException(SrdmConstants.ERROR_E0027, SrdmConstants.ERROR_E0027);
					}
				}
			}
		}
		// ログインアカウントのパスワードチェック
		if (editAccount.isChangePasswordFlag()) {
			// ログインアカウントのパスワードチェック
			boolean checkResult = false;
			if (loginAccountId.isEmpty()) {
				// sessionIdチェックを行っているので基本的にここでのエラーはない。
				logger.warn("login accountId not found. sessionId[{}]", sessionId);
				throw new SrdmDataNotFoundException("sessionId", "", "Unable to update the account.");
			}
			try {
				checkResult = accountRepository.checkAuth(loginAccountId, editAccount.getLoginAccountPassword());
			} catch (SrdmDataNotFoundException e) {
				// 操作ログ（失敗：ログインアカウントパスワード確認エラー）
				opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_EDIT_ACCOUNT,
						SrdmLogConstants.OPELOG_CODE_EDT_ACNT_PASSWORD_ERROR, itemList);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0025, SrdmConstants.ERROR_MESSAGE_E0025);
			}
			if (checkResult == false) {
				logger.warn("Login account auth error. login accountId[{}]", loginAccountId);
				// 操作ログ（失敗：ログインアカウントパスワード確認エラー）
				opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_EDIT_ACCOUNT,
						SrdmLogConstants.OPELOG_CODE_EDT_ACNT_PASSWORD_ERROR, itemList);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0025, SrdmConstants.ERROR_MESSAGE_E0025);
			}
		}

		// timeZoneId更新
		if (editAccount.getTimeZoneSpecifingType().equals(SrdmConstants.TIMEZONE_SPECIFING_TYPE_AUTO)) {
			// タイムゾーン指定方式が"auto"の場合、timeZoneに空文字をセット
			editAccount.setTimeZoneId("");
		}
		// homeGroupIdの存在チェック
		MibDomainExtension domainExtension = mibDomainExtensionRepository.findOne(account.getDomainId());
		String homeGroupId = editAccount.getHomeGroupId();
		boolean isUnderGroup = groupRepository.isUnderGroup(domainExtension.getGroupId(), homeGroupId);
		if (isUnderGroup == false) {
			logger.warn("group is not included. homeGroupIs[{}]", homeGroupId);
			// 操作ログ記録（失敗：管理対象グループなし）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_EDIT_ACCOUNT,
					SrdmLogConstants.OPELOG_CODE_EDT_ACNT_UNKNOWN_GRP, itemList);
			throw new SrdmGeneralException(SrdmConstants.ERROR_E0018, "homeGroupId", "", "Unable to update account.");
		}

		// アカウント情報更新
		try {
			accountRepository.update(editAccount);
			roleRepository.updatePrivateRoleAttribute(account.getDomainId(), editAccount.getRoleId(), editPrivateRole);
			if(account.getRoleId() != null && !account.getRoleId().isEmpty()  && !account.getRoleId().equals(editAccount.getRoleId())){
				roleRepository.updatePrivateRoleAttribute(account.getDomainId(), account.getRoleId(), false);
			}
			// 操作ログ記録（成功）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_EDIT_ACCOUNT,
					SrdmLogConstants.OPELOG_CODE_NORMAL, itemList);
		} catch (SrdmDataAccessException e) {
			// 操作ログ記録（失敗：DBアクセスエラー）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_EDIT_ACCOUNT,
					SrdmLogConstants.OPELOG_CODE_EDT_ACNT_FAILD, itemList);
			throw e;
		}

	}

	/**
	 * アカウント削除
	 *
	 * @throws SrdmDataAccessException
	 * @throws SrdmDataNotFoundException
	 * @throws SrdmGeneralException
	 */
	@Override
	public void delete(String sessionId, List<String> accountIdList)
			throws SrdmDataAccessException, SrdmDataNotFoundException, SrdmGeneralException {

		// sessionIdからdomeinIdとaccountIdを取得
		String loginDomainId = srdmSessionRepository.getDomainId(sessionId);
		if (loginDomainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "Unable to delete the account.");
		}
		String loginAccountId = srdmSessionRepository.getAccountId(sessionId);
		if (loginAccountId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login accountId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("domainId", "", "Unable to delete the account.");
		}

		// 自ドメインアカウント管理権限有無
		boolean hasAccountOfOwnDomain = srdmSessionRepository.hasPermission(sessionId,
				SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN);

		// 操作ログ用
		List<LogItem> itemList = new ArrayList<LogItem>();
		// 削除対象ROLE IDリスト
		List<Account> accountListForRoleDelete = new ArrayList<Account>();
		String opelogDomainId = "";
		StringBuilder opeLogAccountName = new StringBuilder();

		// アカウントの存在チェック
		for (String accountId : accountIdList) {

			// 取得したデータが指定ドメインが配下にあるのかのチェック
			Account account;
			try {
				account = accountRepository.findOne(accountId);
			} catch (SrdmDataAccessException | SrdmDataNotFoundException e) {
				logger.warn("account not found. accountId[{}]", accountId);
				// 操作ログ（失敗：指定アカウントなし）
				opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_DELETE_ACCOUNT,
						SrdmLogConstants.OPELOG_CODE_DEL_ACNT_NOT_FOUND, new ArrayList<LogItem>());
				throw e;
			}

			boolean isUnderDomain = domainRepository.isUnderDomain(loginDomainId, account.getDomainId());
			if (isUnderDomain == false) {
				logger.warn("domain is not included. accountId[{}]", accountId);
				// 操作ログ（失敗：指定アカウントなし）
				opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_DELETE_ACCOUNT,
						SrdmLogConstants.OPELOG_CODE_DEL_ACNT_NOT_FOUND, new ArrayList<LogItem>());
				throw new SrdmDataNotFoundException("accountId", "", "Unable to delete the account.");
			}

			// 操作ログ用データ保持
			/**
			 * domainIdは、リストの最後のaccountの属するdomainIdを保持。
			 * 現状のUIは、アカウント削除時、domainをまたがった指定が出来ないため、問題なし。
			 * 将来、domainをまたがった指定を可能にする場合、操作ログの内容を見直す必要あり。
			 */
			opelogDomainId = account.getDomainId();
			opeLogAccountName.append(account.getAccountName()).append(",");

			// 自ドメインアカウント管理権限有無によるチェック
			if (hasAccountOfOwnDomain == false) {
				// 編集対象がログインドメインのログインアカウント以外
				if (loginDomainId.equals(account.getDomainId()) == true) {
					logger.warn("You do not have sufficient privileges. permission[{}]",
							SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN);
					throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023);
				}
			}

			// 削除不可アカウントのチェック
			if (account.isPermanentAccount()) {
				logger.warn("Can not delete. accountId[{}]", accountId);
				itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_ID, opelogDomainId));
				try {

					Domain itemDomain = domainRepository.findOne(opelogDomainId);
					itemList.add(
							new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_NAME, itemDomain.getDomainName()));
				} catch (SrdmDataNotFoundException | SrdmDataAccessException e) {
					// 操作ログ記録時のdomainName取得エラー
					logger.error("[Operation Log write] domain name get error. domainId[{}]", opelogDomainId, e);
				}
				itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_ACCOUNT_NAME,
						opeLogAccountName.subSequence(0, opeLogAccountName.length() - 1).toString()));

				// 操作ログ（失敗：指定アカウントなし）
				opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_DELETE_ACCOUNT,
						SrdmLogConstants.OPELOG_CODE_DEL_ACNT_CANTDEL_ACNT, itemList);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023);
			}

			// 紐付け制限ありロールの抽出（アカウントと同時に削除）
			Role role = roleRepository.getRoleDetails(account.getDomainId(), account.getRoleId());
			if (role != null && role.getRoleAttribute().isPrivateRole()) {
				accountListForRoleDelete.add(account);
			}
		}

		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_ID, opelogDomainId));
		try {

			Domain domain = domainRepository.findOne(opelogDomainId);
			itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_NAME, domain.getDomainName()));
		} catch (SrdmDataNotFoundException | SrdmDataAccessException e) {
			// 操作ログ記録時のdomainName取得エラー
			logger.error("[Operation Log write] domain name get error. domainId[{}]", opelogDomainId, e);
		}
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_ACCOUNT_NAME,
				opeLogAccountName.subSequence(0, opeLogAccountName.length() - 1).toString()));

		// 対象にログインアカウントが含まれるかのチェック
		for (String accountId : accountIdList) {
			if (loginAccountId.equals(accountId)) {
				logger.warn("specified accountId is login accountId. sessionId[{}]", accountId);
				// 操作ログ（失敗：ログインアカウントが含まれる）
				opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_DELETE_ACCOUNT,
						SrdmLogConstants.OPELOG_CODE_DEL_ACNT_OWN, itemList);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023);
			}
		}

		// アカウント情報削除
		try {
			accountRepository.updateAccountStatusToDeleted(accountIdList);
			if (accountListForRoleDelete.isEmpty() == false) {
				for (Account accountForRoleDelete : accountListForRoleDelete) {
					List<String> roleIdList = new ArrayList<String>();
					roleIdList.add(accountForRoleDelete.getRoleId());
					roleRepository.delete(accountForRoleDelete.getDomainId(), roleIdList);
				}
			}
			// 操作ログ記録（成功）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_DELETE_ACCOUNT,
					SrdmLogConstants.OPELOG_CODE_NORMAL, itemList);
		} catch (SrdmDataAccessException e) {
			// 操作ログ記録（失敗：DBアクセスエラー）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_DELETE_ACCOUNT,
					SrdmLogConstants.OPELOG_CODE_DEL_ACNT_FAILD, itemList);
			throw e;
		}
	}

	/**
	 * アカウントロック解除
	 *
	 * @throws SrdmGeneralException
	 */
	@Override
	public void accountUnlock(String sessionId, List<String> accountIdList)
			throws SrdmDataAccessException, SrdmDataNotFoundException, SrdmGeneralException {

		// sessionIdからdomeinIdとaccountIdを取得
		String loginDomainId = srdmSessionRepository.getDomainId(sessionId);
		if (loginDomainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "Unable to delete the account.");
		}
		String loginAccountId = srdmSessionRepository.getAccountId(sessionId);
		if (loginAccountId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login accountId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("domainId", "", "Unable to Unlock the account.");
		}

		// 操作ログ用
		String opelogDomainId = "";
		StringBuilder opeLogAccountName = new StringBuilder();

		// 自ドメインアカウント管理権限有無
		boolean hasAccountOfOwnDomain = srdmSessionRepository.hasPermission(sessionId,
				SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN);

		// 認証エラー(5回連続)によるロック中アカウント
		List<String> authErrorLockedAccountIdList = new ArrayList<String>();
		// ユーザ操作によるロック中アカウント
		List<String> manualLockedAccountIdList = new ArrayList<String>();
		// アカウントの存在チェック
		for (String accountId : accountIdList) {

			// 取得したデータが指定ドメインが配下にあるのかのチェック
			Account account;
			try {
				account = accountRepository.findOne(accountId);
			} catch (SrdmDataAccessException | SrdmDataNotFoundException e) {
				logger.warn("account not found. accountId[{}]", accountId);
				// 操作ログ（失敗：指定アカウントなし）
				opelogWriteService.writeOperationLog(sessionId,
						SrdmLogConstants.OPELOG_OPERATION_CHANGE_ACCOUNT_LOCK_STATE,
						SrdmLogConstants.OPELOG_CODE_CHG_LOCK_STATE_NOT_FOUND, new ArrayList<LogItem>());
				throw e;
			}

			boolean isUnderDomain = domainRepository.isUnderDomain(loginDomainId, account.getDomainId());
			if (isUnderDomain == false) {
				logger.warn("domain is not included. accountId[{}]", accountId);
				// 操作ログ（失敗：指定アカウントなし）
				opelogWriteService.writeOperationLog(sessionId,
						SrdmLogConstants.OPELOG_OPERATION_CHANGE_ACCOUNT_LOCK_STATE,
						SrdmLogConstants.OPELOG_CODE_CHG_LOCK_STATE_NOT_FOUND, new ArrayList<LogItem>());
				throw new SrdmDataNotFoundException("accountId", "", "Unable to Unlock the account.");
			}

			// 操作ログ用データ保持
			/**
			 * domainIdは、リストの最後のaccountの属するdomainIdを保持。
			 * 現状のUIは、アカウント削除時、domainをまたがった指定が出来ないため、問題なし。
			 * 将来、domainをまたがった指定を可能にする場合、操作ログの内容を見直す必要あり。
			 */
			opelogDomainId = account.getDomainId();
			opeLogAccountName.append(account.getAccountName()).append(",");

			// 自ドメインアカウント管理権限有無によるチェック
			if (hasAccountOfOwnDomain == false) {
				// 編集対象がログインドメインのログインアカウント以外
				if (loginDomainId.equals(account.getDomainId()) == true) {
					logger.warn("You do not have sufficient privileges. permission[{}]",
							SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN);
					throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023);
				}
			}

			// 解除するアカウントを振り分け
			String accountStatus = account.getAccountStatus();
			if (accountStatus.equals(SrdmConstants.ACCOUNT_STATUS_LOCKED)) {
				// 認証エラーによるロックからの解除
				authErrorLockedAccountIdList.add(accountId);
			} else if (accountStatus.equals(SrdmConstants.ACCOUNT_STATUS_LOCKED)) {
				// ユーザ操作によるロックからの解除
				manualLockedAccountIdList.add(accountId);
			} else {
				// 実際にはないはず
				authErrorLockedAccountIdList.add(accountId);
			}
		}
		// 操作ログ用情報
		List<LogItem> itemList = new ArrayList<LogItem>();
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_ID, opelogDomainId));
		try {

			Domain domain = domainRepository.findOne(opelogDomainId);
			itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_NAME, domain.getDomainName()));
		} catch (SrdmDataNotFoundException | SrdmDataAccessException e) {
			// 操作ログ記録時のdomainName取得エラー
			logger.error("[Operation Log write] domain name get error. domainId[{}]", opelogDomainId, e);
		}
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_ACCOUNT_NAME,
				opeLogAccountName.subSequence(0, opeLogAccountName.length() - 1).toString()));
		itemList.add(
				new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_LOCK_STATE, SrdmLogConstants.OPELOG_ITEM_VALUE_UNLOCK));
		itemList.add(
				new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_CHANGE_FACT, SrdmLogConstants.OPELOG_IETM_VALUE_NONE));

		// ログインアカウントチェック
		for (String accountId : accountIdList) {

			if (loginAccountId.equals(accountId)) {
				logger.warn("specified accountId is login accountId. sessionId[{}]", accountId);
				// 操作ログ（失敗：指定されたアカウントがログインアカウント）
				opelogWriteService.writeOperationLog(sessionId,
						SrdmLogConstants.OPELOG_OPERATION_CHANGE_ACCOUNT_LOCK_STATE,
						SrdmLogConstants.OPELOG_CODE_CHG_LOCK_STATE_OWN, itemList);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023);
			}
		}

		try {
			if (authErrorLockedAccountIdList.isEmpty() == false) {
				accountRepository.clearAuthErrorInfo(authErrorLockedAccountIdList);
			}
			// ユーザ操作によるロック状態の解除
			if (manualLockedAccountIdList.isEmpty() == false) {
				accountRepository.clearManualLock(manualLockedAccountIdList);
			}
			// 操作ログ記録（成功）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_CHANGE_ACCOUNT_LOCK_STATE,
					SrdmLogConstants.OPELOG_CODE_NORMAL, itemList);
		} catch (SrdmDataAccessException e) {
			// 操作ログ記録（失敗：DBアクセスエラー）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_CHANGE_ACCOUNT_LOCK_STATE,
					SrdmLogConstants.OPELOG_CODE_CHG_LOCK_STATE_FAILD, itemList);
			throw e;
		}
	}

	/**
	 * アカウントロック
	 *
	 * @throws SrdmGeneralException
	 */
	@Override
	public void accountLock(String sessionId, List<String> accountIdList)
			throws SrdmDataAccessException, SrdmDataNotFoundException, SrdmGeneralException {

		// sessionIdからdomeinIdとaccountIdを取得
		String loginDomainId = srdmSessionRepository.getDomainId(sessionId);
		if (loginDomainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "Unable to delete the account.");
		}
		String loginAccountId = srdmSessionRepository.getAccountId(sessionId);
		if (loginAccountId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login accountId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("domainId", "", "Unable to Unlock the account.");
		}

		// 操作ログ用
		String opelogDomainId = "";
		StringBuilder opeLogAccountName = new StringBuilder();
		Map<String, Account> accountMap = new HashMap<String, Account>();

		// 自ドメインアカウント管理権限有無
		boolean hasAccountOfOwnDomain = srdmSessionRepository.hasPermission(sessionId,
				SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN);

		// アカウントの存在チェック
		for (String accountId : accountIdList) {

			// 取得したデータが指定ドメインが配下にあるのかのチェック
			Account account;
			try {
				account = accountRepository.findOne(accountId);
				accountMap.put(accountId, account);
			} catch (SrdmDataAccessException | SrdmDataNotFoundException e) {
				logger.warn("account not found. accountId[{}]", accountId);
				// 操作ログ（失敗：指定アカウントなし）
				opelogWriteService.writeOperationLog(sessionId,
						SrdmLogConstants.OPELOG_OPERATION_CHANGE_ACCOUNT_LOCK_STATE,
						SrdmLogConstants.OPELOG_CODE_CHG_LOCK_STATE_NOT_FOUND, new ArrayList<LogItem>());
				throw e;
			}

			boolean isUnderDomain = domainRepository.isUnderDomain(loginDomainId, account.getDomainId());
			if (isUnderDomain == false) {
				logger.warn("domain is not included. accountId[{}]", accountId);
				// 操作ログ（失敗：指定アカウントなし）
				opelogWriteService.writeOperationLog(sessionId,
						SrdmLogConstants.OPELOG_OPERATION_CHANGE_ACCOUNT_LOCK_STATE,
						SrdmLogConstants.OPELOG_CODE_CHG_LOCK_STATE_NOT_FOUND, new ArrayList<LogItem>());
				throw new SrdmDataNotFoundException("accountId", "", "Unable to Unlock the account.");
			}

			// 操作ログ用データ保持
			/**
			 * domainIdは、リストの最後のaccountの属するdomainIdを保持。
			 * 現状のUIは、アカウント削除時、domainをまたがった指定が出来ないため、問題なし。
			 * 将来、domainをまたがった指定を可能にする場合、操作ログの内容を見直す必要あり。
			 */
			opelogDomainId = account.getDomainId();
			opeLogAccountName.append(account.getAccountName()).append(",");

			// 自ドメインアカウント管理権限有無によるチェック
			if (hasAccountOfOwnDomain == false) {
				// 編集対象がログインドメインのログインアカウント以外
				if (loginDomainId.equals(account.getDomainId()) == true) {
					logger.warn("You do not have sufficient privileges. permission[{}]",
							SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN);
					throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023);
				}
			}

		}

		// 操作ログ用情報
		List<LogItem> itemList = new ArrayList<LogItem>();
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_ID, opelogDomainId));
		try {

			Domain domain = domainRepository.findOne(opelogDomainId);
			itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_NAME, domain.getDomainName()));
		} catch (SrdmDataNotFoundException | SrdmDataAccessException e) {
			// 操作ログ記録時のdomainName取得エラー
			logger.error("[Operation Log write] domain name get error. domainId[{}]", opelogDomainId, e);
		}
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_ACCOUNT_NAME,
				opeLogAccountName.subSequence(0, opeLogAccountName.length() - 1).toString()));
		itemList.add(
				new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_LOCK_STATE, SrdmLogConstants.OPELOG_ITEM_VALUE_LOCK));
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_CHANGE_FACT,
				SrdmLogConstants.OPELOG_ITEM_VALUE_USER_OPE));

		// ログインアカウントチェック
		for (String accountId : accountIdList) {

			if (loginAccountId.equals(accountId)) {
				logger.warn("specified accountId is login accountId. sessionId[{}]", accountId);
				// 操作ログ（失敗：指定されたアカウントがログインアカウント）
				opelogWriteService.writeOperationLog(sessionId,
						SrdmLogConstants.OPELOG_OPERATION_CHANGE_ACCOUNT_LOCK_STATE,
						SrdmLogConstants.OPELOG_CODE_CHG_LOCK_STATE_OWN, itemList);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023);
			}

			// 削除不可アカウントのチェック
			Account account = accountMap.get(accountId);
			if (account != null && account.isPermanentAccount()) {
				opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_CHANGE_ACCOUNT_LOCK_STATE,
						SrdmLogConstants.OPELOG_CODE_CHG_LOCK_STATE_CANTDEL_ACNT, itemList);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023);
			}
		}

		try {
			accountRepository.updateAccountStatusToManualLock(accountIdList);
			// 操作ログ記録（成功）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_CHANGE_ACCOUNT_LOCK_STATE,
					SrdmLogConstants.OPELOG_CODE_NORMAL, itemList);
		} catch (SrdmDataAccessException e) {
			// 操作ログ記録（失敗：DBアクセスエラー）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_CHANGE_ACCOUNT_LOCK_STATE,
					SrdmLogConstants.OPELOG_CODE_CHG_LOCK_STATE_FAILD, itemList);
			throw e;
		}
	}

}
