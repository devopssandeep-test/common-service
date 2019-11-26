package srdm.cloud.commonService.util.setting;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringReader;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ScheduleService Server Setting 取得クラス
 *
 */
public class ScheduleServerSettings {

	private static final Logger logger = LoggerFactory.getLogger(ScheduleServerSettings.class);

	private static final String DefaultServerName = "localhost";
	private static final int DefaultServerPort = 8085;
	private static final int DefaultConnectTimeout = 60001;
	private static final int DefaultReadTimeout = 180001;

	private String mServerName = DefaultServerName;
	private int mServerPort = DefaultServerPort;
	private int mConnectTimeout = DefaultConnectTimeout;
	private int mReadTimeout = DefaultReadTimeout;

	/**
	 * iPAUAgentManagerSettings.xmlの内容(スケジュールサービスのサーバー情報のみ)
	 *
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "iPauAgentManager")
	@ToString
	public static class ScheduleServerSetting implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = -3278350593051538838L;
		@ToString
		@Getter
		@Setter
		private static class ScheduleServer implements Serializable {
			/**
			 *
			 */
			private static final long serialVersionUID = -4957141173726310012L;
			private String servername = DefaultServerName;
			private int port = DefaultServerPort;
			private int connectTimeout = DefaultConnectTimeout;
			private int readTimeout = DefaultReadTimeout;
		}
		private ScheduleServer scheduleServer = new ScheduleServer();

		public String getServerName() {
			return scheduleServer.getServername();
		}
		public int getServerPort() {
			return scheduleServer.getPort();
		}
		public int getConnectTimeout() {
			return scheduleServer.getConnectTimeout();
		}
		public int getReadTimeout() {
			return scheduleServer.getReadTimeout();
		}
	}

	public ScheduleServerSettings() {
		readServerSetting();
	}

	/**
	 * 設定ファイル読み込み
	 */
	private void readServerSetting() {
		final String path = System.getProperties().getProperty("catalina.home") + "/webapps/agentManager/WEB-INF/classes/iPAUAgentManagerSetting.xml";
		try (
			FileInputStream fis = new FileInputStream(path);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
		) {
			StringBuilder sb = new StringBuilder();
			String buf;
			while ((buf = br.readLine()) != null) {
				sb.append(buf);
			}
			String xml = sb.toString();
			ScheduleServerSetting sss = JAXB.unmarshal(new StringReader(xml), ScheduleServerSetting.class);
			mServerName = sss.getServerName();
			mServerPort = sss.getServerPort();
			mConnectTimeout = sss.getConnectTimeout();
			mReadTimeout = sss.getReadTimeout();
			logger.info("[ScheduleServerSetting] Schedule Server Configuration: " + sss.toString());
		} catch (IOException e) {
			logger.error("[ScheduleServerSetting] Schedule Server Configuration Read Error: " + e);
		}
	}

	public String getServerName() {
		return mServerName;
	}

	public int getServerPort() {
		return mServerPort;
	}

	public int getConnectTimeout() {
		return mConnectTimeout;
	}

	public int getReadTimeout() {
		return mReadTimeout;
	}

}
