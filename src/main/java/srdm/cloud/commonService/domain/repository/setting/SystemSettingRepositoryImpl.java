package srdm.cloud.commonService.domain.repository.setting;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.MongoQueryException;
import com.srdm.mongodb.constants.GlobalStrings;
import com.srdm.mongodb.service.IDBRequest;
import com.srdm.mongodb.serviceImpl.DBRequest;

import srdm.cloud.commonService.domain.model.EditNetworkSetting;
import srdm.cloud.commonService.domain.model.EditSmtpSetting;
import srdm.cloud.commonService.domain.model.NetworkSetting;
import srdm.cloud.commonService.domain.model.SmtpSetting;
import srdm.cloud.commonService.domain.model.SystemSettingNetwork;
import srdm.cloud.commonService.util.CryptoUtil;
import srdm.common.constant.SrdmConstants;
import srdm.common.exception.SrdmDataAccessException;

@Repository
public class SystemSettingRepositoryImpl implements SystemSettingRepository {

	private static final Logger logger = LoggerFactory.getLogger(SystemSettingRepositoryImpl.class);
	String DB_SMTP_SETTINGS_PATH = "systemSettings.smtpSetting";
	String DB_NETWORK_SETTINGS_PATH = "systemSettings.networkSetting";

	@Autowired
	CryptoUtil cryptoUtil;

	/**
	 * SMTP設定取得
	 */
	@Override
	public SmtpSetting getSmtpSetting() throws SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();
		
		BasicDBObject searchCondition = new BasicDBObject();
		searchCondition.append("systemSettings.smtpSetting", new BasicDBObject("$exists", true));

		// Form DB request query
		BasicDBObject dbOutputObj = new BasicDBObject();
		dbOutputObj.append("_id", 0).append(DB_SMTP_SETTINGS_PATH, 1);
		SmtpSetting smtpSetting = null;

		try {
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.IPAU_SYSTEM_SETTINGS_COLLECTION, 0, 0, searchCondition,
					dbOutputObj, new BasicDBObject());
			if (listResult.size() > 0) {
				ObjectMapper mapper = new ObjectMapper();
				JSONArray jsonarray = new JSONArray(listResult.toString());
				String smtpsettingsInfo = jsonarray.getJSONObject(0).getJSONObject("systemSettings")
						.getJSONObject("smtpSetting").toString();

				smtpSetting = mapper.readValue(smtpsettingsInfo, SmtpSetting.class);

			} else {
				// SMTP設定取得失敗
				logger.warn("[getSmtpSetting] SMTP Setting Get error.");
				throw new SrdmDataAccessException("SMTP Setting Get error.");
			}
		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[getSmtpSetting] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		}  catch (MongoException e) {
			logger.error("[getSmtpSetting] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[getSmtpSetting] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		return smtpSetting;
	}

	/**
	 * SMTP設定更新
	 */
	@Override
	public void updateSmtpSetting(EditSmtpSetting smtpSetting) throws SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();
		
		
		BasicDBObject searchCondition = new BasicDBObject("systemSettings.smtpSetting",
				new BasicDBObject("$exists", true));

		// Form DB request query
		BasicDBObject dbOutputObj = new BasicDBObject();
		dbOutputObj.append("_id", 0);

		BasicDBObject updateFields = new BasicDBObject();
		updateFields.append("systemSettings.smtpSetting.host", smtpSetting.getSmtpHost());
		updateFields.append("systemSettings.smtpSetting.port", smtpSetting.getSmtpPort());
		updateFields.append("systemSettings.smtpSetting.useAuth", smtpSetting.isUseAuth());
		updateFields.append("systemSettings.smtpSetting.userName", smtpSetting.getUserName());

		if (smtpSetting.isPwdChgFlag() == true) {
			if (smtpSetting.getPassword().isEmpty() == true) {
				// パスワードが空の場合
				updateFields.append("systemSettings.smtpSetting.password", "");
			} else {
				try {
					updateFields.append("systemSettings.smtpSetting.password", cryptoUtil.encrypt(smtpSetting.getPassword()));
				} catch (GeneralSecurityException e) {
					logger.error("[updateSmtpSetting] Password encrypt error.", e);
					throw new SrdmDataAccessException("Password encrypt error.", e);
				}
			}
		}
		updateFields.append("systemSettings.smtpSetting.fromAddress", smtpSetting.getFromAddress());
		updateFields.append("systemSettings.smtpSetting.useSsl", smtpSetting.isUseSSL());

		BasicDBObject setQueryObj = new BasicDBObject();
		setQueryObj.append(GlobalStrings.OPERATOR_SET, updateFields);
		Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
		collectionsMap.put(GlobalStrings.IPAU_SYSTEM_SETTINGS_COLLECTION, setQueryObj);
		try {
			dbRequest.updateDB(searchCondition, collectionsMap, false, false);

		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[updateSmtpSetting] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[updateSmtpSetting] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[updateSmtpSetting] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}
	}

	/**
	 * ネットワーク設定取得
	 */
	@Override
	public NetworkSetting getNetworkSetting() throws SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();
		
		BasicDBObject searchCondition = new BasicDBObject();
		searchCondition.append("systemSettings", new BasicDBObject("$exists", true));

		// Form DB request query
		BasicDBObject dbOutputObj = new BasicDBObject();
		dbOutputObj.append("_id", 0).append("systemSettings", 1);
		NetworkSetting networkSetting = new NetworkSetting();

		try {
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.IPAU_SYSTEM_SETTINGS_COLLECTION, 0, 0, searchCondition,
					dbOutputObj, new BasicDBObject());
			if (listResult.size() > 0) {				
				JSONArray jsonarray = new JSONArray(listResult.toString());				
				
				String ipAddressEnable = jsonarray.getJSONObject(0).getJSONObject("systemSettings")
						.getJSONObject("networkSetting").getJSONObject("ipAddress").getString("enablePublic");
				networkSetting.setIpAddressEnable(ipAddressEnable);

				String ipAddress = jsonarray.getJSONObject(0).getJSONObject("systemSettings")
				.getJSONObject("networkSetting").getJSONObject("ipAddress").getString("publicIp");
				networkSetting.setIpAddress(ipAddress);

				String httpPortEnable = jsonarray.getJSONObject(0).getJSONObject("systemSettings")
				.getJSONObject("networkSetting").getJSONObject("httpPort").getString("enablePublic");
				networkSetting.setHttpPortEnable(httpPortEnable);
				
				String httpPort= jsonarray.getJSONObject(0).getJSONObject("systemSettings")
						.getJSONObject("networkSetting").getJSONObject("httpPort").getString("publicPort");
				networkSetting.setHttpPort(httpPort);
				
				String httpsPortEnable= jsonarray.getJSONObject(0).getJSONObject("systemSettings")
						.getJSONObject("networkSetting").getJSONObject("httpsPort").getString("enablePublic");
				networkSetting.setHttpsPortEnable(httpsPortEnable);

				String httpsPort= jsonarray.getJSONObject(0).getJSONObject("systemSettings")
						.getJSONObject("networkSetting").getJSONObject("httpsPort").getString("publicPort");
				networkSetting.setHttpsPort(httpsPort);

				String tunnelPortEnable= jsonarray.getJSONObject(0).getJSONObject("systemSettings")
						.getJSONObject("rspSettings").getJSONObject("tunnelPort").getString("enablePublic");
				networkSetting.setTunnelPortEnable(tunnelPortEnable);

				String tunnelPort= jsonarray.getJSONObject(0).getJSONObject("systemSettings")
						.getJSONObject("rspSettings").getJSONObject("tunnelPort").getString("publicPort");
				networkSetting.setTunnelPort(tunnelPort);

				

			} else {
				// Network設定取得失敗
				logger.warn("[getNetworkSetting] Network Setting Get error.");
				throw new SrdmDataAccessException("Network Setting Get error.");
			}
		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[getNetworkSetting] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[getNetworkSetting] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[getNetworkSetting] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		return networkSetting;
	}

	/**
	 * ネットワーク設定更新
	 */
	@Override
	public void updateNetworkSetting(EditNetworkSetting networkSetting) throws SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();
		
		BasicDBObject searchCondition = new BasicDBObject();
		searchCondition.append("systemSettings", new BasicDBObject("$exists", true));


		// Form DB request query
		BasicDBObject dbOutputObj = new BasicDBObject();
		dbOutputObj.append("_id", 0);

		BasicDBObject updateFields = new BasicDBObject();

		if (networkSetting.isCloudServicePermission() == true) {
			updateFields.append("systemSettings.rspSettings.tunnelPort.enablePublic",
					networkSetting.getTunnelPortEnable());
			updateFields.append("systemSettings.rspSettings.tunnelPort.publicPort", networkSetting.getTunnelPort());

		}

		updateFields.append("systemSettings.networkSetting.ipAddress.enablePublic",
				networkSetting.getIpAddressEnable());
		updateFields.append("systemSettings.networkSetting.ipAddress.publicIp", networkSetting.getIpAddress());
		updateFields.append("systemSettings.networkSetting.httpPort.enablePublic", networkSetting.getHttpPortEnable());
		updateFields.append("systemSettings.networkSetting.httpPort.publicPort", networkSetting.getHttpPort());
		updateFields.append("systemSettings.networkSetting.httpsPort.enablePublic",
				networkSetting.getHttpsPortEnable());
		updateFields.append("systemSettings.networkSetting.httpsPort.publicPort", networkSetting.getHttpsPort());

		BasicDBObject setQueryObj = new BasicDBObject();
		setQueryObj.append(GlobalStrings.OPERATOR_SET, updateFields);
		Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
		collectionsMap.put(GlobalStrings.IPAU_SYSTEM_SETTINGS_COLLECTION, setQueryObj);
		try {
			dbRequest.updateDB(searchCondition, collectionsMap, false, false);

		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[updateNetworkSetting] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[updateNetworkSetting] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[updateNetworkSetting] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}
	}

	@Override
	public SystemSettingNetwork getSystemSettingNetwork() throws SrdmDataAccessException {
		
		// DB object creation
		IDBRequest dbRequest = new DBRequest();
		

		// Form DB request query
		
		BasicDBObject searchCondition = new BasicDBObject();
		searchCondition.append("systemSettings.networkSetting", new BasicDBObject("$exists", true));
		
		BasicDBObject dbOutputObj = new BasicDBObject();
		dbOutputObj.append("_id", 0).append(DB_NETWORK_SETTINGS_PATH, 1)
				.append("systemSettings.networkSetting.ipAddress.enablePublic", 1)
				.append("systemSettings.networkSetting.ipAddress.publicIp", 1)
				.append("systemSettings.networkSetting.ipAddress.privateIp", 1)

				.append("systemSettings.networkSetting.httpPort.enablePublic", 1)
				.append("systemSettings.networkSetting.httpPort.publicPort", 1)
				.append("systemSettings.networkSetting.httpPort.privatePort", 1)

				.append("systemSettings.networkSetting.httpsPort.enablePublic", 1)
				.append("systemSettings.networkSetting.httpsPort.publicPort", 1)
				.append("systemSettings.networkSetting.httpsPort.privatePort", 1)

				.append("systemSettings.networkSetting.enableSsl", 1);

		SystemSettingNetwork networkSetting = null;
		try {
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.IPAU_SYSTEM_SETTINGS_COLLECTION, 0, 0, searchCondition,
					dbOutputObj, new BasicDBObject());
			if (listResult.size() > 0) {
				JSONArray jsonarray = new JSONArray(listResult.toString());
				JSONObject jsonObj = jsonarray.getJSONObject(0);
				JSONObject networkSettingObj = jsonObj.getJSONObject("systemSettings").getJSONObject("networkSetting");

				networkSetting = new SystemSettingNetwork();
				
				networkSetting.setEnablePublicIp(networkSettingObj.getJSONObject("ipAddress").getBoolean("enablePublic"));
				networkSetting.setPublicIp(networkSettingObj.getJSONObject("ipAddress").getString("publicIp"));
				networkSetting.setPrivateIp(networkSettingObj.getJSONObject("ipAddress").getString("privateIp"));

				networkSetting.setEnablePublicHttpPort(networkSettingObj.getJSONObject("httpPort").getBoolean("enablePublic"));
				networkSetting.setPublicHttpPort(networkSettingObj.getJSONObject("httpPort").getString("publicPort"));
				networkSetting.setPrivateHttpPort(networkSettingObj.getJSONObject("httpPort").getString("privatePort"));

				networkSetting.setEnablePublicHttpsPort(networkSettingObj.getJSONObject("httpsPort").getBoolean("enablePublic"));
				networkSetting.setPublicHttpsPort(networkSettingObj.getJSONObject("httpsPort").getString("publicPort"));
				networkSetting.setPrivateHttpsPort(networkSettingObj.getJSONObject("httpsPort").getString("privatePort"));

				networkSetting.setEnableSsl(networkSettingObj.getBoolean("enableSsl"));

			} else {
				// systemSetting/networkSetting取得失敗
				logger.warn("[getSystemSettingNetwork] System Setting Network Get error.");
				throw new SrdmDataAccessException("System Setting Network Get error.");
			}
		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[getSystemSettingNetwork] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[getSystemSettingNetwork] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[getSystemSettingNetwork] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		return networkSetting;
	}

}
