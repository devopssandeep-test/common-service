package srdm.cloud.commonService.util;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

import srdm.cloud.commonService.util.setting.ScheduleServerSettings;

/**
 * Rest Template用ClientHttpRequestFactoryBuilder
 *
 */
public class RequestFactoryBuilder {

	public SimpleClientHttpRequestFactory build() {

		ScheduleServerSettings sss = new ScheduleServerSettings();
		int connectTimeout = sss.getConnectTimeout();
		int readTimeout = sss.getReadTimeout();

		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(connectTimeout);
		requestFactory.setReadTimeout(readTimeout);
		return requestFactory;
	}
}
