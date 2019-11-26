package srdm.cloud.commonService.interceptor;

import java.lang.reflect.Method;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * API開始／終了ログ出力用Interceptor
 */
public class ApiLogInterceptor extends HandlerInterceptorAdapter {

	private static final Logger logger = LoggerFactory.getLogger(ApiLogInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if(logger.isInfoEnabled()) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			Method method = handlerMethod.getMethod();
			logger.info("[Start Controller] {}.{}",
					method.getDeclaringClass().getSimpleName(),method.getName());
		}
		if(logger.isDebugEnabled() == true) {
			logger.debug("request path][{}]",request.getRequestURL());
			Cookie[] cookies = request.getCookies();
			if(cookies != null) {
				for(Cookie cookie : cookies) {
					logger.debug("cookie key[{}] value[{}]",cookie.getName(),cookie.getValue());
				}
			} else {
				logger.debug("cookie is null.");
			}
		}
		return super.preHandle(request, response, handler);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		if(logger.isInfoEnabled()) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			Method method = handlerMethod.getMethod();
			logger.info("[End Controller] {}.{}",
					method.getDeclaringClass().getSimpleName(),method.getName());
		}
	}

}
