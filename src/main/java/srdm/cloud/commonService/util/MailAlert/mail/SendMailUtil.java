package srdm.cloud.commonService.util.MailAlert.mail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;

import srdm.cloud.commonService.util.MailAlert.MailSenderInfo;

public class SendMailUtil {
	private Logger logSimple = null;
	private Logger logDetail = null;

	private String mMailSenderUrl;
	private int mConnectTimeout;
	private int mReadTimeout;

	public SendMailUtil(MailSenderInfo msi, final Logger simpleLogger, final Logger detailLogger) {
		logSimple = simpleLogger;
		logDetail = detailLogger;

		mMailSenderUrl = "http://" + msi.getServer() + ":" + msi.getPort() + "/mailSender/SendMail"; // 末尾にスラッシュを付加しないこと
		mConnectTimeout = msi.getConnectTimeout();
		mReadTimeout = msi.getReadTimeout();

		writeDetailInfoLog("Mail Sender: setup: " + mMailSenderUrl + " connTO=" + mConnectTimeout + " readTO=" + mReadTimeout);
	}

	public boolean send(final String json) {
		String res = null;
		writeSimpleInfoLog("Mail Sender: Start.");
		final int nTries = 3;
		for (int i = 0; i < nTries; i ++) {
			try {
				writeDetailInfoLog("Mail Sender: Requesting. #" + (i + 1));
				res = sendRequest(json);
				writeDetailInfoLog("Mail Sender: Requested. #" + (i + 1));
				break;
			} catch (IOException e) {
				final int n = i + 1;
				writeDetailWarnLog("Mail Sender: #" + n + " Exception: " + e);
				if (n == nTries) {
					writeDetailErrorLog("json:\n" + json);
					for (StackTraceElement ste: e.getStackTrace()) {
						writeDetailFatalLog(" " + ste.toString());
					}
				}
				// retry
			}
		}
		if (res != null) {
			// "{'resultData':[{'response':'" + mailResponse + "'}]}"の内容をチェック
			writeSimpleInfoLog("Mail Sender: End.");
			return true;
		} else {
			writeSimpleErrorLog("Mail Sender: Failed.");
			writeDetailErrorLog("Mail Sender: Failed.");
		}
		return false;
	}

	private String sendRequest(final String req) throws IOException {

		// リクエスト
		URL u = new URL(mMailSenderUrl);
		HttpURLConnection conn = (HttpURLConnection)u.openConnection();
		conn.setConnectTimeout(mConnectTimeout);
		conn.setReadTimeout(mReadTimeout);
		conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		try (OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream(), "UTF-8")) {
			osw.write(req);
			osw.flush();
			osw.close();
		}
		conn.connect();

		// レスポンス
		String res = "{}";
		try (
			InputStreamReader isr = new InputStreamReader(conn.getInputStream(), "UTF-8");
			BufferedReader br = new BufferedReader(isr);
		) {
			StringBuilder sb = new StringBuilder();
			String buf;
			while ((buf = br.readLine()) != null) {
				sb.append(buf);
			}
			res = sb.toString();
			conn.disconnect();
		}

		return res;
	}
	private void writeSimpleErrorLog(final Object message) {
		if (logSimple != null) {
			logSimple.error(message);
		}
	}
	private void writeSimpleInfoLog(final Object message) {
		if (logSimple != null) {
			logSimple.info(message);
		}
	}
	private void writeDetailFatalLog(final Object message) {
		if (logDetail != null) {
			logDetail.fatal(message);
		}
	}
	private void writeDetailErrorLog(final Object message) {
		if (logDetail != null) {
			logDetail.error(message);
		}
	}
	private void writeDetailWarnLog(final Object message) {
		if (logDetail != null) {
			logDetail.warn(message);
		}
	}
	private void writeDetailInfoLog(final Object message) {
		if (logDetail != null) {
			logDetail.info(message);
		}
	}
}
