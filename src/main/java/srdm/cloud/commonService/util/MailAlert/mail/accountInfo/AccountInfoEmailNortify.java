package srdm.cloud.commonService.util.MailAlert.mail.accountInfo;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

import srdm.cloud.commonService.util.MailAlert.MailDestinationInfo;
import srdm.cloud.commonService.util.MailAlert.mail.DataConvert;
import srdm.cloud.commonService.util.MailAlert.mail.EmailDetails;
import srdm.cloud.commonService.util.MailAlert.mail.GlobalStrings;
import srdm.cloud.commonService.util.MailAlert.mail.MailAlertBase;
import srdm.cloud.commonService.util.MailAlert.mail.SendMailUtil;

public class AccountInfoEmailNortify extends MailAlertBase {
	private static final Logger logSimple = Logger.getLogger("srdm.cloud.commonService"); // 実行ログ
	private static final Logger logDetail = Logger.getLogger("srdm.cloud.commonService"); // 詳細ログ

	public AccountInfoEmailNortify(MailDestinationInfo dest) {
		super(dest, logSimple, logDetail);
	}

	/**
	 * メール送信処理実行
	 */
	public boolean execute(final AccountInfoEmailContent content) throws IOException {

		writeSimpleInfoLog("Account Information Mail Notification: Start.");
		if (content == null) {
			writeSimpleErrorLog("Account Information Mail Notification: Invalid Argument.");
			writeDetailErrorLog("Account Information Mail Notification: Invalid Argument.");
			return false;
		}

		EmailDetails emailDetails = new EmailDetails();

		emailDetails.emailAlertSetting = DataConvert.getEmailAlertSetting(getDestinationInfo());
		emailDetails.smtpSetting = DataConvert.getSmtpSetting(getServerInfo());

		emailDetails.mailSubject = content.getSubject();
		emailDetails.mailBody = content.getBody();

		boolean bRet = false;
		String json = makeJson(emailDetails);
		if (json != null && json.length() > 0) {
			SendMailUtil smu = new SendMailUtil(getSenderInfo(), logSimple, logDetail);
			bRet = smu.send(json);
			writeSimpleInfoLog("Account Information Mail Notification: Result: " + bRet);
		} else {
			writeDetailInfoLog("Account Information Mail Notification: Mail Body Creation Error.");
		}
		return bRet;
	}

	private String makeJson(EmailDetails emailDetails ) {
		try {
			XStream xEmailDetailStream = new XStream(new JettisonMappedXmlDriver());
			xEmailDetailStream.setMode(XStream.NO_REFERENCES);
			xEmailDetailStream.alias(GlobalStrings.emailAlertSetting, EmailDetails.class);
			String jsonStr = xEmailDetailStream.toXML(emailDetails);
//			writeSimpleInfoLog("[TEST] JSON:\n" + jsonStr);
			return jsonStr;
		} catch (IllegalArgumentException | XStreamException e) {
			writeDetailErrorLog("makeJson(): " + e);
			return null;
		}
	}
}
