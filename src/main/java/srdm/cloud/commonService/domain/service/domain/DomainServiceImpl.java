package srdm.cloud.commonService.domain.service.domain;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import srdm.cloud.commonService.domain.model.CreateDomain;
import srdm.cloud.commonService.domain.model.Domain;
import srdm.cloud.commonService.domain.model.DomainDetail;
import srdm.cloud.commonService.domain.model.EditDomain;
import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.GetListRes;
import srdm.cloud.commonService.domain.model.LogItem;
import srdm.cloud.commonService.domain.model.MibDomainExtension;
import srdm.cloud.commonService.domain.model.SimpleDomain;
import srdm.cloud.commonService.domain.model.SimpleGroup;
import srdm.cloud.commonService.domain.model.SystemSettingNetwork;
import srdm.cloud.commonService.domain.repository.account.AccountRepository;
import srdm.cloud.commonService.domain.repository.domain.DomainRepository;
import srdm.cloud.commonService.domain.repository.domain.RoleRepository;
import srdm.cloud.commonService.domain.repository.domainExtension.FdDomainExtensionRepository;
import srdm.cloud.commonService.domain.repository.domainExtension.FssDomainExtensionRepository;
import srdm.cloud.commonService.domain.repository.domainExtension.MibDomainExtensionRepository;
import srdm.cloud.commonService.domain.repository.domainExtension.TcoDomainExtensionRepository;
import srdm.cloud.commonService.domain.repository.group.GroupRepository;
import srdm.cloud.commonService.domain.repository.schedule.ScheduleRepository;
import srdm.cloud.commonService.domain.repository.session.SrdmSessionRepository;
import srdm.cloud.commonService.domain.repository.setting.SystemSettingRepository;
import srdm.cloud.commonService.domain.service.log.OpeLogWriteService;
import srdm.common.constant.SrdmConstants;
import srdm.common.constant.SrdmLogConstants;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;
import srdm.common.exception.SrdmGeneralException;
import srdm.common.exception.SrdmParameterValidationException;

@Service
public class DomainServiceImpl implements DomainService {

	private static final Logger logger = LoggerFactory.getLogger(DomainServiceImpl.class);

	private static final String LOGIN_URL_PROTOCOL_HTTP = "http";
	private static final String LOGIN_URL_PROTOCOL_HTTPS = "https";
	private static final String LOGIN_URL_PREFIX = "/WebUI/?d=";

	private static final String EXPAND_VALUE_CHILDREN_ONLY = "childrenOnly";

	@Autowired
	DomainRepository domainRepository;

	@Autowired
	AccountRepository accountRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	MibDomainExtensionRepository mibDomainExtensionRepository;

	@Autowired
	TcoDomainExtensionRepository tcoDomainExtensionRepository;

	@Autowired
	FssDomainExtensionRepository fssDomainExtensionRepository;

	@Autowired
	FdDomainExtensionRepository fdDomainExtensionRepository;

	@Autowired
	SystemSettingRepository systemSettingRepository;

	@Autowired
	GroupRepository groupRepository;

	@Autowired
	SrdmSessionRepository srdmSessionRepository;

	@Autowired
	OpeLogWriteService opelogWriteService;

	@Autowired
	ScheduleRepository scheduleRepository;

	/**
	 * ドメイン取得（単一）
	 * 
	 * @throws SrdmDataAccessException
	 * @throws SrdmDataNotFoundException
	 */
	@Override
	public Domain getDomain(String sessionId, String domainId)
			throws SrdmDataNotFoundException, SrdmDataAccessException {

		// 指定ドメインが配下にあるのかのチェック
		String loginDomainId = srdmSessionRepository.getDomainId(sessionId);
		if (loginDomainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "Unable to get the domain permission.");
		}
		boolean isUnderDomain = domainRepository.isUnderDomain(loginDomainId, domainId);
		if (isUnderDomain == false) {
			logger.warn("domain is not included. domainId[{}]", domainId);
			throw new SrdmDataNotFoundException("domainId", "", "domain not found.");
		}

		// domain情報取得
		Domain domain = domainRepository.findOne(domainId);
		return domain;
	}

	/**
	 * ドメイン取得（getDomain用にtargetGroupId、loginURLを返す）
	 * 
	 * @throws SrdmDataAccessException
	 * @throws SrdmDataNotFoundException
	 */
	@Override
	public DomainDetail getDomainDetail(String sessionId, String domainId)
			throws SrdmDataNotFoundException, SrdmDataAccessException {

		// 指定ドメインが配下にあるのかのチェック
		String loginDomainId = srdmSessionRepository.getDomainId(sessionId);
		if (loginDomainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "Unable to get the domain permission.");
		}
		boolean isUnderDomain = domainRepository.isUnderDomain(loginDomainId, domainId);
		if (isUnderDomain == false) {
			logger.warn("domain is not included. domainId[{}]", domainId);
			throw new SrdmDataNotFoundException("domainId", "", "domain not found.");
		}

		DomainDetail domainDetail = new DomainDetail();
		// domain情報取得
		Domain domain = domainRepository.findOne(domainId);
		BeanUtils.copyProperties(domain, domainDetail);

		// targetGroupId取得
		try {
			MibDomainExtension domainExtension = mibDomainExtensionRepository.findOne(domainId);
			domainDetail.setTargetGroupId(domainExtension.getGroupId());
			domainDetail.setTargetGroupName(domainExtension.getGroupName());
		} catch (SrdmDataNotFoundException e) {
			logger.warn("domain not found in iPAUDomainExtensionDB. domainId[{}]", domainId);
			domainDetail.setTargetGroupId("");
			domainDetail.setTargetGroupName("");
		}

		// URLの生成
		SystemSettingNetwork networkSetting = systemSettingRepository.getSystemSettingNetwork();
		String host;
		if (networkSetting.isEnablePublicIp() == true) {
			host = networkSetting.getPublicIp();
		} else {
			host = networkSetting.getPrivateIp();
		}
		String httpPort;
		if (networkSetting.isEnablePublicHttpPort() == true) {
			httpPort = networkSetting.getPublicHttpPort();
		} else {
			httpPort = networkSetting.getPrivateHttpPort();
		}
		String httpsPort;
		if (networkSetting.isEnablePublicHttpsPort() == true) {
			httpsPort = networkSetting.getPublicHttpsPort();
		} else {
			httpsPort = networkSetting.getPrivateHttpsPort();
		}
		String urlPath = LOGIN_URL_PREFIX + domainId;

		// http url生成
		domainDetail.setHttpURL(getUrl(LOGIN_URL_PROTOCOL_HTTP, host, httpPort, urlPath));

		// https url生成
		if (networkSetting.isEnableSsl() == true) {
			domainDetail.setHttpsURL(getUrl(LOGIN_URL_PROTOCOL_HTTPS, host, httpsPort, urlPath));
		} else {
			domainDetail.setHttpsURL("");
		}
		return domainDetail;
	}

	/**
	 * 指定されたパラメータを元にURLEncodeされたURLを返す。
	 * 
	 * @param protocol
	 * @param host
	 * @param port
	 * @param file
	 * @return 成功時、URLを返す／失敗時、空文字を返す
	 */
	private String getUrl(String protocol, String host, String port, String file) {

		int iPort;
		try {
			iPort = Integer.parseInt(port);
		} catch (NumberFormatException e) {
			logger.error("Port Number error.port[{}]", port, e);
			return "";
		}
		String encodeUrl;
		try {
			URL url = new URL(protocol, host, iPort, file);
			logger.debug("[getUrl] url[{}]", url.toURI().toString());
			encodeUrl = URLEncoder.encode(url.toURI().toString(), SrdmConstants.SYSTEM_CHARSET_NAME);
		} catch (IOException | URISyntaxException e) {
			logger.error("getUrl error.port[{}]", port, e);
			return "";
		}
		return encodeUrl;
	}

	/**
	 * ドメイン一覧取得（parentDomainIdを指定し取得）
	 */
	@Override
	public GetListRes getDomainsByParentDomainId(String sessionId, GetListReq getListReq)
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
		if (!isUnderDomain) {
			logger.warn("domain is not included. domainId[{}]", getListReq.getKeyMap().get("domainId"));
			throw new SrdmDataNotFoundException("domainId", "", "Unable to get the domain list.");
		}

		// 指定データ取得
		String expand = getListReq.getKeyMap().get("expand");
		List<SimpleDomain> domainList;
		if (expand.equals(EXPAND_VALUE_CHILDREN_ONLY) == true) {

			// 子ドメインを取得
			domainList = domainRepository.findAllByParentDomainIdWithPagable(getListReq);
			getListRes.setList(domainList);
			getListRes.setResultCount(domainList.size());

			// Total件数取得
			long total;
			try {
				total = domainRepository.count(getListReq.getKeyMap().get("domainId"));
			} catch (SrdmDataNotFoundException e) {
				// Total件数 0件
				total = 0;
			}
			getListRes.setTotalCount(total);
		} else {

			// 指定ドメインを取得
			domainList = new ArrayList<SimpleDomain>();
			try {
				SimpleDomain simpleDomain = new SimpleDomain();
				Domain domain = domainRepository.findOne(getListReq.getKeyMap().get("domainId"));
				BeanUtils.copyProperties(domain, simpleDomain);
				domainList.add(simpleDomain);
			} catch (SrdmDataNotFoundException e) {
				logger.warn("domain not found. domainId[{}]", getListReq.getKeyMap().get("domainId"));
			}

			getListRes.setList(domainList);
			getListRes.setResultCount(domainList.size());
			getListRes.setTotalCount(domainList.size());
		}

		return getListRes;
	}

	/**
	 * Theme設定取得
	 */
	@Override
	public String getThemeSetting(String sessionId) throws SrdmDataNotFoundException, SrdmDataAccessException {

		String domainId = srdmSessionRepository.getDomainId(sessionId);
		if (domainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "UI Theme get error.");
		}

		Domain domain = domainRepository.findOne(domainId);

		return domain.getTheme();
	}

	/**
	 * Theme設定更新
	 */
	@Override
	public void setThemeSetting(String sessionId, String theme)
			throws SrdmDataNotFoundException, SrdmDataAccessException {

		String domainId = srdmSessionRepository.getDomainId(sessionId);
		if (domainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "UI Theme set error.");
		}

		domainRepository.updateTheme(domainId, theme);
	}

	/**
	 * ドメイン作成
	 */
	@Override
	public String create(String sessionId, CreateDomain createDomain) throws SrdmDataAccessException,
			SrdmGeneralException, SrdmDataNotFoundException, SrdmParameterValidationException {

		// parentDomainIdが配下、または、自身のdomainIDかをチェック
		String loginDomainId = srdmSessionRepository.getDomainId(sessionId);
		if (loginDomainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "Unable to create domain.");
		}
		boolean isUnderDomain = domainRepository.isUnderDomain(loginDomainId, createDomain.getParentDomainId());
		if (isUnderDomain == false) {
			logger.warn("domain is not included. parentDomainId[{}]", createDomain.getParentDomainId());
			throw new SrdmDataNotFoundException("parentDomainId", "", "Unable to create domain.");
		}

		// 操作ログ用情報作成
		List<LogItem> itemList = new ArrayList<LogItem>();
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_NAME, createDomain.getDomainName()));
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_PARENT_DOMAIN_ID, createDomain.getParentDomainId()));
		try {

			Domain domain = domainRepository.findOne(createDomain.getParentDomainId());
			itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_PARENT_DOMAIN_NAME, domain.getDomainName()));
		} catch (SrdmDataNotFoundException | SrdmDataAccessException e) {
			// 操作ログ記録時のdomainName取得エラー
			logger.error("[Operation Log write] domain name get error. domainId[{}]", createDomain.getParentDomainId(),
					e);
		}

		// 同一ドメイン名有無チェック
		boolean isExist = domainRepository.isExistDomainName(createDomain.getDomainName(), "");
		if (isExist == true) {
			logger.warn("Domain Name is exist.");
			// 操作ログ（失敗：同一ドメイン名あり）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_CREATE_DOMAIN,
					SrdmLogConstants.OPELOG_CODE_CRE_DMN_SAME_NAME, itemList);
			throw new SrdmGeneralException(SrdmConstants.ERROR_E0042, SrdmConstants.ERROR_MESSAGE_E0042);
		}

		Domain domain = new Domain();
		domain.setDomainName(createDomain.getDomainName());
		domain.setParentDomainId(createDomain.getParentDomainId());

		String targetGroupId = ""; // 管理対象グループID
		// アカウント管理権限の有無チェック
		boolean hasAccountPermission = srdmSessionRepository.hasPermission(sessionId, SrdmConstants.PERM_NAME_ACCOUNT);
		if (hasAccountPermission == true) {
			// targetGroupIdの存在をチェック
			String loginGroup = srdmSessionRepository.getGroupId(sessionId);
			boolean isUnderGroup = groupRepository.isUnderGroup(loginGroup, createDomain.getTargetGroupId());
			if (isUnderGroup == false) {
				logger.warn("group is not included. targetGroupIs[{}]", createDomain.getTargetGroupId());
				// 操作ログ記録（失敗：管理対象グループなし）
				opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_CREATE_DOMAIN,
						SrdmLogConstants.OPELOG_CODE_CRE_DMN_UNKNOWN_GRP, itemList);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0018, "targetGroupId", "",
						"Unable to create domain.");
			} else {
				targetGroupId = createDomain.getTargetGroupId();
			}
		}
		
		// Check whether the target group is exist or not.
		SimpleGroup group = groupRepository.findOne(targetGroupId);
		if (group.getGroupName().isEmpty()) {
			logger.warn("group does not exist. targetGroupIs[{}]", targetGroupId);
			// 操作ログ記録（失敗：管理対象グループなし）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_CREATE_DOMAIN,
					SrdmLogConstants.OPELOG_CODE_CRE_DMN_UNKNOWN_GRP, itemList);
			throw new SrdmGeneralException(SrdmConstants.ERROR_E0018, "targetGroupId", "", "Unable to create domain.");

		}
			

		String id;
		try {
			id = createDomain(domain, targetGroupId, hasAccountPermission);
			insertSchedule(sessionId, targetGroupId);
			// 操作ログ記録（成功）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_CREATE_DOMAIN,
					SrdmLogConstants.OPELOG_CODE_NORMAL, itemList);
		} catch (SrdmDataAccessException e) {
			// 操作ログ記録（失敗：アクセスエラー）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_CREATE_DOMAIN,
					SrdmLogConstants.OPELOG_CODE_CRE_DMN_FAILD, itemList);
			throw e;
		}

		return id;
	}

	// ドメイン作成（DB更新）
	private String createDomain(Domain domain, String groupId, boolean hasAccountPermission)
			throws SrdmDataAccessException {
		// cmnDomainにドメインを追加
		String id = domainRepository.create(domain);

		// domainExtensionDBに関連情報を追加
		mibDomainExtensionRepository.add(domain.getDomainId(), groupId);
		tcoDomainExtensionRepository.add(domain.getDomainId());
		fssDomainExtensionRepository.add(domain.getDomainId());
		fdDomainExtensionRepository.add(domain.getDomainId());

		return id;
	}

	/**
	 * ドメイン編集
	 */
	@Override
	public void update(String sessionId, EditDomain editDomain) throws SrdmDataAccessException,
			SrdmDataNotFoundException, SrdmGeneralException, SrdmParameterValidationException {

		// 指定ドメインが配下にあるのかのチェック
		String loginDomainId = srdmSessionRepository.getDomainId(sessionId);
		if (loginDomainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "Unable to edit domain.");
		}
		boolean isUnderDomain = domainRepository.isUnderDomain(loginDomainId, editDomain.getDomainId());
		if (isUnderDomain == false) {
			logger.warn("domain is not included. domainId[{}]", editDomain.getDomainId());
			throw new SrdmDataNotFoundException("domainId", "", "Unable to edit domain.");
		}

		// 変更対象がログインアカウントが属するドメインは、編集不可とする
		if (loginDomainId.equals(editDomain.getDomainId()) == true) {
			logger.warn("Access improper. can not change login domain. domainId[{}]", editDomain.getDomainId());
			throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023);
		}

		// 操作ログ用情報作成
		List<LogItem> itemList = new ArrayList<LogItem>();
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_ID, editDomain.getDomainId()));
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_NAME, editDomain.getDomainName()));
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_CHANGE_ITEM, editDomain.getTargetGroupId()));

		// 同一ドメイン名有無チェック
		boolean isExist = domainRepository.isExistDomainName(editDomain.getDomainName(), editDomain.getDomainId());
		if (isExist == true) {
			logger.warn("Domain Name is exist.");
			// 操作ログ（失敗：同一ドメイン名あり）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_EDIT_DOMAIN,
					SrdmLogConstants.OPELOG_CODE_EDT_DMN_SAME_NAME, itemList);
			throw new SrdmGeneralException(SrdmConstants.ERROR_E0042, SrdmConstants.ERROR_MESSAGE_E0042);
		}

		// 指定されたtargetGroupIdをチェック
		String loginGroup = srdmSessionRepository.getGroupId(sessionId);
		boolean isUnderGroup = groupRepository.isUnderGroup(loginGroup, editDomain.getTargetGroupId());
		if (isUnderGroup == false) {
			logger.warn("group is not included. targetGroupIs[{}]", editDomain.getTargetGroupId());
			// 操作ログ記録（失敗：管理対象グループなし）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_EDIT_DOMAIN,
					SrdmLogConstants.OPELOG_CODE_EDT_DMN_UNKNOWN_GRP, itemList);
			throw new SrdmGeneralException(SrdmConstants.ERROR_E0018, "targetGroupId", "", "Unable to update domain.");
		}

		// DBの更新（cmnDomainとiPAUDomainExtensionDBとiPAUScheduleDB）
		try {
			updateDomain(editDomain);
			insertSchedule(sessionId, editDomain.getTargetGroupId());
			// 操作ログ記録（成功）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_EDIT_DOMAIN,
					SrdmLogConstants.OPELOG_CODE_NORMAL, itemList);
		} catch (SrdmDataAccessException | SrdmDataNotFoundException e) {
			// 操作ログ記録（失敗：アクセスエラー）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_EDIT_DOMAIN,
					SrdmLogConstants.OPELOG_CODE_EDT_DMN_FAILD, itemList);
			throw e;
		}
	}

	// ドメイン情報更新(DB更新)
	private void updateDomain(EditDomain editDomain) throws SrdmDataAccessException, SrdmDataNotFoundException {

		// cmnDomainの更新
		domainRepository.update(editDomain);

		// iPAUDomainExtensionDBの更新
		mibDomainExtensionRepository.update(editDomain.getDomainId(), editDomain.getTargetGroupId());
	}

	// MIBスケジュールの登録（存在する場合は、追加しない）
	private void insertSchedule(String sessionId, String groupId) {

		// スケジュール有無チェック
		try {
			boolean isExists;
			isExists = scheduleRepository.isExistMibSchedule(groupId);
			if (isExists == true) {
				// スケジュールが登録済みの場合、処理せず抜ける
				return;
			}
		} catch (SrdmDataAccessException e) {
			/**
			 * スケジュールサービスへのアクセスエラーは、エラーにせず、処理を抜ける。 ドメイン編集でも本処理が行われるため。
			 */
			logger.warn("MIB schedule unknown.", e);
			return;
		}

		// MIBスケジュールを新規登録
		String domainId = srdmSessionRepository.getDomainId(sessionId);
		String accountId = srdmSessionRepository.getAccountId(sessionId);
		try {
			scheduleRepository.insertMibSchedule(groupId, domainId, accountId);
		} catch (SrdmDataAccessException e) {
			logger.error("MIB schedule regist error.", e);
		}
	}

	/**
	 * ドメイン削除
	 */
	@Override
	public void delete(String sessionId, List<String> domainIdList)
			throws SrdmDataAccessException, SrdmDataNotFoundException, SrdmGeneralException {

		// 削除対象が配下のドメインかをチェック & ログインアカウントが属するドメインが含まれていないかをチェック
		String loginDomainId = srdmSessionRepository.getDomainId(sessionId);
		StringBuilder sb = new StringBuilder();
		String parentDomainId = ""; // 操作ログ用
		if (loginDomainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "Unable to delete domain.");
		}
		for (String domainId : domainIdList) {
			if (loginDomainId.equals(domainId) == true) {
				logger.warn("Access improper. can not delete login domain. domainId[{}]", domainId);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023);
			}

			boolean isUnderDomain = domainRepository.isUnderDomain(loginDomainId, domainId);
			if (isUnderDomain == false) {
				logger.warn("domain is not included. domainId[{}]", domainId);
				// 操作ログ記録（失敗:配下に無し（削除済み））
				// 削除済みの場合、ItemListの内容を構築できない為、空とする。
				opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_DELETE_DOMAIN,
						SrdmLogConstants.OPELOG_CODE_DEL_DMN_NOT_FOUND, new ArrayList<LogItem>());
				throw new SrdmDataNotFoundException("domainId", "", "Unable to delete domain.");
			}
			// 操作ログ用データ
			Domain domain = domainRepository.findOne(domainId);
			sb.append(domain.getDomainName()).append(",");
			/**
			 * parentDomainIdを設定
			 * SRDM2.4.0のUIでは、domainの削除は、１件しか指定できない。そのため、ループ内で更新しても問題なし。
			 * 今後、UI変更で直下でなく、下位ドメインすべてから削除対象が選択できるようになった場合、変更が必要。
			 */
			parentDomainId = domain.getParentDomainId();
		}
		List<LogItem> itemList = new ArrayList<LogItem>();
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_NAME,
				sb.subSequence(0, sb.length() - 1).toString()));
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_PARENT_DOMAIN_ID, parentDomainId));
		try {

			Domain domain = domainRepository.findOne(parentDomainId);
			itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_PARENT_DOMAIN_NAME, domain.getDomainName()));
		} catch (SrdmDataNotFoundException | SrdmDataAccessException e) {
			// 操作ログ記録時のdomainName取得エラー
			logger.error("[Operation Log write] domain name get error. domainId[{}]", parentDomainId, e);
		}

		// DBから削除
		try {

			deleteDomain(domainIdList);
			// 操作ログ記録（成功）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_DELETE_DOMAIN,
					SrdmLogConstants.OPELOG_CODE_NORMAL, itemList);
		} catch (SrdmDataAccessException e) {
			// 操作ログ記録（失敗:アクセスエラー）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_DELETE_DOMAIN,
					SrdmLogConstants.OPELOG_CODE_DEL_DMN_FAILD, itemList);
			throw e;
		}
	}

	// ドメイン削除（DB更新）
	private void deleteDomain(List<String> domainIdList) throws SrdmDataAccessException {

		// ドメイン削除時、cmnOpeLog/cmnSysMgtLogを削除しない

		// cmnAccount
		accountRepository.deleteByDomainId(domainIdList);

		// 各APPのDomainExtension
		mibDomainExtensionRepository.delete(domainIdList);
		tcoDomainExtensionRepository.delete(domainIdList);
		fssDomainExtensionRepository.delete(domainIdList);
		fdDomainExtensionRepository.delete(domainIdList);

		// cmnDomain
		domainRepository.delete(domainIdList);
	}
}
