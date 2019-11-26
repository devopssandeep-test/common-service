package srdm.cloud.commonService.util.MailAlert;

import lombok.ToString;

@ToString
public class MailSenderInfo {
	/*
	 *  メールセンダー情報
	 */
	private String server = null;
	private int port = 0;
	private int connectTimeout = 60001;
	private int readTimeout = 180001;

	public void setServer(final String server) {
		this.server = server;
	}
	public String getServer() {
		return this.server;
	}
	public boolean setPort(final int port) {
		if (port > 0 && port < 65536) {
			this.port = port;
			return true;
		}
		return false;
	}
	public long getPort() {
		return this.port;
	}
	public void setConnectTimeout(final int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	public int getConnectTimeout() {
		return this.connectTimeout;
	}
	public void setReadTimeout(final int readTimeout) {
		this.readTimeout = readTimeout;
	}
	public int getReadTimeout() {
		return this.readTimeout;
	}
}
