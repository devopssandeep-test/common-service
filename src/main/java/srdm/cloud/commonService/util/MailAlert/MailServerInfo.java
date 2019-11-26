package srdm.cloud.commonService.util.MailAlert;

import lombok.ToString;

@ToString(exclude="password") // パスワードはログに出力しない
public class MailServerInfo {
	/*
	 * メールサーバー情報
	 */
	private String server = null;
	private long port = 0;
	private boolean useAuth = false;
	private String username = null;
	private String password = null;
	private String fromAddress = null;
	private boolean useSsl = false;

	public void setServer(final String server) {
		this.server = server;
	}
	public String getServer() {
		return this.server;
	}
	public boolean setPort(final long port) {
		if (port > 0 && port < 65536) {
			this.port = port;
			return true;
		}
		return false;
	}
	public long getPort() {
		return this.port;
	}
	public void setUseAuth(final boolean useAuth) {
		this.useAuth = useAuth;
	}
	public boolean getUseAuth() {
		return this.useAuth;
	}
	public void setUsername(final String username) {
		this.username = username;
	}
	public String getUsername() {
		return this.username;
	}
	public void setPassword(final String password) {
		this.password = password;
	}
	public String getPassword() {
		return this.password;
	}
	public void setFromAddress(final String fromAddress) {
		this.fromAddress = fromAddress;
	}
	public String getFromAddress() {
		return this.fromAddress;
	}
	public void setUseSsl(final boolean useSsl) {
		this.useSsl = useSsl;
	}
	public boolean getUseSsl() {
		return this.useSsl;
	}
}
