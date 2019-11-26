package srdm.cloud.commonService.domain.repository.rspj;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.MongoQueryException;
import com.srdm.mongodb.constants.GlobalStrings;
import com.srdm.mongodb.service.IDBRequest;
import com.srdm.mongodb.serviceImpl.DBRequest;

import srdm.cloud.commonService.domain.model.RspSetting;
import srdm.cloud.commonService.domain.model.comm.CommonResponse;
import srdm.cloud.commonService.domain.model.comm.rspj.GetSystemManagerStateCodeResponse;
import srdm.cloud.commonService.util.OxmProcessor;
import srdm.cloud.commonService.util.ScheduleServerInfo;
import srdm.common.constant.SrdmConstants;
import srdm.common.constant.SrdmConstants.RspjStatus;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmGeneralException;

@Repository
public class RspjRepositoryImpl implements RspjRepository {

	private static final Logger logger = LoggerFactory.getLogger(RspjRepositoryImpl.class);

	// RSPJ Server
	private static final String RSPJ_SERVER = "127.0.0.1";
	private static final String RSPJ_SERVER_SCHEME = "http";

	// RSPJ状態取得
	private static final String RSPJ_API_GET_RSPJ_STATE_CODE = "/RSPJManager/getSystemManagerStateCode/";
	// RSPJ開始
	private static final String RSPJ_API_START_RSPJ = "/RSPJManager/startRspj/";
	// RSPJ停止
	private static final String RSPJ_API_STOP_RSPJ = "/RSPJManager/stopRspj/";
	// network settingのロード
	private static final String REPJ_API_LOAD_NETWORK_SETTING = "/loadNetworkSetting/";

	private static final String RSPJ_CORE_SETTING_FILEPATH = System.getProperties().getProperty("catalina.home") + "/conf/RspjCoreSetting.xml";
	public static final String RSPJ_CORE_SETTING_USE_RSPJ = "/rspjCoreSetting/useRSPJ";
	String DB_RSP_SETTINGS_PATH = "systemSettings.rspSettings";
	String DB_GET_RSP_STATUS = "systemSettings.rspSettings.enableRsp";

	// TODO:暫定でRSPJ ServerのポートはScueduleServiceの設定を使用。
	@Autowired
	ScheduleServerInfo scheduleServerInfo;

	@Autowired
	RestOperations restOperations;

	@Autowired
	OxmProcessor oxmProcessor;

	@Autowired
	ResourceLoader resourceLoader;

	/**
	 * RSPJの状態取得
	 */
	@Override
	public String getRspStatus() {

		final String url = RSPJ_SERVER_SCHEME + "://" + RSPJ_SERVER + ":" + scheduleServerInfo.getPort() + RSPJ_API_GET_RSPJ_STATE_CODE;

		String rspjStatus;
		try {
			GetSystemManagerStateCodeResponse response;
			response = restOperations.postForObject(
					url,
					"",
					GetSystemManagerStateCodeResponse.class);

			logger.debug("[getRspStatus] response:[{}]",response.toString());

			if(response.getCommon().getErrorList().length > 0) {

				logger.error("[getRspStatus] RSPJManager error response.response:[{}]", response.toString());
				rspjStatus = RspjStatus.RSPJ_STATUS_CODE_S100;
			} else {
				rspjStatus = response.getSystemManagerStateCode();
			}

		} catch (RestClientException e) {
			logger.error("[getRspStatus] RspjManager access error.", e);
			rspjStatus = RspjStatus.RSPJ_STATUS_CODE_S100;
		}

		return rspjStatus;
	}

	/**
	 * RSPJの有効状態取得(DBから取得)
	 */
	@Override
	public String getEnableRsp() throws SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		BasicDBObject searchCondition = new BasicDBObject();
		searchCondition.append(DB_RSP_SETTINGS_PATH, new BasicDBObject("$exists", true));

		// Form DB request query
		BasicDBObject dbOutputObj = new BasicDBObject();
		dbOutputObj.append("_id", 0).append(DB_GET_RSP_STATUS, 1);
		RspSetting rspSetting = new RspSetting();

		try {
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.IPAU_SYSTEM_SETTINGS_COLLECTION, 0, 0,
					searchCondition, dbOutputObj, new BasicDBObject());

			if (listResult.size() != 0) {
				JSONObject jsonObj = new JSONObject(listResult.get(0).toString());
				rspSetting.setEnableRsp(
						jsonObj.getJSONObject("systemSettings").getJSONObject("rspSettings").getString("enableRsp"));
			} else {
				// SMTP設定取得失敗
				logger.warn("[getEnableRsp] RSP Setting Get error.");
				throw new SrdmDataAccessException("RSP Setting Get error.");
			}
		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[getEnableRsp] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[getEnableRsp] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[getEnableRsp] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		String result;
		if (Boolean.parseBoolean(rspSetting.getEnableRsp()) == true) {
			result = SrdmConstants.RSPJ_STATUS_ENABLE;
		} else {
			result = SrdmConstants.RSPJ_STATUS_DISABLE;
		}
		return result;
	}

	@Override
	public void setEnableRsp(String rspEnableStatus) throws SrdmDataAccessException, SrdmGeneralException {

		boolean requestValue;
		if(rspEnableStatus.equals(SrdmConstants.RSPJ_STATUS_ENABLE)) {
			requestValue = true;
		} else {
			requestValue = false;
		}
		// RSPJの有効／無効の指定をRspjCoreSetting.xmlに設定
		try {
			updateRspjCoreSettingXml(Boolean.toString(requestValue));
		} catch (SrdmDataAccessException e) {
			throw new SrdmGeneralException(SrdmConstants.ERROR_E0032, SrdmConstants.ERROR_MESSAGE_E0032, e);
		}

		// RSPJの有効／無効の指定に従い、RSPJManagerのAPIを呼び出す
		try {
			updateRspj(requestValue);
		} catch (SrdmDataAccessException e) {
			// RspjCoreSetting.xmlをリカバリ
			try {
				updateRspjCoreSettingXml(Boolean.toString(requestValue));
			} catch (SrdmDataAccessException e1) {
				logger.error("[setEnableRsp] RspjCoreSetting.xml Recovery Error",e1);
			}
			throw new SrdmGeneralException(SrdmConstants.ERROR_E0023, SrdmConstants.ERROR_MESSAGE_E0023, e);
		}

		// RSPJの有効／無効の指定をiPAUSysSettingsDBに保持
		try {

			updateDB(requestValue);
		} catch (SrdmDataAccessException e) {
			// RspjCoreSetting.xmlをリカバリ
			try {
				updateRspjCoreSettingXml(Boolean.toString(requestValue));
			} catch (SrdmDataAccessException e1) {
				logger.error("[setEnableRsp] RspjCoreSetting.xml Recovery Error",e1);
			}

			// RSPJの状態をリカバリ
			requestValue = !requestValue;
			try {

				updateRspj(requestValue);
			} catch (SrdmDataAccessException e1) {
				logger.error("[setEnableRsp] RSPJ Recovery Error",e1);
			}
			throw e;
		}

	}

	// RSPJの有効／無効設定（対DB）
	private void updateDB(boolean requestValue) throws SrdmDataAccessException {
		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		BasicDBObject searchCondition = new BasicDBObject(DB_RSP_SETTINGS_PATH, new BasicDBObject("$exists", true));

		// Form DB request query

		BasicDBObject updateFields = new BasicDBObject();
		updateFields.append(DB_GET_RSP_STATUS, requestValue);

		BasicDBObject setQueryObj = new BasicDBObject();
		setQueryObj.append(GlobalStrings.OPERATOR_SET, updateFields);
		Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
		collectionsMap.put(GlobalStrings.IPAU_SYSTEM_SETTINGS_COLLECTION, setQueryObj);
		try {
			dbRequest.updateDB(searchCondition, collectionsMap, false, false);

		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[updateDB] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		}  catch (MongoException e) {
			logger.error("[updateDB] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[updateDB] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

	}

	// RSPJの有効／無効設定（対RSPJManager）
	private void updateRspj(boolean isEnable) throws SrdmDataAccessException {

		final String baseUrl = RSPJ_SERVER_SCHEME + "://" + RSPJ_SERVER + ":" + scheduleServerInfo.getPort();
		String requestUrl;

		if(isEnable == true) {
			requestUrl = baseUrl + RSPJ_API_START_RSPJ;
		} else {
			requestUrl = baseUrl + RSPJ_API_STOP_RSPJ;
		}
		logger.debug("[updateRspj] request URL:[{}]", requestUrl);
		try {
			CommonResponse response;
			response = restOperations.postForObject(
					requestUrl,
					"",
					CommonResponse.class);
			logger.debug("[updateRspj] response:[{}]",response.toString());

			if(response.getCommon().getErrorList().length > 0) {
				logger.error("[updateRspj] error response. request URL:[{}] response:[{}]", requestUrl, response.toString());
				throw new SrdmDataAccessException("RSPJ access error.");
			}
		} catch (RestClientException e) {
			logger.error("[updateRspj] Rspj access error.", e);
			throw new SrdmDataAccessException("RSPJ access error.", e);
		}
	}

	// RSPJの有効／無効設定（対RspjCoreSetting.xml）
	private void updateRspjCoreSettingXml(String value) throws SrdmDataAccessException {

		Resource resource = resourceLoader.getResource("file://" + RSPJ_CORE_SETTING_FILEPATH);
		logger.debug("file path:[{}]", RSPJ_CORE_SETTING_FILEPATH);

		if(resource.exists() == true) {
			try {
				File settingFile = resource.getFile();
				Document document =
						DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(settingFile);
				document.setXmlStandalone(true);

				XPath xPath = XPathFactory.newInstance().newXPath();

				Node node = null;
				node = (Node) xPath.evaluate(RSPJ_CORE_SETTING_USE_RSPJ + "/text()", document, XPathConstants.NODE);
				if (node != null) {

					node.setNodeValue(value);
				} else {

					node = (Node) xPath.evaluate(RSPJ_CORE_SETTING_USE_RSPJ, document, XPathConstants.NODE);
					node.appendChild(document.createTextNode(value));
				}

				try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(settingFile))){

					TransformerFactory transFactory = TransformerFactory.newInstance();
					Transformer transformer = transFactory.newTransformer();
					transformer.transform(new DOMSource(document), new StreamResult(out));

				} catch (TransformerFactoryConfigurationError e) {
					logger.error("[updateRspjCoreSettingXml] RspjCoreSetting.xml access error.", e);
					throw new SrdmDataAccessException("RspjCoreSetting.xml access error.", e);
				}

			} catch (SAXException | IOException | ParserConfigurationException
					| XPathExpressionException | TransformerException e) {
				logger.error("[updateRspjCoreSettingXml] RspjCoreSetting.xml access error.", e);
				throw new SrdmDataAccessException("RspjCoreSetting.xml access error.", e);
			}
		} else {
			logger.error("[updateRspjCoreSettingXml RspjCoreSetting.xml not found.");
			throw new SrdmDataAccessException("RspjCoreSetting.xml not found.");
		}
	}

	/**
	 * Network設定のLoadリクエスト
	 */
	@Override
	public void loadNetworkSetting() throws SrdmDataAccessException {

		final String baseUrl = RSPJ_SERVER_SCHEME + "://" + RSPJ_SERVER + ":" + scheduleServerInfo.getPort();
		final String reqPath[] = {"/RSP","/APPRSP","/VNCRSP"};

		for(String path : reqPath) {

			String requestUrl = baseUrl + path + REPJ_API_LOAD_NETWORK_SETTING;
			logger.debug("[loadNetworkSetting] request URL:[{}]", requestUrl);

			try {
				CommonResponse response;
				response = restOperations.postForObject(
						requestUrl,
						"",
						CommonResponse.class);
				logger.debug("[loadNetworkSetting] response:[{}]",response.toString());

				if(response.getCommon().getErrorList().length > 0) {
					logger.error("loadNetworkSetting error response. request URL:[{}] response:[{}]", requestUrl, response.toString());
					throw new SrdmDataAccessException("RSPJ access error.");
				}
			} catch (RestClientException e) {
				logger.error("[loadNetworkSetting Rspj access error.", e);
				throw new SrdmDataAccessException("RSPJ access error.", e);
			}
		}
	}

}
