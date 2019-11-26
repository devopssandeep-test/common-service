package srdm.cloud.commonService.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import srdm.common.constant.SrdmConstants;
import srdm.common.exception.SrdmGeneralException;

/**
 * IP Addressチェック用Interceptor
 *
 */
public class IpAddressCheckInterceptor extends HandlerInterceptorAdapter {

	private static final Logger logger = LoggerFactory.getLogger(IpAddressCheckInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		String remoteAddr = request.getRemoteAddr();
		String localAddr = request.getLocalAddr();
		if(SrdmConstants.LOCAL_HOST_ADDRESS.equals(remoteAddr) == false) {
			logger.warn("Execute IP Address check : NG.[Can not be accsessed from remote host. IP-Address[{}]]",remoteAddr);
			throw new SrdmGeneralException(SrdmConstants.ERROR_E0057, SrdmConstants.ERROR_MESSAGE_E0057);
		}

		logger.debug("Execute IP Address check : ok.[remoteAddr[{}], localAddr[{}]]",remoteAddr,localAddr);

		return super.preHandle(request, response, handler);
	}

}
