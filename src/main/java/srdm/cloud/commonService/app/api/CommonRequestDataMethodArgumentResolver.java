package srdm.cloud.commonService.app.api;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import srdm.cloud.commonService.app.bean.CommonRequestData;
import srdm.common.util.CookeUtil;

public class CommonRequestDataMethodArgumentResolver implements HandlerMethodArgumentResolver {

	private static final Logger logger = LoggerFactory.getLogger(CommonRequestDataMethodArgumentResolver.class);

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return CommonRequestData.class.isAssignableFrom(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		// cookieからsessionIdを取得
		Cookie[] cookies = request.getCookies();
		String sessionId = CookeUtil.getSessionId(cookies);
		if(StringUtils.isEmpty(sessionId)){
			// Interceptorによるチェック完了後に呼び出されるため、cookieからsessionIdが取得出来ないということはない。
			logger.warn("SessionId Not found.");
		}
		CommonRequestData commonRequestData = new CommonRequestData();
		commonRequestData.setSessionId(sessionId);
		return commonRequestData;
	}

}
