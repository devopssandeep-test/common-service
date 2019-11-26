package srdm.cloud.commonService.util.MailAlert;

import lombok.ToString;

@ToString
public class MailDestinationInfo {
	/*
	 * 宛先情報
	 */
	private String toAddress = null;
	private String ccAddress = null;
	private String bccAddress = null;

	public void setToAddress(final String toAddress) {
		this.toAddress = toAddress;
	}
	public String getToAddress() {
		return this.toAddress;
	}
	public void setCcAddress(final String ccAddress) {
		this.ccAddress = ccAddress;
	}
	public String getCcAddress() {
		return this.ccAddress;
	}
	public void setBccAddress(final String bccAddress) {
		this.bccAddress = bccAddress;
	}
	public String getBccAddress() {
		return this.bccAddress;
	}

	// チェック
	public boolean canSend() {
		if (toAddress != null && toAddress.trim().length() > 0) {
			return true;
		}
		return false;
	}
}
