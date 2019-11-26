package srdm.cloud.commonService.domain.service.setting;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import srdm.cloud.commonService.domain.model.EditNetworkSetting;
import srdm.cloud.commonService.domain.model.EditSmtpSetting;
import srdm.cloud.commonService.domain.model.LogItem;
import srdm.cloud.commonService.domain.model.NetworkSetting;
import srdm.cloud.commonService.domain.model.ProtocolSetting;
import srdm.cloud.commonService.domain.model.SmtpSetting;
import srdm.cloud.commonService.domain.model.SystemSettingNetwork;
import srdm.cloud.commonService.domain.repository.rspj.RspjRepository;
import srdm.cloud.commonService.domain.repository.session.SrdmSessionRepository;
import srdm.cloud.commonService.domain.repository.setting.SrdmCustomPropertiesRepository;
import srdm.cloud.commonService.domain.repository.setting.SystemSettingRepository;
import srdm.cloud.commonService.domain.service.log.SysMgtLogWriteService;
import srdm.common.constant.SrdmConstants;
import srdm.common.constant.SrdmLogConstants;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;
import srdm.common.exception.SrdmGeneralException;

@Service
public class SystemSettingServiceImpl implements SystemSettingService {

	private static final Logger logger = LoggerFactory.getLogger(SystemSettingServiceImpl.class);
	@Autowired
	SystemSettingRepository systemSettingRepository;

	@Autowired
	SrdmSessionRepository srdmSessionRepository;

	@Autowired
	SrdmCustomPropertiesRepository srdmCustomPropertiesRepository;

	@Autowired
	SysMgtLogWriteService sysMgtLogWriteService;

	@Autowired
	RspjRepository rspjRepository;

	/**
	 * SMTP設定取得
	 */
	@Override
	public SmtpSetting getSmtpSetting() throws SrdmDataAccessException {

		SmtpSetting smtpSetting = systemSettingRepository.getSmtpSetting();
		return smtpSetting;
	}

	/**
	 * SMTP設定更新
	 */
	@Override
	public void updateSmtpSetting(String sessionId, EditSmtpSetting smtpSetting) throws SrdmDataAccessException {

		// システム管理ログ用情報
		List<LogItem> itemList = new ArrayList<LogItem>();
		String isEnable = (smtpSetting.getSmtpHost().isEmpty() ? SrdmLogConstants.SYSMGT_ITEM_VALUE_DISABLE : SrdmLogConstants.SYSMGT_ITEM_VALUE_ENABLE);
		itemList.add(new LogItem(SrdmLogConstants.SYSMGT_ITEM_NAME_SMTP_SETTING_STATE, isEnable));

		try {
			systemSettingRepository.updateSmtpSetting(smtpSetting);
			// システム管理ログ記録（正常）
			sysMgtLogWriteService.writeSystemManagementLog(
					sessionId,
					SrdmLogConstants.SYSMGT_OPERATION_SMTP_SETTING,
					SrdmLogConstants.SYSMGT_CODE_NORMAL,
					itemList);
		} catch (SrdmDataAccessException e) {
			// システム管理ログ記録（失敗:アクセスエラー）
			sysMgtLogWriteService.writeSystemManagementLog(
					sessionId,
					SrdmLogConstants.SYSMGT_OPERATION_SMTP_SETTING,
					SrdmLogConstants.SYSMGT_CODE_SMTP_SETTING_FAILD,
					itemList);
			throw e;
		}

	}

	/**
	 * Network設定取得
	 */
	@Override
	public NetworkSetting getNetworkSetting(String sessionId) throws SrdmDataAccessException {

		NetworkSetting networkSetting = systemSettingRepository.getNetworkSetting();

		// クラウド環境設定権限が無い場合、tunnelPortに関する情報をクリアする
		if(srdmSessionRepository.hasPermission(sessionId, SrdmConstants.PERM_NAME_CLOUDSERVICE) == false ) {
			networkSetting.setTunnelPortEnable("");
			networkSetting.setTunnelPort("");
		}
		return networkSetting;
	}

	/**
	 * Network設定更新
	 * @throws SrdmGeneralException
	 */
	@Override
	public void updateNetworkSetting(String sessionId, EditNetworkSetting networkSetting) throws SrdmDataAccessException, SrdmGeneralException {

		// システム管理ログ用情報
		List<LogItem> itemList = new ArrayList<LogItem>();
		// Public IP Address
		String isEnable = (Boolean.parseBoolean(
				networkSetting.getIpAddressEnable()) ? SrdmLogConstants.SYSMGT_ITEM_VALUE_ENABLE : SrdmLogConstants.SYSMGT_ITEM_VALUE_DISABLE);
		itemList.add(new LogItem(SrdmLogConstants.SYSMGT_ITEM_NAME_PUBLIC_IP_STATE, isEnable));
		// Public Http Port
		isEnable = (Boolean.parseBoolean(
				networkSetting.getHttpPortEnable()) ? SrdmLogConstants.SYSMGT_ITEM_VALUE_ENABLE : SrdmLogConstants.SYSMGT_ITEM_VALUE_DISABLE);
		itemList.add(new LogItem(SrdmLogConstants.SYSMGT_ITEM_NAME_PUBLIC_HTTP_PORT_STATE, isEnable));
		// Public Https Port
		isEnable = (Boolean.parseBoolean(
				networkSetting.getHttpsPortEnable()) ? SrdmLogConstants.SYSMGT_ITEM_VALUE_ENABLE : SrdmLogConstants.SYSMGT_ITEM_VALUE_DISABLE);
		itemList.add(new LogItem(SrdmLogConstants.SYSMGT_ITEM_NAME_PUBLIC_HTTPS_PORT_STATE, isEnable));

		// クラウド環境設定権限の有無を取得
		boolean hasCloudService = srdmSessionRepository.hasPermission(sessionId, SrdmConstants.PERM_NAME_CLOUDSERVICE);
		if(hasCloudService == true) {
			if(networkSetting.getTunnelPortEnable() == null || networkSetting.getTunnelPortEnable().isEmpty()) {
				// 必須項目エラー(E0011を返す）
				logger.warn("tunnelPortEnable is required.");
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0011, "tunnelPortEnable", "", SrdmConstants.ERROR_MESSAGE_E0011);
			}
			// Public tunnel Port
			isEnable = (Boolean.parseBoolean(
					networkSetting.getTunnelPortEnable()) ? SrdmLogConstants.SYSMGT_ITEM_VALUE_ENABLE : SrdmLogConstants.SYSMGT_ITEM_VALUE_DISABLE);
			itemList.add(new LogItem(SrdmLogConstants.SYSMGT_ITEM_NAME_PUBLIC_TUNNEL_PORT_STATE, isEnable));

		} else {
			if(networkSetting.getTunnelPortEnable() != null && networkSetting.getTunnelPortEnable().isEmpty() == false) {
				// 指定不可項目を指定(E0024を返す）
				logger.warn("You do not have sufficient privileges for tunnelPort setting.");
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0024, "tunnelPortEnable", "", SrdmConstants.ERROR_MESSAGE_E0024);
			}
		}
		networkSetting.setCloudServicePermission(hasCloudService);

		// Network設定更新
		try {
			updateNetworkSetting(networkSetting);
			// システム管理ログ記録（正常）
			sysMgtLogWriteService.writeSystemManagementLog(
					sessionId,
					SrdmLogConstants.SYSMGT_OPERATION_NETWORK_SETTING,
					SrdmLogConstants.SYSMGT_CODE_NORMAL,
					itemList);
		} catch (SrdmDataAccessException e) {
			// システム管理ログ記録（失敗:アクセスエラー）
			sysMgtLogWriteService.writeSystemManagementLog(
					sessionId,
					SrdmLogConstants.SYSMGT_OPERATION_NETWORK_SETTING,
					SrdmLogConstants.SYSMGT_CODE_NETWORK_SETTING_FAILD,
					itemList);
			throw e;
		}

	}

	// Network設定更新
	private void updateNetworkSetting(EditNetworkSetting networkSetting) throws SrdmDataAccessException {

		// Network設定の内容をDBに反映
		systemSettingRepository.updateNetworkSetting(networkSetting);

		// srdmcustom.propertiesファイルを更新
		srdmCustomPropertiesRepository.updateNetworkSetting(networkSetting);

		// RSPJに対してsrdmcustom.propertiesの再ロードを依頼
		rspjRepository.loadNetworkSetting();
	}
	
	/**
	 *
	 * @throws SrdmDataAccessException 
	 */
	@Override
	public ProtocolSetting getProtocolSetting(String sessionId)
			throws SrdmDataAccessException {
		String host = "";
		ProtocolSetting protSettings = new ProtocolSetting();
		SystemSettingNetwork networkSetting = systemSettingRepository.getSystemSettingNetwork();
		if (networkSetting.isEnablePublicIp() == true) {
			host = networkSetting.getPublicIp();
		} else {
			host = networkSetting.getPrivateIp();
		}
		String httpPort;
		if (networkSetting.isEnablePublicHttpPort() == true) {
			httpPort = networkSetting.getPublicHttpPort();
		} else {
			httpPort = networkSetting.getPrivateHttpPort();
		}
		String httpsPort;
		if (networkSetting.isEnablePublicHttpsPort() == true) {
			httpsPort = networkSetting.getPublicHttpsPort();
		} else {
			httpsPort = networkSetting.getPrivateHttpsPort();
		}

		// http port number
		protSettings.setHttp(httpPort);

		// https port number
		if (networkSetting.isEnableSsl() == true) {
			protSettings.setHttps(httpsPort);
		} else {
			protSettings.setHttps("");
		}
		
		// Server IP Address.
		protSettings.setServerIP(host);
		
		// Operating module.
		protSettings.setLandingModule("WebUI");
		

		return protSettings;
	}

}
