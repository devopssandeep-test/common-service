package srdm.cloud.commonService.util.MailAlert.mail;

import srdm.cloud.commonService.util.MailAlert.MailDestinationInfo;
import srdm.cloud.commonService.util.MailAlert.MailServerInfo;

public class DataConvert {
	public static EmailAlertSetting getEmailAlertSetting(final MailDestinationInfo dest) {
		EmailAlertSetting data = new EmailAlertSetting();

		data.setToAddress(dest.getToAddress());
		data.setCcAddress(dest.getCcAddress());
		data.setBccAddress(dest.getBccAddress());

		return data;
	}

	public static EmailDetails getEmailDetails() {
		EmailDetails data = new EmailDetails();

		return data;
	}

	public static SmtpSetting getSmtpSetting(final MailServerInfo server) {
		SmtpSetting data = new SmtpSetting();
		data.setHost(server.getServer());
		data.setPort(server.getPort() + "");
		data.setUseAuth(server.getUseAuth() + "");
		data.setUserName(server.getUsername());
		data.setPassword(server.getPassword());
		data.setFromAddress(server.getFromAddress());
		data.setUseSsl(server.getUseSsl() + "");
		return data;
	}
}
