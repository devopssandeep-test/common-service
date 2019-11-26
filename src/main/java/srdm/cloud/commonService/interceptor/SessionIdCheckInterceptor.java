package srdm.cloud.commonService.interceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import srdm.cloud.commonService.domain.repository.session.SrdmSessionRepository;
import srdm.cloud.commonService.validation.Validation;
import srdm.common.constant.SrdmConstants;
import srdm.common.exception.SrdmGeneralException;
import srdm.common.util.CookeUtil;

/**
 * sessionCheck用Interceptor
 * sessionIdの有効／無効チェックを実施。
 * Login関係以外のAPIに適用。
 */
public class SessionIdCheckInterceptor extends HandlerInterceptorAdapter {

	private static final Logger logger = LoggerFactory.getLogger(SessionIdCheckInterceptor.class);

	@Autowired
	Validation validation;

	@Autowired
	SrdmSessionRepository srdmSessionRepository;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		// cookieからsessionIdを取得
		Cookie[] cookies = request.getCookies();
		String sessionId = CookeUtil.getSessionId(cookies);
		if(StringUtils.isEmpty(sessionId)){
			logger.warn("sessionId check : error.[SessionId Not found.]");
			throw new SrdmGeneralException(SrdmConstants.ERROR_E0021, SrdmConstants.ERROR_MESSAGE_E0021);
		}

		// sessionIdのバリデーションチェック
		if(validation.sessionId(sessionId) == false) {
			logger.warn("sessionId check : error.[invalid sessionId. sessionId=[{}]]", sessionId);
			throw new SrdmGeneralException(SrdmConstants.ERROR_E0021, SrdmConstants.ERROR_MESSAGE_E0021);
		}

		// sessionの有効チェック
		String result = srdmSessionRepository.checkSessionResultAndReson(sessionId);
		if(result.isEmpty() == false) {
			logger.warn("sessionId check : error.[sessionId=[{}] reason[{}]]", sessionId, result);
			throw new SrdmGeneralException(result.substring(0, result.indexOf("/")), result.substring(result.indexOf("/") + 1));
		}

		logger.debug("sessionId check : ok.");

		return super.preHandle(request, response, handler);
	}
}
