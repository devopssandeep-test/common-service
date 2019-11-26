package srdm.cloud.commonService.interceptor;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import srdm.cloud.commonService.domain.repository.session.SrdmSessionRepository;
import srdm.common.constant.SrdmConstants;
import srdm.common.exception.SrdmGeneralException;
import srdm.common.util.CookeUtil;

/**
 * API実行権限チェック用Interceptor
 *
 */
public class ApiPermissionCheckInterceptor extends HandlerInterceptorAdapter {

	private static final Logger logger = LoggerFactory.getLogger(ApiPermissionCheckInterceptor.class);

	/**
	 * 権限チェックリスト
	 * APIのURLを必要な権限チェック対象リストに追加する。
	 * ・複数の権限が必要な場合は、それぞれのリストに追加すること。
	 * ・複数の権限の内、いづれかが必要なものは、専用のリストに追加すること。
	 * ・権限チェック不要なAPIは、本Interceptorを呼び出さないようにするため、
	 *  WebMvcconfig.javaのリストに追加すること。
	 */
	/* アカウント管理権限チェック対象リスト */
	private static final String[] mCheckListAccount = {
			"/domain/getDomainList/",
			"/domain/getDomain/",
			"/domain/editDomain/",
			"/domain/getThemeSetting/",
			"/domain/setThemeSetting/",
			"/account/getAccountList/",
			"/account/createAccount/",
			"/account/deleteAccount/",
			"/account/accountUnlock/",
			"/account/getAccountPermission/",
			"/account/setEmailNortify/",
			"/account/accountLock/",
			"/role/getRoleList/",
			"/role/createRole/",
			"/role/getRole/",
			"/role/editRole/",
			"/role/deleteRole/",
			"/group/getGroupList/",
			"/log/getOperationLog/",
			"/log/deleteOperationLog/",
			"/log/getOperationLogDetail/",
			"/log/startDeleteLog/",
			"/export/exportOperationLog/",
			"/setting/getDeleteLogSchedule/",
			"/setting/setDeleteLogSchedule/"
	};

	/* ドメイン追加／削除権限チェック対象リスト */
	private static final String[] mCheckListDomain = {
			"/domain/createDomain/",
			"/domain/deleteDomain/"
	};

	/* システム管理権限チェック対象リスト */
	private static final String[] mCheckListSystem = {
			"/export/exportSystemLog/",
			"/log/getSystemLog/",
			"/log/deleteSystemLog/",
			"/log/getSystemLogDetail/",
			"/log/makeDownloadLogFile/",
			"/log/downloadLogFile/",
			"/setting/getSMTPSetting/",
			"/setting/setSMTPSetting/",
			"/setting/getDeleteLogSchedule/",
			"/setting/setDeleteLogSchedule/",
			"/setting/getDeleteSystemLogSchedule/",
			"/setting/setDeleteSystemLogSchedule/",
			"/maintenance/setMaintenanceInfo/",
			"/maintenance/getScheduledMaintenanceSettings/",
			"/maintenance/setScheduledMaintenanceSettings/",
			"/maintenance/startOptimization/",
			"/log/startDeleteLog/",
			"/log/startDeleteSysMgtLog/"
	};

	/* クラウド環境設定権限チェック対象リスト */
	private static final String[] mCheckListCloud = {
			"/setting/getRspStatus/",
			"/setting/getEnableRsp/",
			"/setting/setEnableRsp/",
			"/setting/getNetworkSetting/",
			"/setting/setNetworkSetting/"
	};

	/* システム管理権限 or アカウント管理権限権限チェック対象リスト */
	private static final String[] mCheckListSystemOrAccount = {
			"/export/downloadExportData/"
	};

	private static final String protocolSettings = "/setting/getProtocolSetting/";

	@Autowired
	SrdmSessionRepository srdmSessionRepository;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		// requestのURLを取得
		String requestPath = request.getServletPath();
		logger.debug("API permission check. servletPath[{}]", requestPath);

		// cookieからsessionIdを取得
		Cookie[] cookies = request.getCookies();
		String sessionId = CookeUtil.getSessionId(cookies);
		if(StringUtils.isEmpty(sessionId)){
			logger.warn("API Permission Check: NG.[SessionId Not found.]");
			throw new SrdmGeneralException(SrdmConstants.ERROR_E0024, SrdmConstants.ERROR_MESSAGE_E0024);
		}

		// API実行に必要権限取得
		List<String> requiredPerm = new ArrayList<String>();	// 必ず必要な権限
		List<String> eitherPerm = new ArrayList<String>();		// いずれかが必要な権限
		// アカウント管理権限
		for (String path: mCheckListAccount) {
			if (path.equalsIgnoreCase(requestPath) == true) {
				requiredPerm.add(SrdmConstants.PERM_NAME_ACCOUNT);
			}
		}

		// ドメイン追加／削除権限
		for (String path: mCheckListDomain) {
			if (path.equalsIgnoreCase(requestPath) == true) {
				requiredPerm.add(SrdmConstants.PERM_NAME_DOMAIN);
			}
		}

		// システム管理権限
		for (String path: mCheckListSystem) {
			if (path.equalsIgnoreCase(requestPath) == true) {
				requiredPerm.add(SrdmConstants.PERM_NAME_SYSTEM);
			}
		}

		// クラウド環境設定権限
		for (String path: mCheckListCloud) {
			if (path.equalsIgnoreCase(requestPath) == true) {
				requiredPerm.add(SrdmConstants.PERM_NAME_CLOUDSERVICE);
			}
		}

		// システム管理 or アカウント管理権限
		for (String path: mCheckListSystemOrAccount) {
			if (path.equalsIgnoreCase(requestPath) == true) {
				eitherPerm.add(SrdmConstants.PERM_NAME_SYSTEM);
				eitherPerm.add(SrdmConstants.PERM_NAME_ACCOUNT);
			}
		}

		if(!protocolSettings.equalsIgnoreCase(requestPath)){
		if(requiredPerm.isEmpty() == true && eitherPerm.isEmpty() == true) {
			logger.warn("API Permission Check: NG.[Api authority is not specified.]");
			throw new SrdmGeneralException(SrdmConstants.ERROR_E0024, SrdmConstants.ERROR_MESSAGE_E0024);
		} else {
			// 指定権限が全てあるかをチェック
			if(requiredPerm.isEmpty() == false) {
				for(String perm : requiredPerm) {
					boolean isPermission = srdmSessionRepository.hasPermission(sessionId, perm);
					if(isPermission == false) {
						logger.warn("API Permission Check: NG.[sessionId[{}] Don't have {} permisson.]", sessionId, perm);
						throw new SrdmGeneralException(SrdmConstants.ERROR_E0024, SrdmConstants.ERROR_MESSAGE_E0024);
					}
				}
			}
			if(eitherPerm.isEmpty() == false) {
				boolean hasPermission = false;
				for(String perm : eitherPerm) {
					boolean isPermission = srdmSessionRepository.hasPermission(sessionId, perm);
					if(isPermission == true) {
						hasPermission = true;
					}
				}
				if(hasPermission == false) {
					StringBuilder sb = new StringBuilder();
					eitherPerm.stream().forEach(p -> sb.append(p).append("or"));
					logger.warn("API Permission Check: NG.[Don't have either permisson.sessionId[{}], permission[{}]]", sessionId, sb.toString());
					throw new SrdmGeneralException(SrdmConstants.ERROR_E0024, SrdmConstants.ERROR_MESSAGE_E0024);
				}
			}
		}}

		logger.debug("API permission check. OK.");
		return super.preHandle(request, response, handler);
	}
}
