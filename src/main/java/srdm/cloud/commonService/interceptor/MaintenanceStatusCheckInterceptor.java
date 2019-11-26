package srdm.cloud.commonService.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import srdm.cloud.shared.system.MaintenanceStatus;
import srdm.common.constant.SrdmConstants;
import srdm.common.exception.SrdmGeneralException;

/**
 * メンテナンスステータスチェックインターセプター
 * （getMaintenanceStatus API以外に適用）
 *
 */
public class MaintenanceStatusCheckInterceptor extends HandlerInterceptorAdapter {

	private static final Logger logger = LoggerFactory.getLogger(MaintenanceStatusCheckInterceptor.class);

	private static final String loginPath = "/account/login";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		if (MaintenanceStatus.isInMaintenance() == true) {
			logger.error("[MAINTENANCE] status=" + MaintenanceStatus.getStatusName() + " API=" + request.getServletPath());
			String errorCode;
			String errorMessage;
			if (request.getServletPath().contains(loginPath) == true) {
				errorCode = SrdmConstants.ERROR_E0058;				// メンテナンス中
				errorMessage = SrdmConstants.ERROR_MESSAGE_E0058;
			} else { // ログインAPI以外はセッションタイムアウトエラーを返す
				errorCode = SrdmConstants.ERROR_E0026;				// タイムアウト
				errorMessage = SrdmConstants.ERROR_MESSAGE_E0026_01;
			}
			throw new SrdmGeneralException(errorCode, errorMessage);
		}
		return super.preHandle(request, response, handler);
	}
}
