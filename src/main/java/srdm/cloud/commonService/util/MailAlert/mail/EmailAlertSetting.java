package srdm.cloud.commonService.util.MailAlert.mail;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("emailAlertSetting")
public class EmailAlertSetting {

	public String getToAddress() {
		return toAddress;
	}

	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}

	public String getCcAddress() {
		return ccAddress;
	}

	public void setCcAddress(String ccAddress) {
		this.ccAddress = ccAddress;
	}

	public String getBccAddress() {
		return bccAddress;
	}

	public void setBccAddress(String bccAddress) {
		this.bccAddress = bccAddress;
	}

	@XStreamAlias("toAddress")
	private String toAddress;

	@XStreamAlias("ccAddress")
	private String ccAddress;

	@XStreamAlias("bccAddress")
	private String bccAddress;
}
