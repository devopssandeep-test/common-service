package srdm.cloud.commonService.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import srdm.cloud.commonService.domain.repository.schedule.ScheduleRepository;
import srdm.cloud.shared.system.ServerCheckService;
import srdm.common.constant.SrdmConstants;
import srdm.common.exception.SrdmGeneralException;

public class DiskFullCheckInterceptor extends HandlerInterceptorAdapter {

	private static final Logger logger = LoggerFactory.getLogger(DiskFullCheckInterceptor.class);

	private static final String loginPath = "/account/login";

	@Autowired
	ScheduleRepository scheduleRepository;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		ServerCheckService scs = new ServerCheckService();
		int iRet = scs.checkDiskFull();
		if( iRet != 0 ){
			logger.error("[DiskFull] result=[{}], API=",iRet, request.getServletPath());
			String errorCode;
			String errorMessage;
			if( iRet == 1 ){
				// ShceduleServiceへの停止リクエスト
				logger.error("[DiskFull] ScheduleService Shutdown request.");
				scheduleRepository.shutdownScheduleService();
			}
			if (request.getServletPath().contains(loginPath) == true) {
				errorCode = SrdmConstants.ERROR_E0046;
				errorMessage = SrdmConstants.ERROR_MESSAGE_E0046;
			} else { // ログインAPI以外はセッションタイムアウトエラーを返す
				errorCode = SrdmConstants.ERROR_E0026;
				errorMessage = SrdmConstants.ERROR_MESSAGE_E0026_01;
			}
			throw new SrdmGeneralException(errorCode, errorMessage);
		}

		return super.preHandle(request, response, handler);
	}

}
