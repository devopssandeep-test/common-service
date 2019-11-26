package srdm.cloud.commonService.util.MailAlert.mail;


import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("emailDetails")
public class EmailDetails {

	@XStreamAlias("mailSubject")
	public String mailSubject;

	@XStreamAlias("mailBody")
	public String mailBody;

	public EmailAlertSetting emailAlertSetting;
	public SmtpSetting smtpSetting;

}
