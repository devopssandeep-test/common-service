package srdm.cloud.commonService.util.MailAlert.mail.maintenance;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

import srdm.cloud.commonService.util.MailAlert.MailDestinationInfo;
import srdm.cloud.commonService.util.MailAlert.MailServerInfo;
import srdm.cloud.commonService.util.MailAlert.mail.DataConvert;
import srdm.cloud.commonService.util.MailAlert.mail.EmailDetails;
import srdm.cloud.commonService.util.MailAlert.mail.GlobalStrings;
import srdm.cloud.commonService.util.MailAlert.mail.MailAlertBase;
import srdm.cloud.commonService.util.MailAlert.mail.SendMailUtil;
import srdm.cloud.commonService.util.optimization.DBOptConst;
import srdmlocale.cloud.i18n.ResourceBundle;


public class MaintenanceMailAlert extends MailAlertBase {
	public MaintenanceMailAlert(final MailDestinationInfo dest, final Logger simpleLogger, final Logger detailLogger) {
		super(dest, simpleLogger, detailLogger);
	}

	/**
	 * メール送信が可能か<br/>
	 * (メールサーバーのホスト名が設定されている場合は送信可)
	 *
	 * @return
	 */
	public boolean canSend() {
		try {
			final MailServerInfo msi = this.getServerInfo();
			final String host = msi.getServer();
			if (host != null && host.trim().length() > 0) {
				return true;
			}
		} catch (IOException e) {
			writeDetailInfoLog("Maintenance Mail Check: can't get mail server info.");
		}
		return false;
	}

	/**
	 * メールアラート送信処理を実行する
	 *
	 */
	public boolean execute(final MaintenanceMailContentInfo content) {
		try {
			writeSimpleInfoLog("Maintenance Mail Notification: Start.");
			writeDetailInfoLog("Maintenance Mail Notification: Start.");
			if (content == null) {
				writeSimpleErrorLog("Maintenance Mail Notification: Invalid Argument.");
				writeDetailErrorLog("Maintenance Mail Notification: Invalid Argument.");
				return false;
			}

			EmailDetails emailDetails = new EmailDetails();

			emailDetails.emailAlertSetting = DataConvert.getEmailAlertSetting(getDestinationInfo());
			emailDetails.smtpSetting = DataConvert.getSmtpSetting(getServerInfo());

			SimpleDateFormat sdf = new SimpleDateFormat(content.getDateTimeFormat());
			sdf.setTimeZone(TimeZone.getTimeZone(content.getTimeZoneId()));

			logSimple.fatal("Not implement. [srdmlocale related issue]");
			ResourceBundle rb = ResourceBundle.loadLocalePropertiesFromClassPath(content.getLanguage(), "", "CommonServiceConstants");

			emailDetails.mailSubject = localizeSubject(rb);
			emailDetails.mailBody = localizeBody(content, rb, sdf);

			boolean bRet = false;
			String json = makeJson(emailDetails);
			if (json != null && json.length() > 0) {
				SendMailUtil smu = new SendMailUtil(getSenderInfo(), logSimple, logDetail);
				bRet = smu.send(json);
				writeDetailInfoLog("Maintenance Mail Notification: Result: " + bRet);
			} else {
				writeDetailInfoLog("Maintenance Mail Notification: Mail Body Creation Error.");
			}
			return bRet;
		} catch (IOException e) {
			writeDetailErrorLog("Maintenance Mail Notification: Exception: " + e);
			return false;
		}
	}

	private static final String FMT = "%-40s";
	private static final String FMT_R = "%39s";

	/**
	 * 件名をローカライズ
	 *
	 * @param subject
	 * @param rb
	 * @return
	 */
	private String localizeSubject(final ResourceBundle rb) {
		final String subj = rb.getProperty(FieldNames.subject);
		writeDetailInfoLog("Mail Subject: " + subj);
		return subj;
	}

	/**
	 * 本文をローカライズ
	 *
	 * @param content
	 * @param rb
	 * @param sdf
	 * @return
	 */
	private String localizeBody(final MaintenanceMailContentInfo content, final ResourceBundle rb, final SimpleDateFormat sdf) {
		StringBuilder sb = new StringBuilder();

		/*

		SRDM schedule maintenance is completed.

		Scheduled maintenance execution result: Success
		*/

		/*
		SRDM schedule maintenance is completed.

		Scheduled maintenance execution result: Failed

		Please check the error logs in the below specified location
		<xxxx\SRDM\tomcat\logs\>
		*/
		Map<String, String> errors = content.getErrors();

		// 先頭のメッセージ
		sb.append(rb.getProperty(FieldNames.message));
		sb.append("\n\n");
		// 結果
		sb.append(String.format(FMT, rb.getProperty(FieldNames.execResult))).append(": ");
		if (errors == null || errors.isEmpty() == true) {
			sb.append(rb.getProperty(FieldNames.execResultSuccess));
		} else {
			sb.append(rb.getProperty(FieldNames.execResultFailure));
		}
		sb.append("\n\n");
		// データフォルダのサイズ(実行前)
		sb.append(String.format(FMT, rb.getProperty(FieldNames.dataFolderSizeBefore))).append(": ");
		sb.append(content.getDataFolderSizeBefore()).append(" ").append(rb.getProperty(FieldNames.bytes));
		sb.append("\n");
		// データフォルダのサイズ(実行後)
		sb.append(String.format(FMT, rb.getProperty(FieldNames.dataFolderSizeAfter))).append(": ");
		sb.append(content.getDataFolderSizeAfter()).append(" ").append(rb.getProperty(FieldNames.bytes));
		sb.append("\n");
		// ディスク空き容量(実行前)
		sb.append(String.format(FMT, rb.getProperty(FieldNames.diskFreeSizeBefore))).append(": ");
		sb.append(content.getDiskFreeSizeBefore()).append(" ").append(rb.getProperty(FieldNames.bytes));
		sb.append("\n");
		// ディスク空き容量(実行後)
		sb.append(String.format(FMT, rb.getProperty(FieldNames.diskFreeSizeAfter))).append(": ");
		sb.append(content.getDiskFreeSizeAfter()).append(" ").append(rb.getProperty(FieldNames.bytes));
		sb.append("\n");
		// 開始日時
		sb.append(String.format(FMT, rb.getProperty(FieldNames.startDateTime))).append(": ");
		sb.append(sdf.format(content.getStartTimestamp()));
		sb.append("\n");
		// 終了日時
		sb.append(String.format(FMT, rb.getProperty(FieldNames.endDateTime))).append(": ");
		sb.append(sdf.format(content.getEndTimestamp()));
		sb.append("\n");
		if (errors != null && errors.isEmpty() == false) {
			// エラー内容
			sb.append("\n");
			sb.append(String.format(FMT, rb.getProperty(FieldNames.errorList))).append("\n");
			for (Map.Entry<String, String> entry: errors.entrySet()) {
				final String dbName = entry.getKey();
				final String error = entry.getValue();
				if (DBOptConst.ListOfInternalSections.contains(dbName) == true) {
					sb.append(rb.getProperty(FieldNames.otherErrors));
				} else {
					sb.append(String.format(FMT_R, dbName));
				}
				sb.append(" : ");
//				sb.append(rb.getProperty(FieldNames.errorPrefix + error)); // エラー内容をローカライズ
				sb.append(DBOptConst.getErrorCode(error)); // エラーコード
				sb.append("\n");
			}
		}
		sb.append("\n");
//		writeDetailInfoLog("[TEST] Mail Body:\n" + sb.toString());
		writeDetailInfoLog("Mail Body:\n" + sb.toString());

		return sb.toString();
	}

	private String makeJson(EmailDetails emailDetails ) {
		try {
			XStream xEmailDetailStream = new XStream(new JettisonMappedXmlDriver());
			xEmailDetailStream.setMode(XStream.NO_REFERENCES);
			xEmailDetailStream.alias(GlobalStrings.emailAlertSetting, EmailDetails.class);
			String jsonStr = xEmailDetailStream.toXML(emailDetails);
//			writeDetailInfoLog("[TEST] JSON:\n" + jsonStr);
			return jsonStr;
		} catch (IllegalArgumentException | XStreamException e) {
			writeDetailErrorLog("makeJson(): " + e);
			return null;
		}
	}


}
