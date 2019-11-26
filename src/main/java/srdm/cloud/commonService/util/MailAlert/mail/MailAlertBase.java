package srdm.cloud.commonService.util.MailAlert.mail;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.srdm.mongodb.constants.GlobalStrings;
import com.srdm.mongodb.service.IDBRequest;
import com.srdm.mongodb.serviceImpl.DBRequest;

import srdm.cloud.commonService.domain.model.SmtpSetting;
import srdm.cloud.commonService.util.CryptoUtil;
import srdm.cloud.commonService.util.MailAlert.MailDestinationInfo;
import srdm.cloud.commonService.util.MailAlert.MailSenderInfo;
import srdm.cloud.commonService.util.MailAlert.MailServerInfo;
import srdm.cloud.commonService.util.optimization.ScheduleServiceUtil;

 public class MailAlertBase {
	 
	String DB_SMTP_SETTINGS_PATH = "systemSettings.smtpSetting";
	protected Logger logSimple = null;
	protected Logger logDetail = null;

	private MailServerInfo mMailServerInfo = new MailServerInfo();
	private MailSenderInfo mMailSenderInfo = new MailSenderInfo();
	CryptoUtil  cryptoUtil = new CryptoUtil();
	private MailDestinationInfo mMailDestinationInfo = null;

	public MailAlertBase(final MailDestinationInfo dest, final Logger simpleLogger, final Logger detailLogger) {
		logSimple = simpleLogger;
		logDetail = detailLogger;
		mMailDestinationInfo = dest;
		setupMailServerInfo();
		setupMailSenderInfo();
	}

	protected MailServerInfo getServerInfo() throws IOException {
		if (mMailServerInfo.getServer() != null && mMailServerInfo.getPort() != 0 && mMailServerInfo.getFromAddress() != null) {
			return mMailServerInfo;
		}
		writeSimpleErrorLog("MailServerInfo Error");
		writeDetailErrorLog("MailServerInfo Error: " + mMailServerInfo.toString());
		throw new IOException("MailServerInfo Error");
	}

	protected MailSenderInfo getSenderInfo() throws IOException {
		if (mMailSenderInfo.getServer() != null && mMailSenderInfo.getPort() != 0) {
			return mMailSenderInfo;
		}
		writeSimpleErrorLog("MailSenderInfo Error");
		writeDetailErrorLog("MailSenderInfo Error: " + mMailSenderInfo.toString());
		throw new IOException("MailSenderInfo Error");
	}

	protected MailDestinationInfo getDestinationInfo() throws IOException {
		if (mMailDestinationInfo != null && mMailDestinationInfo.getToAddress() != null) {
			return mMailDestinationInfo;
		}
		writeSimpleErrorLog("MailDestinationInfo Error");
		if (mMailDestinationInfo == null) {
			writeDetailErrorLog("MailDestinationInfo Error: null");
		} else {
			writeDetailErrorLog("MailDestinationInfo Error: " + mMailDestinationInfo.toString());
		}
		throw new IOException("MailDestinationInfo Error");
	}

	protected void writeSimpleErrorLog(final Object message) {
		if (logSimple != null) {
			logSimple.error(message);
		}
	}
	protected void writeSimpleInfoLog(final Object message) {
		if (logSimple != null) {
			logSimple.info(message);
		}
	}
	protected void writeDetailErrorLog(final Object message) {
		if (logDetail != null) {
			logDetail.error(message);
		}
	}
	protected void writeDetailInfoLog(final Object message) {
		if (logDetail != null) {
			logDetail.info(message);
		}
	}

	/**
	 * SMTP設定を取得する
	 *
	 * @return
	 */
	private boolean setupMailServerInfo() {
		boolean bRet = false;

		// DB object creation
		IDBRequest dbRequest = new DBRequest();
		
		BasicDBObject searchCondition = new BasicDBObject();
		searchCondition.append("systemSettings.smtpSetting", new BasicDBObject("$exists", true));

		// Form DB request query
		BasicDBObject dbOutputObj = new BasicDBObject();
		dbOutputObj.append("_id", 0).append(DB_SMTP_SETTINGS_PATH, 1);
		SmtpSetting ssi = null;

		try {
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.IPAU_SYSTEM_SETTINGS_COLLECTION, 0, 0, searchCondition,
					dbOutputObj, new BasicDBObject());
			if (listResult.size() > 0) {
				ObjectMapper mapper = new ObjectMapper();
				JSONArray jsonarray = new JSONArray(listResult.toString());
				String smtpsettingsInfo = jsonarray.getJSONObject(0).getJSONObject("systemSettings")
						.getJSONObject("smtpSetting").toString();

				ssi = mapper.readValue(smtpsettingsInfo, SmtpSetting.class);

				String host = ssi.getHost();
				if (host != null && host.trim().length() > 0) {
					mMailServerInfo.setPort(Long.parseLong(ssi.getPort()));
					mMailServerInfo.setUseAuth(Boolean.parseBoolean(ssi.getUseAuth()));
					mMailServerInfo.setUsername(ssi.getUserName());
					mMailServerInfo.setPassword(cryptoUtil.decrypt(ssi.getPassword()));
					mMailServerInfo.setFromAddress(ssi.getFromAddress());
					mMailServerInfo.setUseSsl(Boolean.parseBoolean(ssi.getUseSsl()));
					mMailServerInfo.setServer(host); // host有無でメールサーバー設定有無を決定するため、最後にセットする
				}
				bRet = true;
				writeDetailInfoLog("Mail Server Info: " + mMailServerInfo.toString());
			} else {
				writeDetailErrorLog("Mail Server Info: no data.");
			}
		} catch (NumberFormatException | MongoException e) {
			writeDetailErrorLog("setupMailServerInfo(): " + e);
		} catch (Exception e) {
			writeDetailErrorLog("setupMailServerInfo(): " + e);
		}
		return bRet;
	}

	/**
	 * mailSenderモジュールに対する設定。<br/>
	 * ※他のメールアラートアドインに合わせてスケジュールサービス用の情報を流用する。<br/>
	 */
	private void setupMailSenderInfo() {
		// スケジュールサービス用の情報
		ScheduleServiceUtil ssu = new ScheduleServiceUtil();
		mMailSenderInfo.setServer(ssu.getServer());
		mMailSenderInfo.setPort(ssu.getPort());
		mMailSenderInfo.setConnectTimeout(ssu.getConnectTimeout());
		mMailSenderInfo.setReadTimeout(ssu.getReadTimeout());
		writeDetailInfoLog("Mail Sender Info: " + mMailSenderInfo.toString());
	}
}
