package srdm.cloud.commonService.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import srdm.cloud.commonService.app.api.CommonRequestDataMethodArgumentResolver;
import srdm.cloud.commonService.interceptor.ApiLogInterceptor;
import srdm.cloud.commonService.interceptor.ApiPermissionCheckInterceptor;
import srdm.cloud.commonService.interceptor.IpAddressCheckInterceptor;
import srdm.cloud.commonService.interceptor.MaintenanceStatusCheckInterceptor;
import srdm.cloud.commonService.interceptor.SessionIdCheckInterceptor;

@Configuration
@EnableWebMvc
@ComponentScan({
	"srdm.cloud.commonService.app.api",
	"srdm.cloud.commonService.app.bean",
	"srdm.cloud.commonService.domain.model",
	"srdm.cloud.commonService.domain.service",
	"srdm.cloud.commonService.asynchronous",
	"srdm.cloud.commonService.interceptor",
	"srdm.cloud.commonService.util",
	"srdm.cloud.commonService.validation",
	"srdm.cloud.commonService.repositoryNoDB"
})
//テスト用のリポジトリを読むために"commonService"← test/java/commonService を追加
public class TestNoDBWebMvcConfig extends WebMvcConfigurerAdapter {

	/**
	 * sessionIdチェックを実施しないAPI
	 */
	private static final String[] sessionIdCheckExcludePath = {
			"/resoueces/**",
			"/account/login/",
			"/account/getLoginStatus/",
			"/account/logout/",
			"/account/loginForSchedule/",
			"/account/logoutForSchedule/",
			"/account/loginForAgent/",
			"/account/logoutForAgent/",
			"/account/loginForOnPremisesAgent/",
			"/maintenance/getMaintenanceStatus/"
	};

	/**
	 * 権限チェックを実施しないAPI
	 */
//	private static final String[] permissionCheckExcludePath = {
//			"/resoueces/**",
//			"/account/getAccount/",
//			"/account/editAccount/",
//			"/account/login/",
//			"/account/getLoginStatus/",
//			"/account/logout/",
//			"/account/loginForSchedule/",
//			"/account/logoutForSchedule/",
//			"/account/loginForAgent/",
//			"/account/logoutForAgent/",
//			"/account/getLoginAccountInfo/",
//			"/maintenance/getMaintenanceInfo/",
//			"/maintenance/getMaintenanceStatus/",
//			"/system/getStorageStatus/"
//	};

	/**
	 * Maintenance中チェックを実施しないAPI
	 */
	private static final String[] maintenanceCheckExcludePath = {
			"/resoueces/**",
			"/account/logoutForSchedule/",
			"/account/loginForOnPremisesAgent/",
			"/maintenance/getMaintenanceStatus/"
	};

	/**
	 * Disk Fullチェックを実施しないAPI
	 */
// ビルド時のテストでは使用しない
//	private static final String[] diskFullCheckExcludePath = {
//			"/resoueces/**",
//			"/log/downloadLogFile/",
//			"/export/downloadExportData/"
//	};

	/**
	 * localhostチェックを実施するAPI
	 */
	private static final String[] localhostCheckAddPath = {
			"/account/loginForSchedule/",
			"/account/logoutForSchedule/",
			"/account/loginForOnPremisesAgent/",
			"/log/startDeleteLog/",
			"/log/startDeleteSysMgtLog/",
			"/maintenance/startOptimization/"
	};

	/**
	 * Interceptor内でInjectionを利用する場合、Interceptorの生成用メソッドを用意し、
	 * Interceptor登録時、メソッド経由で登録する必要がある。
	 */
	/**
	 * Disk Fullチェック用Interceptorの生成
	 * @return
	 */
// ビルド時のテストでは使用しない
//	@Bean
//	public DiskFullCheckInterceptor diskFullCheckInterceptor() {
//		return new DiskFullCheckInterceptor();
//	}

	/**
	 * sessionIdチェック用Interceptorの生成
	 * （Interceptor内でAutowireするためにIntorceptorの生成をBeanに登録）
	 * @return
	 */
	@Bean
	public SessionIdCheckInterceptor sessionIdCheckInterceptor() {
		return new SessionIdCheckInterceptor();
	}

	/**
	 * 権限チェック用Interceptorの生成
	 * （Interceptor内でAutowireするためにIntorceptorの生成をBeanに登録）
	 * @return
	 */
	@Bean
	public ApiPermissionCheckInterceptor apiPermissionCheckInterceptor() {
		return new ApiPermissionCheckInterceptor();
	}

	/*
	 * Interceptorの登録
	 * Interceptorは、登録順に実行される。
	 * (非 Javadoc)
	 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter#addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry)
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {

		// API開始／終了ログ出力用Interceptor
		registry.addInterceptor(new ApiLogInterceptor())
			.addPathPatterns("/**")
			.excludePathPatterns("/resoueces/**");

		registry.addInterceptor(new IpAddressCheckInterceptor())
		.addPathPatterns(localhostCheckAddPath);

		// MaintenanceStatusチェック用Interceptor
		registry.addInterceptor(new MaintenanceStatusCheckInterceptor())
			.addPathPatterns("/**")
			.excludePathPatterns(maintenanceCheckExcludePath);

		// Disk Fullチェック用Interceptor
// パラメータチェックを対象としており、
// ビルドサーバのディスクの空き容量によってエラーになる可能性があるため
// ビルド時のテストでは実行しない
//		registry.addInterceptor(diskFullCheckInterceptor())
//			.addPathPatterns("/**")
//			.excludePathPatterns(diskFullCheckExcludePath);

		// sessionIdチェック用Interceptor
		registry.addInterceptor(sessionIdCheckInterceptor())
		.addPathPatterns("/**")
		.excludePathPatterns(sessionIdCheckExcludePath);

		// 権限チェック用Interceptor
// パラメータチェックを対象とするためテストでは実行しない
//		registry.addInterceptor(apiPermissionCheckInterceptor())
//		.addPathPatterns("/**")
//		.excludePathPatterns(permissionCheckExcludePath);

}

	/*
	 * ArgumentResolverの登録
	 * (非 Javadoc)
	 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter#addArgumentResolvers(java.util.List)
	 */
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(new CommonRequestDataMethodArgumentResolver());
	}
}
