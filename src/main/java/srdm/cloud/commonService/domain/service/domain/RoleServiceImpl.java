package srdm.cloud.commonService.domain.service.domain;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import srdm.cloud.commonService.domain.model.Account;
import srdm.cloud.commonService.domain.model.Domain;
import srdm.cloud.commonService.domain.model.EditRole;
import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.GetListRes;
import srdm.cloud.commonService.domain.model.LogItem;
import srdm.cloud.commonService.domain.model.Permission;
import srdm.cloud.commonService.domain.model.Role;
import srdm.cloud.commonService.domain.model.SimpleRole;
import srdm.cloud.commonService.domain.repository.account.AccountRepository;
import srdm.cloud.commonService.domain.repository.domain.DomainRepository;
import srdm.cloud.commonService.domain.repository.domain.RoleRepository;
import srdm.cloud.commonService.domain.repository.session.SrdmSessionRepository;
import srdm.cloud.commonService.domain.service.log.OpeLogWriteService;
import srdm.common.constant.SrdmConstants;
import srdm.common.constant.SrdmLogConstants;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;
import srdm.common.exception.SrdmGeneralException;
import srdm.common.exception.SrdmParameterValidationException;

@Service
public class RoleServiceImpl implements RoleService {

	private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

	@Autowired
	DomainRepository domainRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	AccountRepository accountRepository;

	@Autowired
	SrdmSessionRepository srdmSessionRepository;

	@Autowired
	OpeLogWriteService opelogWriteService;

	/**
	 * ロール取得（単一）
	 *
	 * @throws SrdmDataNotFoundException
	 * @throws SrdmDataAccessException
	 * @throws SrdmGeneralException
	 */
	@Override
	public Role getRole(String sessionId, String domainId, String roleId)
			throws SrdmDataNotFoundException, SrdmDataAccessException, SrdmGeneralException {

		// 指定ドメインが配下にあるのかのチェック
		String loginDomainId = srdmSessionRepository.getDomainId(sessionId);
		if (loginDomainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionid", "", "Unable to get the role.");
		}
		boolean isUnderDomain = domainRepository.isUnderDomain(loginDomainId, domainId);
		if (isUnderDomain == false) {
			logger.warn("domain is not included. domainId[{}]", domainId);
			throw new SrdmDataNotFoundException("domainId", "", "Unable to get the role.");
		}

		Role role = roleRepository.findOne(domainId, roleId);

		// 自ドメインアカウント管理権限チェック
		boolean hasAccountOfOwnDomain = srdmSessionRepository.hasPermission(sessionId,
				SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN);
		if (hasAccountOfOwnDomain == false) {
			String loginAccountId = srdmSessionRepository.getAccountId(sessionId);
			Account loginAccount = accountRepository.findOne(loginAccountId);
			// 編集対象がログインドメインのログインアカウントに紐付くロール以外
			if ((role.getRoleId().equals(loginAccount.getRoleId()) != true)
					&& (loginDomainId.equals(domainId) == true)) {
				logger.warn("You do not have sufficient privileges. permission[{}]",
						SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023);
			}
		}

		return role;
	}

	/**
	 * ロール取得（複数）
	 *
	 * @throws SrdmDataAccessException
	 * @throws SrdmDataNotFoundException
	 */
	@Override
	public GetListRes getRoles(String sessionId, GetListReq getListReq)
			throws SrdmDataAccessException, SrdmDataNotFoundException {

		GetListRes getListRes = new GetListRes();

		// 指定ドメインが配下にあるのかのチェック
		String loginDomainId = srdmSessionRepository.getDomainId(sessionId);
		if (loginDomainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "Unable to get the role list.");
		}
		boolean isUnderDomain = domainRepository.isUnderDomain(loginDomainId, getListReq.getKeyMap().get("domainId"));
		if (isUnderDomain == false) {
			logger.warn("domain is not included. domainId[{}]", getListReq.getKeyMap().get("domainId"));
			throw new SrdmDataNotFoundException("domainId", "", "Unable to get the role list.");
		}

		String loginAccountId = srdmSessionRepository.getAccountId(sessionId);
		Account loginAccount = accountRepository.findOne(loginAccountId);

		boolean hasAccountOfOwnDomain = srdmSessionRepository.hasPermission(sessionId,
				SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN);
		List<String> roleIdList = new ArrayList<String>();
		// 自ドメインのアカウント管理権限が無い場合、ログインアカウントに紐付くロール以外を除外リストに追加
		if (hasAccountOfOwnDomain == false) {
			List<Role> roleList = roleRepository.findAllByDomainId(loginDomainId); // ログインドメインのロールリストを取得
			roleList.stream().forEach(r -> roleIdList.add(r.getRoleId()));
			roleIdList.remove(loginAccount.getRoleId()); // ログインアカウントのroleIdを除く
		} else {
			roleIdList.add("");
		}
		roleIdList.add("");
		getListReq.getKeyListMap().put("ignoreRoleIdList", roleIdList);

		// 削除可否判定用のaccountIdを設定
		getListReq.getKeyMap().put("loginAccountRoleId", loginAccount.getRoleId());

		// 指定データ取得
		List<SimpleRole> roleList = roleRepository.findAllWithPagable(getListReq);
		getListRes.setList(roleList);
		getListRes.setResultCount(roleList.size());
		getListRes.setTotalCount(roleList.size());

		return getListRes;
	}

	/**
	 * ロール更新
	 *
	 * @throws SrdmDataAccessException
	 * @throws SrdmDataNotFoundException
	 * @throws SrdmParameterValidationException
	 * @throws SrdmGeneralException
	 */
	@Override
	public void update(String sessionId, EditRole editRole) throws SrdmDataAccessException, SrdmDataNotFoundException,
			SrdmParameterValidationException, SrdmGeneralException {

		// 指定ドメインが配下にあるのかのチェック
		String loginDomainId = srdmSessionRepository.getDomainId(sessionId);
		if (loginDomainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "Unable to update the role.");
		}
		boolean isUnderDomain = domainRepository.isUnderDomain(loginDomainId, editRole.getDomainId());
		if (isUnderDomain == false) {
			logger.warn("domain is not included. domainId[{}]", editRole.getDomainId());
			throw new SrdmDataNotFoundException("domainId", "", "Unable to update the role.");
		}

		// ドメイン情報取得
		String editRoleDomainId = editRole.getDomainId();
		Domain domain = domainRepository.findOne(editRoleDomainId);

		// 操作ログ用情報
		List<LogItem> itemList = new ArrayList<LogItem>();
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_ID, domain.getDomainId()));
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_NAME, domain.getDomainName()));
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_ROLE_NAME, editRole.getRoleName()));
		String changeItem;
		if ((editRole.getGrantedPermissionList().isEmpty() == false)
				|| (editRole.getDeprivedPermissionList().isEmpty() == false)) {
			changeItem = SrdmLogConstants.OPELOG_ITEM_VALUE_PERMISSION;
		} else {
			changeItem = SrdmLogConstants.OPELOG_ITEM_VALUE_OTHER;
		}
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_CHANGE_ITEM, changeItem));

		// ロールの存在チェック
		Role role = null; // grantedPermissionListに指定された権限の内、既に登録されている権限の有無チェック用
		String roleId = editRole.getRoleId();
		try {
			role = roleRepository.findOne(editRoleDomainId, roleId);
		} catch (SrdmDataAccessException | SrdmDataNotFoundException e) {
			// 操作ログ（失敗：指定ロール無し）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_EDIT_ROLE,
					SrdmLogConstants.OPELOG_CODE_EDT_ROLE_NOT_FOUND, itemList);
			throw e;
		}

		// 指定したロールのドメインがログインアカウントのドメインと同じ場合
		Boolean editIsRoleCanEdit = editRole.isRoleCanEdit();
		// 同一ドメインの場合は「isRoleCanEdit」変更不可
		if (loginDomainId.equals(editRoleDomainId)) {
			// 値を変更しようとしている
			if (editIsRoleCanEdit != role.getRoleAttribute().isRoleCanEdit()) {
				logger.warn("Illegal change of isRoleCanEdit. domainId[{}]",
						editRole.getDomainId());
				opelogWriteService.writeOperationLog(sessionId,
						SrdmLogConstants.OPELOG_OPERATION_EDIT_ROLE,
						SrdmLogConstants.OPELOG_CODE_EDT_ROLE_NOCHANGE_ATTRBT,
						itemList);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0024,
						"Illegal change of isRoleCanEdit.");
			} else if (editIsRoleCanEdit.equals("true") == true) {
				logger.warn("Contents can not be changed.[same domain]");
				opelogWriteService.writeOperationLog(sessionId,
						SrdmLogConstants.OPELOG_OPERATION_EDIT_ROLE,
						SrdmLogConstants.OPELOG_CODE_EDT_ROLE_NOCHANGE_ATTRBT,
						itemList);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0024,
						"Contents can not be changed.");
			}
		}

		// ログインアカウントのロールチェック
		String loginAccountId = srdmSessionRepository.getAccountId(sessionId);
		Account loginAccount = accountRepository.findOne(loginAccountId);
		if (loginAccount.getRoleId().equals(editRole.getRoleId()) == true) {
			logger.warn("This role is attached to login account. roleId[{}]", editRole.getRoleId());
			// 操作ログ（失敗：指定されたロールがログインアカウントのロール）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_EDIT_ROLE,
					SrdmLogConstants.OPELOG_CODE_EDT_ROLE_OWN_ROLE, itemList);
			throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023);
		}

		// 自ドメインアカウント管理権限チェック
		boolean hasAccountOfOwnDomain = srdmSessionRepository.hasPermission(sessionId,
				SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN);
		if (hasAccountOfOwnDomain == false) {
			// 編集対象のロールがログインドメインのロール且つ、ログインアカウントに紐付くロール以外
			if ((loginAccount.getRoleId().equals(editRole.getRoleId()) == false)
					&& (loginDomainId.equals(editRole.getDomainId()) == true)) {
				logger.warn("You do not have sufficient privileges. permission[{}]",
						SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023);
			}
		}
		List<String> currentPermList = new ArrayList<String>();
		role.getPermissionList().stream()
				.forEach(permission -> currentPermList.add(permission.getPermissionName()));
		// 指定された権限が全てログインアカウントが保持するロールの権限に含まれているかをチェック
		if (editRole.getGrantedPermissionList() != null && editRole.getGrantedPermissionList().isEmpty() == false) {
			for (String permissionName : editRole.getGrantedPermissionList()) {

				if (srdmSessionRepository.hasPermission(sessionId, permissionName) == false) {
					logger.warn("Don't have permission in account. Permission name[{}]", permissionName);
					logger.warn("grantedPermissionList[{}]", editRole.getGrantedPermissionList());
					// 操作ログ（失敗：指定されたロールがアカウントロールにない）
					opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_EDIT_ROLE,
							SrdmLogConstants.OPELOG_CODE_EDT_ROLE_UNKNOWN_PERM, itemList);
					throw new SrdmParameterValidationException("grantedPermissionList", "");
				}
			}

			// grantedPermissionListに指定された権限の内、既に設定されている権限を除く
			for (String permName : editRole.getGrantedPermissionList()) {
				if (currentPermList.contains(permName) == false) {
					currentPermList.add(permName);
				}
			}
			if (editRole.getDeprivedPermissionList() != null
					&& editRole.getDeprivedPermissionList().isEmpty() == false) {
				for (String permName : editRole.getDeprivedPermissionList()) {
					if (currentPermList.contains(permName) == true) {
						currentPermList.remove(permName);
					}
				}
			}
			editRole.setGrantedPermissionList(currentPermList);
			// 以下の場合は権限の変更不可
			// - 現在のisRoleCanEditがfalseからの変更がない場合
			if (!role.getRoleAttribute().isRoleCanEdit() && !editIsRoleCanEdit) {
				logger.warn("Illegal change of grantedPermissionList.");
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0024, "Illegal change of grantedPermissionList.");
			} else {
				editRole.setGrantedPermissionList(currentPermList);
			}
		} else {
			logger.info("grantedPermissionList none.");
		}
		if (editRole.getDeprivedPermissionList() != null && editRole.getDeprivedPermissionList().isEmpty() == false) {
			for (String permissionName : editRole.getDeprivedPermissionList()) {
				if (srdmSessionRepository.hasPermission(sessionId, permissionName) == false) {
					logger.warn("Don't have permission in account. Permission name[{}]", permissionName);
					logger.warn("deprivedPermissionList[{}]", editRole.getDeprivedPermissionList());
					// 操作ログ（失敗：指定されたロールがドメインACLに含まれていない）
					opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_EDIT_ROLE,
							SrdmLogConstants.OPELOG_CODE_EDT_ROLE_UNKNOWN_PERM, itemList);
					throw new SrdmParameterValidationException("deprivedPermissionList", "");
				}
			}

			// 以下の場合は権限の変更不可
			// - 現在のisRoleCanEditがfalseからの変更がない場合
			if (!role.getRoleAttribute().isRoleCanEdit() && !editIsRoleCanEdit) {
				logger.warn("Illegal change of grantedPermissionList.");
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0024,
						"Illegal change of grantedPermissionList.");
			}

			for (String permName : editRole.getDeprivedPermissionList()) {
				if (currentPermList.contains(permName) == true) {
					currentPermList.remove(permName);
				}
			}
			editRole.setGrantedPermissionList(currentPermList);

		} else {
			logger.info("deprivedPermissionList none.");
		}
		if (editRole.getSessionTimeout() != -1) { // sessionTimeoutを変更または同じ値をセットしようとしている場合
			// 以下の場合はセッションタイムアウトの変更不可
			// - 現在のisRoleCanEditがfalseからの変更がない場合
			if (!role.getRoleAttribute().isRoleCanEdit() && !editIsRoleCanEdit) {
				logger.warn("Illegal change of sessionTimeout.");
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0024, "Illegal change of sessionTimeout.");
			}
		}

		// DB update
		try {
			roleRepository.update(editRole);
			// 操作ログ記録（成功）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_EDIT_ROLE,
					SrdmLogConstants.OPELOG_CODE_NORMAL, itemList);
		} catch (SrdmDataAccessException | SrdmDataNotFoundException e) {
			// 操作ログ記録（失敗：DBアクセスエラー）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_EDIT_ROLE,
					SrdmLogConstants.OPELOG_CODE_EDT_ROLE_FAILD, itemList);
			throw e;
		}
	}

	/**
	 * ロール作成
	 *
	 * @throws SrdmDataAccessException
	 * @throws SrdmParameterValidationException
	 * @throws SrdmDataNotFoundException
	 * @throws SrdmGeneralException
	 */
	@Override
	public String create(String sessionId, String domainId, Role role) throws SrdmDataAccessException,
			SrdmParameterValidationException, SrdmDataNotFoundException, SrdmGeneralException {

		// 指定ドメインが配下にあるのかのチェック
		String loginDomainId = srdmSessionRepository.getDomainId(sessionId);
		if (loginDomainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "Unable to update the role.");
		}
		boolean isUnderDomain = domainRepository.isUnderDomain(loginDomainId, domainId);
		if (isUnderDomain == false) {
			logger.warn("domain is not included. domainId[{}]", domainId);
			throw new SrdmDataNotFoundException("domainId", "", "Unable to update the role.");
		}

		// 自ドメインアカウント管理権限チェック
		boolean hasAccountOfOwnDomain = srdmSessionRepository.hasPermission(sessionId,
				SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN);
		if (hasAccountOfOwnDomain == false) {
			// 作成先のドメインがログインアカウントのドメインかをチェック
			if (loginDomainId.equals(domainId) == true) {
				// 「自ドメインアカウント管理権限」は、変更できない為、エラーの場合でも操作ログには、記録しない。
				logger.warn("You do not have sufficient privileges. permission[{}]",
						SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023);
			}
		}

		// ドメイン情報取得
		Domain domain = domainRepository.findOne(domainId);

		// 操作ログ用情報
		List<LogItem> itemList = new ArrayList<LogItem>();
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_ID, domainId));
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_NAME, domain.getDomainName()));
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_ROLE_NAME, role.getRoleName()));

		// isRoleCanEditの指定チェック
		if (loginDomainId.equals(domainId) == true) {
			boolean isRoleCanEdit = role.getRoleAttribute().isRoleCanEdit();
			if (!isRoleCanEdit) {
				logger.warn("Invalid specified value to isRoleCanEdit. domainId[{}]",
						domainId);
				opelogWriteService.writeOperationLog(sessionId,
						SrdmLogConstants.OPELOG_OPERATION_CREATE_ROLE,
						SrdmLogConstants.OPELOG_CODE_CRE_ROLE_INVLD_ATTRBT,
						itemList);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0024,
						"Invalid specified value to isRoleCanEdit.");
			}
		}

		// 指定された権限がログインアカウントが保持するロールの権限に含まれているかをチェック
		for (Permission permission : role.getPermissionList()) {
			if (srdmSessionRepository.hasPermission(sessionId, permission.getPermissionName()) == false) {
				logger.warn("Don't have permission in account.");
				logger.warn("permissionList[{}]", role.getPermissionList());
				// 操作ログ記録（失敗：指定された権限がドメインACLにない）
				opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_CREATE_ROLE,
						SrdmLogConstants.OPELOG_CODE_CRE_ROLE_UNKNOWN_PERM, itemList);
				throw new SrdmParameterValidationException("permissionList", "");
			}
		}

		String id;
		try {
			id = roleRepository.create(domainId, role);
			// 操作ログ記録（成功）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_CREATE_ROLE,
					SrdmLogConstants.OPELOG_CODE_NORMAL, itemList);
		} catch (SrdmDataAccessException e) {
			// 操作ログ記録（失敗：DBアクセスエラー）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_CREATE_ROLE,
					SrdmLogConstants.OPELOG_CODE_CRE_ROLE_FAILD, itemList);
			throw e;
		}

		return id;
	}

	/**
	 * ロール削除
	 *
	 * @throws SrdmDataNotFoundException
	 * @throws SrdmDataAccessException
	 * @throws SrdmGeneralException
	 */
	@Override
	public void delete(String sessionId, String domainId, List<String> roleIdList)
			throws SrdmDataAccessException, SrdmDataNotFoundException, SrdmGeneralException {

		// 指定ドメインが配下にあるのかのチェック
		String loginDomainId = srdmSessionRepository.getDomainId(sessionId);
		if (loginDomainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "Unable to delete the role.");
		}
		boolean isUnderDomain = domainRepository.isUnderDomain(loginDomainId, domainId);
		if (isUnderDomain == false) {
			logger.warn("domain is not included. domainId[{}]", domainId);
			throw new SrdmDataNotFoundException("domainId", "", "Unable to get the role list.");
		}

		// 操作ログ用情報
		List<LogItem> itemList = new ArrayList<LogItem>();
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_ID, domainId));
		try {

			Domain domain = domainRepository.findOne(domainId);
			itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_NAME, domain.getDomainName()));
		} catch (SrdmDataNotFoundException | SrdmDataAccessException e) {
			// 操作ログ記録時のdomainName取得エラー
			logger.error("[Operation Log write] domain name get error. domainId[{}]", domainId, e);
		}

		String loginAccountId = srdmSessionRepository.getAccountId(sessionId);
		Account loginAccount = accountRepository.findOne(loginAccountId);
		StringBuilder sb = new StringBuilder();
		for (String roleId : roleIdList) {
			// 指定ロールの存在チェック
			Role role;
			try {
				role = roleRepository.findOne(domainId, roleId);
				sb.append(role.getRoleName()).append(",");
			} catch (SrdmDataNotFoundException e) {
				logger.error("Role not found. roleId[{}]", roleId, e);
				// 操作ログ記録（失敗：指定されたロールが存在しない）
				opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_DELETE_ROLE,
						SrdmLogConstants.OPELOG_CODE_DEL_ROLE_NOT_FOUND, itemList);
				throw e;
			}
			// 削除不可の場合
			if (role.getRoleAttribute().isPrivateRole()) {
				logger.warn("Can not delete. roleId[{}]", roleId);
				// 操作ログ（失敗：削除不可）
				opelogWriteService.writeOperationLog(sessionId,
						SrdmLogConstants.OPELOG_OPERATION_DELETE_ROLE,
						SrdmLogConstants.OPELOG_CODE_DEL_ROLE_CANTDEL_ROLE,
						itemList);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023);
			}
		}
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_ROLE_NAME,
				sb.subSequence(0, sb.length() - 1).toString()));

		// ログインアカウントのロールチェック
		for (String roleId : roleIdList) {
			if (loginAccount.getRoleId().equals(roleId) == true) {
				logger.warn("This role is attached to login account. roleId[{}]", roleId);
				// 操作ログ（失敗：指定されたロールにログインロールが含まれる）
				opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_DELETE_ROLE,
						SrdmLogConstants.OPELOG_CODE_DEL_ROLE_OWN_ROLE, itemList);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023);
			}
		}

		// 自ドメインアカウント管理権限チェック
		boolean hasAccountOfOwnDomain = srdmSessionRepository.hasPermission(sessionId,
				SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN);
		if (hasAccountOfOwnDomain == false) {
			for (String roleId : roleIdList) {
				// 編集対象のロールがログインドメインのロール且つ、ログインアカウントに紐付くロール以外
				if ((loginAccount.getRoleId().equals(roleId) == false) && (loginDomainId.equals(domainId) == true)) {
					logger.warn("You do not have sufficient privileges. permission[{}]",
							SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN);
					throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023);
				}
			}
		}

		try {
			deleteRoles(domainId, roleIdList);
			// 操作ログ記録（成功）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_DELETE_ROLE,
					SrdmLogConstants.OPELOG_CODE_NORMAL, itemList);
		} catch (SrdmDataAccessException e) {
			// 操作ログ記録（失敗：DBアクセスエラー）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_DELETE_ROLE,
					SrdmLogConstants.OPELOG_CODE_DEL_ROLE_FAILD, itemList);
			throw e;
		}
	}

	// ロール削除
	private void deleteRoles(String domainId, List<String> roleIdList) throws SrdmDataAccessException {

		// ロール削除（roleがDomain内に存在しなくてもエラーにしない）
		roleRepository.delete(domainId, roleIdList);

		// 削除したロールを使用しているアカウントのroleIdをクリア
		accountRepository.clearRoleId(domainId, roleIdList);
	}
}
