package srdm.cloud.commonService.repositoryNoDB;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import srdm.cloud.commonService.domain.model.EditNetworkSetting;
import srdm.cloud.commonService.domain.repository.setting.SrdmCustomPropertiesRepository;
import srdm.common.exception.SrdmDataAccessException;

@Repository
public class TestSrdmCustomPropertiesRepositoryImpl implements SrdmCustomPropertiesRepository {

	private static final Logger logger = LoggerFactory.getLogger(TestSrdmCustomPropertiesRepositoryImpl.class);

	// srdmCustom.propertiesファイルパス
	private static final String SRDM_CUSTOM_PROPERTIES_PATH = System.getProperties().getProperty("catalina.home") + "/srdmConf/srdmCustom.properties";
	// srdmCustom.properties Key
	private static final String WEBSERVER_HTTP_ENABLE_PUBLIC = "webserver.http.enablePublic";
	private static final String WEBSERVER_HTTP_PUBLIC_PORT = "webserver.http.publicPort";
	private static final String WEBSERVER_HTTPS_ENABLE_PUBLIC = "webserver.https.enablePublic";
	private static final String WEBSERVER_HTTPS_PUBLIC_PORT = "webserver.https.publicPort";

	/**
	 * Network設定更新
	 */
	@Override
	public void updateNetworkSetting(EditNetworkSetting networkSetting) throws SrdmDataAccessException {

		try(FileInputStream in =  new FileInputStream(SRDM_CUSTOM_PROPERTIES_PATH)) {

			Properties properties = new Properties();
			properties.load(in);

			properties.put(WEBSERVER_HTTP_ENABLE_PUBLIC, networkSetting.getHttpPortEnable());
			properties.put(WEBSERVER_HTTP_PUBLIC_PORT, networkSetting.getHttpPort());
			properties.put(WEBSERVER_HTTPS_ENABLE_PUBLIC, networkSetting.getHttpsPortEnable());
			properties.put(WEBSERVER_HTTPS_PUBLIC_PORT, networkSetting.getHttpsPort());

			try(FileOutputStream out = new FileOutputStream(SRDM_CUSTOM_PROPERTIES_PATH)) {
				properties.store(out, null);
			}

		} catch (IOException e) {
			logger.error("[updateNetworkSetting] srdmCustom.properties access error",e);
			throw new SrdmDataAccessException("Properties file access error.", e);
		}
	}

}
