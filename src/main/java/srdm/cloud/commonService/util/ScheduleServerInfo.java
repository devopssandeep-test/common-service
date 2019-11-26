package srdm.cloud.commonService.util;

import srdm.cloud.commonService.util.setting.ScheduleServerSettings;

/**
 * ScheduleServer情報
 * AppConfigでBean定義
 */
public class ScheduleServerInfo {

	private String mServerName;
	private int mServerPort;
	private int mConnectTimeout;
	private int mReadTimeout;

	public void initialize() {
		ScheduleServerSettings sss = new ScheduleServerSettings();
		mServerName = sss.getServerName();
		mServerPort = sss.getServerPort();
		mConnectTimeout = sss.getConnectTimeout();
		mReadTimeout = sss.getReadTimeout();
	}

	public String getServer() {
		return mServerName;
	}
	public int getPort() {
		return mServerPort;
	}

	public int getConnectTimeout() {
		return mConnectTimeout;
	}

	public int getReadTimeout() {
		return mReadTimeout;
	}
}
