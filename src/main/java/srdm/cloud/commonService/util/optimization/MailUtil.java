package srdm.cloud.commonService.util.optimization;

import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.srdm.mongodb.constants.GlobalStrings;
import com.srdm.mongodb.service.IDBRequest;
import com.srdm.mongodb.serviceImpl.DBRequest;

import lombok.ToString;
import srdm.cloud.commonService.util.MailAlert.MailDestinationInfo;
import srdm.cloud.commonService.util.MailAlert.mail.maintenance.MaintenanceMailAlert;
import srdm.cloud.commonService.util.MailAlert.mail.maintenance.MaintenanceMailContentInfo;

/**
 *
 * メール通知用ユーティリティ―<br/>
 * ※DB最適化実行前にインスタンスを生成すること
 *
 */
public class MailUtil {
	private Logger logSimple = null;
	private Logger logDetail = null;

	@ToString
	private static class MailInfo {
		// メール通知有無
		private boolean sendFlag = false;
		// ローカライズ情報
		private String timeZoneId = null;
		private String dateTimeFormat = null;
		private String language = null;
	}
	private MailInfo mMailInfo = new MailInfo();

	private MaintenanceMailAlert mMaintenanceMailAlert = null;
	private MailDestinationInfo mMailDestinationInfo = null;

	public MailUtil(final Logger simpleLogger, final Logger detailLogger) {
		logSimple = simpleLogger;
		logDetail = detailLogger;
	}

	public boolean setup() {
		boolean bRet = true;
		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		BasicDBObject searchCondition = new BasicDBObject();
		searchCondition.append("systemSettings.scheduledMaintenanceInfo", new BasicDBObject("$exists", true));
		// Form DB request query
		BasicDBObject dbOutputObj = new BasicDBObject();
		dbOutputObj.append("_id", 0).append("systemSettings.scheduledMaintenanceInfo", 1);
		ScheduledMaintenanceUtil smi = null;

		try {
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.IPAU_SYSTEM_SETTINGS_COLLECTION, 0, 0, searchCondition,
					dbOutputObj, new BasicDBObject());

			if (listResult.size() > 0) {
				ObjectMapper mapper = new ObjectMapper();
				JSONArray jsonarray = new JSONArray(listResult.toString());
				String scheduledMaintenanceInfo = jsonarray.getJSONObject(0).getJSONObject("systemSettings")
						.getJSONObject("scheduledMaintenanceInfo").toString();

				smi = mapper.readValue(scheduledMaintenanceInfo, ScheduledMaintenanceUtil.class);

				// メール通知/ローカライズ情報
				mMailInfo.sendFlag = smi.sendFlag;
				mMailInfo.timeZoneId = smi.timeZone;
				mMailInfo.dateTimeFormat = smi.dateTimeFormat;
				mMailInfo.language = smi.language;
				writeDetailInfoLog("Mail Notification/Localization Info: " + mMailInfo.toString());
				// メール宛先設定
				mMailDestinationInfo = new MailDestinationInfo();
				mMailDestinationInfo.setToAddress(smi.toAddress);
				mMailDestinationInfo.setCcAddress(smi.ccAddress);
				mMailDestinationInfo.setBccAddress(smi.bccAddress);
				writeDetailInfoLog("Mail Destination Info: " + mMailDestinationInfo.toString());
				// メールアラートのインスタンスを生成
				mMaintenanceMailAlert = new MaintenanceMailAlert(mMailDestinationInfo, logSimple, logDetail);
			} else {
				writeDetailInfoLog("MailUtil#setup(): no data.");
			}
		} catch (MongoException e) {
			writeDetailErrorLog("MailUtil#setup(): " + e);
			bRet = false;
		} catch (Exception e) {
			writeDetailErrorLog("MailUtil#setup(): " + e);
			bRet = false;
		}
		return bRet;
	}

	public boolean send(final DBOptResultInfo resultInfo) {
		if (isEnabled() == false) { // sendFlag=falseの場合
			writeSimpleInfoLog("No mail sent.");
			writeDetailInfoLog("No mail sent: disabled.");
			return false;
		}
		if (mMaintenanceMailAlert == null || mMaintenanceMailAlert.canSend() == false) { // セットアップ失敗(メール通知情報)
			writeSimpleErrorLog("Mail Configuration Error.");
			writeDetailErrorLog("Mail Configuration Error.");
			return false;
		}
		if (mMailDestinationInfo == null || mMailDestinationInfo.canSend() == false) { // セットアップ失敗(宛先)
			writeSimpleErrorLog("Mail Destination Setup Error.");
			writeDetailErrorLog("Mail Destination Setup Error.");
			return false;
		}

		// メール送信内容設定
		MaintenanceMailContentInfo content = new MaintenanceMailContentInfo();
		content.setTimeZoneId(mMailInfo.timeZoneId);
		content.setDateTimeFormat(mMailInfo.dateTimeFormat);
		content.setLanguage(mMailInfo.language);
		content.setStartTimestamp(resultInfo.getStartTimestamp());
		content.setEndTimestamp(resultInfo.getEndTimestamp());
		content.setDataFolderSizeBefore(resultInfo.getDbDataSizeBefore());
		content.setDataFolderSizeAfter(resultInfo.getDbDataSizeAfter());
		content.setDiskFreeSizeBefore(resultInfo.getDiskFreeSizeBefore());
		content.setDiskFreeSizeAfter(resultInfo.getDiskFreeSizeAfter());
		content.setErrors(resultInfo.getErrors());
		writeDetailInfoLog("Maintenance Mail Content Info: " + content.toString());
		// メール送信実行
		boolean bRet = mMaintenanceMailAlert.execute(content);
		if (bRet == true) {
			writeSimpleInfoLog("Maintenance Mail Send Complete.");
			writeDetailInfoLog("Maintenance Mail Send Complete: " + mMailInfo.toString());
		} else {
			writeSimpleErrorLog("Maintenance Mail Send Error.");
			writeDetailErrorLog("Maintenance Mail Send Error: " + mMailInfo.toString());
		}

		return bRet;
	}

	public boolean isEnabled() {
		return mMailInfo.sendFlag;
	}

	public boolean canSend() {
		if (mMaintenanceMailAlert != null) {
			return mMaintenanceMailAlert.canSend();
		}
		return false;
	}

	private void writeSimpleErrorLog(final Object message) {
		if (logSimple != null) {
			logSimple.error(message);
		}
	}
	private void writeSimpleInfoLog(final Object message) {
		if (logSimple != null) {
			logSimple.info(message);
		}
	}
	private void writeDetailErrorLog(final Object message) {
		if (logDetail != null) {
			logDetail.error(message);
		}
	}
	private void writeDetailInfoLog(final Object message) {
		if (logDetail != null) {
			logDetail.info(message);
		}
	}
}
