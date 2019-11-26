package srdm.cloud.commonService.app.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import srdm.cloud.commonService.util.OxmProcessor;
import srdm.cloud.commonService.util.RequestFactoryBuilder;
import srdm.cloud.commonService.util.ScheduleServerInfo;

@Configuration
@PropertySource(value = {"classpath:application.properties"})
@EnableAsync
public class AppConfig {

	/**
	 * 非同期処理用のExecutorの指定
	 */
	@Bean
	public AsyncTaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setMaxPoolSize(10);
		return executor;
	}

	/**
	 * Property Sourceの読み込み
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	/**
	 * O/X Mapping Processor
	 */
	@Bean
	public OxmProcessor getOxmProcessor() {
		OxmProcessor handler = new OxmProcessor();
		handler.setMarshaller(getJaxbMarshaller());
		handler.setUnmarshaller(getJaxbMarshaller());
		return handler;
	}

	/**
	 * Jaxb2Mashaller
	 */
	@Bean
	public Jaxb2Marshaller getJaxbMarshaller() {

		Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
		jaxb2Marshaller.setPackagesToScan("srdm.cloud.commonService.domain.model");
		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("jaxb.formatted.output",true);			// 出力結果を整形する場合は、この行を有効にする。
		map.put("jaxb.fragment", true);
		jaxb2Marshaller.setMarshallerProperties(map);
		return jaxb2Marshaller;
	}

	/**
	 * ScheduleServerInfoの読み込み
	 */
	@Bean
	public ScheduleServerInfo getScheduleServerInfo() {
		ScheduleServerInfo serverInfo = new ScheduleServerInfo();
		serverInfo.initialize();
		return serverInfo;
	}

	/**
	 * Rest Template
	 * (Timeout設定）
	 */
	@Bean
	public RestTemplate getRestTemplate() {
		RequestFactoryBuilder builder = new RequestFactoryBuilder();

		RestTemplate restTemplate = new RestTemplate(builder.build());
		return restTemplate;
	}
}
