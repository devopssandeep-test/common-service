package srdm.cloud.commonService.util.optimization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.JAXB;

import org.apache.log4j.Logger;

import lombok.ToString;
import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;
import srdm.cloud.commonService.domain.model.comm.Common;
import srdm.cloud.commonService.domain.model.comm.scheduleService.Schedule;
import srdm.cloud.commonService.util.setting.ScheduleServerSettings;



public class ScheduleServiceUtil {

	private static final Logger log = Logger.getLogger(ScheduleServiceUtil.class);

	public static final String ScheduleName = "dbOptimize";
	public static final String SystemId = "COMMON";

	private String mServerUrl;
	private String mServerName;
	private int mServerPort;
	private int mConnectTimeout;
	private int mReadTimeout;

	private String mSessionId;

	@ToString
	public static class CommonOnlyResponse {
		public Common common;

		public boolean hasError() {
			return (common != null && common.getErrorList() != null && common.getErrorList().length > 0);
		}
	}

	// createSchedule, createOneTimeSchedule
	@ToString
	public static class CreateScheduleResponse {
		public Common common;
		public String scheduleId;

		public boolean hasError() {
			return (common != null && common.getErrorList() != null && common.getErrorList().length > 0);
		}
	}

	@ToString
	public static class ScheduleServiceResponse {
		public Common common;

		@ToString
		public static class ResultData {
			public Schedule schedule;
		}
		public ResultData[] resultData;

		public boolean hasError() {
			return (common != null && common.getErrorList() != null && common.getErrorList().length > 0);
		}
		public List<String> getScheduleId() {
			List<String> listResult = new ArrayList<String>();
			if (resultData != null && resultData.length > 0) {
				for (ResultData rd: resultData) {
					if (rd.schedule != null) {
						String id = rd.schedule.getScheduleId();
						if (id != null && id.trim().length() > 0) {
							listResult.add(id);
						}
					}
				}
			}
			return listResult;
		}
		public List<Schedule> getSchedule() {
			List<Schedule> listResult = new ArrayList<Schedule>();
			if (resultData != null && resultData.length > 0) {
				for (ResultData rd: resultData) {
					if (rd.schedule != null) {
						listResult.add(rd.schedule);
					}
				}
			}
			return listResult;
		}
	}

	public ScheduleServiceUtil() {
		mSessionId = "sessionId";
		setupServerInfo();
	}

	public ScheduleServiceUtil(final String sessionId) {
		// セッションID
		mSessionId = sessionId;
		// サーバー情報
		setupServerInfo();
	}

	/**
	 * スケジュールサービスのサーバー情報を取得する
	 *
	 */
	private void setupServerInfo() {
		ScheduleServerSettings sss = new ScheduleServerSettings();
		mServerName = sss.getServerName();
		mServerPort = sss.getServerPort();
		mConnectTimeout = sss.getConnectTimeout();
		mReadTimeout = sss.getReadTimeout();
		mServerUrl = "http://" + mServerName + ":" + mServerPort + "/SrdmCloudScheduleService/";
		log.info("Schedule Server URL=[" + mServerUrl + "]");
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

	public static final int MODE_REPEAT = 1;
	public static final int MODE_ONCE = 2;
	public static final int MODE_STOP = 3;
	/**
	 * 定期メンテナンス用スケジュールを更新する。
	 *
	 * @param xml
	 * @param mode
	 * @return
	 */
	public boolean updateSchedule(final String xml, final int mode) {
		boolean bRet = true;

		String api;
		if (mode == MODE_REPEAT) {
			api = API_CREATE_SCHEDULE;
		} else if (mode == MODE_ONCE) {
			api = API_CREATE_ONE_TIME_SCHEDULE;
		} else if (mode == MODE_STOP) {
			// 削除してからreturnするためここのセットは無意味だが、下の処理で変数が初期化されていないという警告が出るためセットしている
			api = API_DELETE_SCHEDULE;
		} else {
			log.error("updateSchedule: unknown mode=" + mode);
			return false;
		}

		/*
		 * scheduleName='dbOptimize'のスケジュールを更新する。
		 * (削除してから新規登録)
		 */
		List<String> listScheduleId = getScheduleIds();
		if (listScheduleId.size() > 0) {
			for (String scheduleId: listScheduleId) {
				deleteSchedule(scheduleId); // DBのエラーではないため、エラーが発生してもスルーする
			}
		}
		if (mode == MODE_STOP) {
			return true;
		}

//		log.info("[TEST] updateSchedule(): xml=" + xml);
		Schedule sched = JAXB.unmarshal(new StringReader(xml),  Schedule.class);
		// 注意: JSON.encode()では<e>の扱いがDB仕様に沿わなくなるため、自分でJSON文字列を作成する。
//		log.info("[TEST] updateSchedule(): Schedule=" + sched.toString());

		String req;
		if (mode == MODE_ONCE) {
			req = makeJsonStringForCreateOneTimeSchedule(sched);
		} else { // mode == MODE_REPEAT
			req = makeJsonStringForCreateSchedule(sched);
		}

		String scheduleId = null;
		try {
			String res = sendRequest(api, req);
			CreateScheduleResponse csr = JSON.decode(res, CreateScheduleResponse.class);
			if (csr.hasError() == true) {
				log.error("updateSchedule(): Schedule Service Error: " + res);
				bRet = false;
			} else {
				scheduleId = csr.scheduleId;
			}
		} catch (IOException e) {
			log.error("updateSchedule(): Exception: " + e);
			bRet = false;
		}

		if (mode != MODE_ONCE && bRet == true) {
			startSchedule(scheduleId);
		}

		return bRet;
	}

	/**
	 * createSchedule用のJSON文字列を作成する。
	 *
	 * @param sched
	 * @return
	 */
	private String makeJsonStringForCreateSchedule(Schedule sched) {
		StringBuilder sb = new StringBuilder();

		sb.append("{");
		sb.append(	quote("sessionId", mSessionId)).append(",");
		sb.append(	makeF(false)).append(",");
		sb.append(	quote("R")).append(":{");
		sb.append(		quote("asFirstInto", "$input")).append(",");
		sb.append(		quote("insertNode")).append(":{");
		sb.append(			quote("schedule")).append(":{");
		sb.append(				quote("scheduleId", sched.getScheduleId())).append(","); // スケジュールサービスで発行
		sb.append(				quote("scheduleName", sched.getScheduleName())).append(",");
		sb.append(				quote("groupId")).append(":").append(sched.getGroupId()).append(",");
		sb.append(				quote("timeZone", sched.getTimeZone())).append(",");
		sb.append(				quote("systemId", sched.getSystemId())).append(",");
		sb.append(				quote("status", sched.getStatus())).append(","); // スケジュールサービスで更新
		sb.append(				quote("noWait", sched.isNoWait())).append(",");
		sb.append(				quote("externalApiPath", sched.getExternalApiPath())).append(",");
		sb.append(				quote("externalApiJsonParam", sched.getExternalApiJsonParam())).append(",");
		sb.append(				quote("runStartDate", sched.getRunStartDate())).append(",");
		sb.append(				quote("runDate")).append(":{");
		if (sched.getRunDate() != null) {
			sb.append(				quote("select", sched.getRunDate().getSelect())).append(",");
			sb.append(				quote("period", sched.getRunDate().getPeriod())).append(",");
			sb.append(				quote("sunday", sched.getRunDate().isSunday())).append(",");
			sb.append(				quote("monday", sched.getRunDate().isMonday())).append(",");
			sb.append(				quote("tuesday", sched.getRunDate().isTuesday())).append(",");
			sb.append(				quote("wednesday", sched.getRunDate().isWednesday())).append(",");
			sb.append(				quote("thursday", sched.getRunDate().isThursday())).append(",");
			sb.append(				quote("friday", sched.getRunDate().isFriday())).append(",");
			sb.append(				quote("saturday", sched.getRunDate().isSaturday())).append(",");
			sb.append(				quote("day")).append(":[");
			if (sched.getRunDate().getDay() != null) {
				StringBuilder sb2 = new StringBuilder();
				for (Long d: sched.getRunDate().getDay()) {
					if (d == null) {
						continue;
					}
					if (sb2.length() > 0) {
						sb2.append(",");
					}
					sb2.append(d);
				}
				sb.append(				sb2);
			}
			sb.append(				"]"); // day
		}
		sb.append(				"},"); // runDate
		sb.append(				quote("runTime")).append(":{");
		if (sched.getRunTime() != null) {
			sb.append(				quote("select", sched.getRunTime().getSelect())).append(",");
			sb.append(				quote("periodHour", sched.getRunTime().getPeriodHour())).append(",");
			sb.append(				quote("periodMinute", sched.getRunTime().getPeriodMinute())).append(",");
			sb.append(				quote("timeList")).append(":[");
			if (sched.getRunTime().getTimeList() != null) {
				StringBuilder sb2 = new StringBuilder();
				for (Schedule.RunTime.TimeList tl: sched.getRunTime().getTimeList()) {
					if (tl == null) {
						continue;
					}
					if (tl.getTime() != null) {
						if (sb2.length() > 0) {
							sb2.append(",");
						}
						sb2.append(		"{");
						sb2.append(			quote("time")).append(":{");
//						sb2.append(				quote("hour", tl.time.hour)).append(",");
//						sb2.append(				quote("minute", tl.time.minute));
						sb2.append(				quote("hour")).append(":").append(tl.getTime().getHour()).append(",");
						sb2.append(				quote("minute")).append(":").append(tl.getTime().getMinute());
						sb2.append(			"}"); // time
						sb2.append(		"}");
					}
				}
				sb.append(				sb2);
			}
			sb.append(				"]"); // timeList
		}
		sb.append(				"},"); // runTime
		sb.append(				quote("loginInfo")).append(":{");
		if (sched.getLoginInfo() != null) {
			sb.append(				quote("domainId", sched.getLoginInfo().getDomainId()));
			sb.append(",");
			sb.append(				quote("accountId", sched.getLoginInfo().getAccountId()));
		}
		sb.append(				"}"); // loginInfo
		sb.append(			"}"); // schedule
		sb.append(		"}"); // insertNode
		sb.append(	"}"); // R
		sb.append("}");

		return sb.toString();
	}

	/**
	 * createOneTimeSchedule用のJSON文字列を作成する。<br/>
	 * <br/>
	 * createScheduleとcreateOneTimeScheduleでは、パラメータ名(構成)や値が異なる箇所があることに注意。<br/>
	 * 例)<br/>
	 * ・パラメータ名：timeZone/timeZoneId、runTimeツリー/startRunTime文字列<br/>
	 * ・externalApiJsonParamの値：{}を含める(値はオプショナル)/自動で付加される(値必須:空白はOK)<br/>
	 *
	 * @param sched
	 * @return
	 */
	private String makeJsonStringForCreateOneTimeSchedule(Schedule sched) {
		// APIパラメータ
		String param = sched.getExternalApiJsonParam();
		if (param == null || param.length() == 0) {
			param = " ";
		}

		// 開始時刻
		long hour = 0;
		long minute = 0;
		if (sched.getRunTime() != null) {
			if (sched.getRunTime().getTimeList() != null) {
				for (Schedule.RunTime.TimeList tl: sched.getRunTime().getTimeList()) {
					if (tl == null) {
						continue;
					}
					if (tl.getTime() != null) {
						if (tl.getTime().getHour() != null) {
							hour = tl.getTime().getHour();
						}
						if (tl.getTime().getMinute() != null) {
							minute = tl.getTime().getMinute();
						}
					}
					break; // 1件だけのはず→複数あれば先頭を採用
				}
			}
		}
		final String hhmm = String.format("%02d%02d", hour, minute);

		// リクエスト
		StringBuilder sb = new StringBuilder();

		sb.append("{");
		sb.append(quote("sessionId", mSessionId)).append(",");
		sb.append(quote("scheduleName", sched.getScheduleName())).append(",");
		sb.append(quote("groupId")).append(":").append(sched.getGroupId()).append(",");
		sb.append(quote("systemId", sched.getSystemId())).append(",");
		sb.append(quote("timeZoneId", sched.getTimeZone())).append(",");
		sb.append(quote("externalApiPath", sched.getExternalApiPath())).append(",");
		sb.append(quote("externalApiJsonParam", param)).append(",");
		sb.append(quote("runStartDate", sched.getRunStartDate())).append(",");
		sb.append(quote("runStartTime", hhmm)).append(",");
		sb.append(quote("loginDomainId", sched.getLoginInfo().getDomainId()));
		sb.append(",");
		sb.append(quote("loginAccountId", sched.getLoginInfo().getAccountId()));
		sb.append("}");

		return sb.toString();
	}

	/**
	 * 文字列をダブルクォートで囲む。
	 *
	 * @param buf
	 * @return
	 */
	private String quote(final String buf) {
		return new StringBuilder().append("\"").append(buf).append("\"").toString();
	}

	/**
	 * 文字列をダブルクォートで囲む。
	 *
	 * @param buf
	 * @param bool
	 * @return
	 */
	private String quote(final String buf, final boolean bool) {
		return quote(buf, (bool == true ? "true" : "false"));
	}

	/**
	 * 文字列をダブルクォートで囲む。
	 *
	 * @param buf
	 * @param n
	 * @return
	 */
	private String quote(final String buf, final Long n) {
		return quote(buf, (n != null ? n.toString() : ""));
	}

	/**
	 * 文字列をダブルクォートで囲む。
	 *
	 * @param buf1
	 * @param buf2
	 * @return
	 */
	private String quote(final String buf1, final String buf2) {
		return new StringBuilder().append("\"").append(buf1).append("\":\"").append((buf2 != null) ? buf2 : "").append("\"").toString();
	}

	/**
	 * F句を作成する。
	 *
	 * @return
	 */
	private String makeF() {
		return makeF(true);
	}

	/**
	 * F句を作成する。
	 * insert時はfalseを指定すること。
	 *
	 * @param bAddRoot パスに"schedule"ノードを付加する(true)/しない(false)
	 * @return
	 */
	private String makeF(final boolean bAddRoot) {
		StringBuilder sb = new StringBuilder();
		sb.append("\"F\":{");
		sb.append("\"variable\":\"$input\",");
		sb.append("\"in\":\"/ipau/scheduleList");
		if (bAddRoot == true) {
			sb.append("/schedule");
		}
		sb.append("\"");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * W句を作成する。
	 *
	 * @return
	 */
	private String makeW() {
		return makeW(null);
	}
	private String makeW(final String scheduleId) {
		StringBuilder sb = new StringBuilder();
		sb.append("\"W\":\"");
		sb.append("$input/systemId='").append(SystemId).append("'");
		sb.append(" and $input/scheduleName='").append(ScheduleName).append("'");
		if (scheduleId != null) {
			sb.append(" and $input/scheduleId='").append(scheduleId).append("'");
		}
		sb.append("\"");
		return sb.toString();
	}

	/**
	 * 定期メンテナンス用のスケジュール情報を取得する<br/>
	 * 基本的には1件しかないが、複数存在する場合は1件目を返す。<br/>
	 * 存在しない場合はnullを返す(例: ワンタイムスケジュール実行後)<br/>
	 *
	 * @return XML文字列(ルートノードはschedule)
	 */
	public String getSchedule() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"sessionId\":\"").append(mSessionId).append("\",");
		sb.append(makeF()).append(",");
		sb.append(makeW()).append(",");
		sb.append("\"R\":\"$input\"");
		sb.append("}");
		final String req = sb.toString();

		final String api = "getSchedule";
		String xml = null;
		try {
			String res = sendRequest(api, req);
			// JSONをデコード
			ScheduleServiceResponse ssr = JSON.decode(res, ScheduleServiceResponse.class);
			if (ssr.hasError() == false) {
				List<Schedule> listSchedule = ssr.getSchedule();
				if (listSchedule.size() > 0) {
					StringWriter sw = new StringWriter();
					JAXB.marshal(ssr.resultData[0].schedule, sw);
					xml = sw.toString();
				}
			}
		} catch (JSONException e) {
			log.error("getSchedule(): JSONException: " + e);
		} catch (IOException e) {
			log.error("getSchedule(): Exception: " + e);
		}

		return xml;
	}

	/**
	 * 定期メンテナンス用のスケジュールIDを取得する。
	 *
	 * @return
	 */
	private List<String> getScheduleIds() {
		List<String> listResult = new ArrayList<String>();

		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"sessionId\":\"").append(mSessionId).append("\",");
		sb.append(makeF()).append(",");
		sb.append(makeW()).append(",");
		sb.append("\"R\":\"<schedule>{$input/scheduleId}</schedule>\"");
		sb.append("}");
		final String req = sb.toString();

		try {
			String res = sendRequest(API_GET_SCHEDULE, req);
			// JSONをデコード
			ScheduleServiceResponse ssr = JSON.decode(res, ScheduleServiceResponse.class);
			if (ssr.hasError() == false) {
				List<Schedule> listSchedule = ssr.getSchedule();
				if (listSchedule.size() > 0) {
					for (int i = 0; i < listSchedule.size(); i ++) {
						listResult.add(listSchedule.get(i).getScheduleId());
					}
				}
			}
		} catch (JSONException e) {
			log.error("getScheduleIds(): JSONException: " + e);
		} catch (IOException e) {
			log.error("getScheduleIds(): Exception: " + e);
		}
		return listResult;
	}

	/**
	 * 指定されたスケジュールIDのスケジュールを開始する。
	 *
	 * @param scheduleId
	 * @return
	 */
	private boolean startSchedule(final String scheduleId) {
		log.info("startSchedule(): scheduleId=" + scheduleId);
		if (scheduleId == null || scheduleId.trim().length() == 0) {
			return true;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append(quote("sessionId", mSessionId)).append(",");
		sb.append(quote("scheduleIds")).append(":[");
		sb.append("{").append(quote("scheduleId", scheduleId)).append("}");
		sb.append("]");
		sb.append("}");

		final String req = sb.toString();

		try {
			String res = sendRequest(API_START_SCHEDULE, req);
			// JSONをデコード
			CommonOnlyResponse cor = JSON.decode(res, CommonOnlyResponse.class);
			if (cor.hasError() == true) {
				log.error("startSchedule(): scheduleId=" + scheduleId + " Error: " + cor.toString());
				return false;
			}
		} catch (JSONException e) {
			log.error("startSchedule(): scheduleId=" + scheduleId + " JSONException: " + e);
			return false;
		} catch (IOException e) {
			log.error("startSchedule(): scheduleId=" + scheduleId + " Exception: " + e);
			return false;
		}
		return true;

	}

	/**
	 * 指定されたスケジュールIDのスケジュールを削除する。
	 *
	 * @param scheduleId
	 */
	public boolean deleteSchedule(final String scheduleId) {
		log.info("deleteSchedule(): scheduleId=" + scheduleId);
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"sessionId\":\"").append(mSessionId).append("\",");
		sb.append("\"scheduleId\":\"").append(scheduleId).append("\",");
		// F句
		sb.append(makeF()).append(",");
		// W句
		sb.append(makeW(scheduleId));
		// 終了
		sb.append("}");
		final String req = sb.toString();

		try {
			String res = sendRequest(API_DELETE_SCHEDULE, req);
			// JSONをデコード
			ScheduleServiceResponse ssr = JSON.decode(res, ScheduleServiceResponse.class);
			if (ssr.hasError() == true) {
				return false;
			}
		} catch (JSONException e) {
			log.error("deleteSchedule(): scheduleId=" + scheduleId + " JSONException: " + e);
			return false;
		} catch (IOException e) {
			log.error("deleteSchedule(): scheduleId=" + scheduleId + " Exception: " + e);
			return false;
		}
		return true;
	}

	private static final String API_GET_SCHEDULE = "getSchedule";
	private static final String API_CREATE_SCHEDULE = "createSchedule";
	private static final String API_DELETE_SCHEDULE = "deleteSchedule";
	private static final String API_CREATE_ONE_TIME_SCHEDULE = "createOneTimeSchedule";
	private static final String API_START_SCHEDULE = "startSchedule";

	/**
	 * スケジュールサービスに対してリクエストを送信する。
	 *
	 * @param api API名
	 * @param req リクエスト(JSON文字列)
	 * @return レスポンスデータ(JSON文字列)
	 * @throws IOException
	 */
	private String sendRequest(final String api, final String req) throws IOException {

//		log.info("[TEST] sendRequest(): api=" + api + " req=" + req); // test
		// リクエスト
		URL u = new URL(mServerUrl + api + "/");
		HttpURLConnection conn = (HttpURLConnection)u.openConnection();
		conn.setConnectTimeout(mConnectTimeout);
		conn.setReadTimeout(mReadTimeout);
		conn.setRequestProperty("Content-Type", "application/json");
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
//		log.info("[TEST] sendRequest(): res=" + res); // test
		return res;
	}

	private String mWorkXmlString = null;

	/**
	 * 元になるXML文字列を登録する<br/>
	 * ※スケジュールサービスのレスポンスをJSONIC・JAXB経由でXMLに変換したもの(ルートノードはschedule)
	 *
	 * @param src
	 */
	public void setXML(final String src) {
		mWorkXmlString = src;
	}

	/**
	 * XML文字列を取得する<br/>
	 * (DB登録用)
	 *
	 * @return
	 */
	public String getXML() {
		return mWorkXmlString;
	}

	/*
	 * ノード名
	 * ※statusはスケジュールサービスで設定されるため、更新対象外とする。
	 */
//	private static final String NodeScheduleId = "scheduleId";
	private static final String NodeRunStartDate = "runStartDate";
	private static final String NodeRunDate = "runDate";
	private static final String NodeSelect = "select";
	private static final String NodePeriod = "period";
	private static final String NodeTimeZone = "timeZone";
	private static final List<String> NodeWeek = Arrays.asList(
			"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"
	);
	private static final String NodeDay = "day";
	private static final String NodeE = "e";
	private static final String NodeRunTime = "runTime";
	private static final String NodeTimeList = "timeList";
	private static final String NodeTime = "time";
	private static final String NodeHour = "hour";
	private static final String NodeMinute = "minute";

//	/**
//	 * スケジュールIDをセットする
//	 *
//	 * @param scheduleId
//	 */
//	public void setScheduleId(final String scheduleId) {
//		mWorkXmlString = replaceXml(mWorkXmlString, NodeScheduleId, makeNode(NodeScheduleId, scheduleId));
//	}


	/**
	 * 1回だけ実行する
	 *
	 */
	public void setScheduleOnce() {
		mWorkXmlString = replaceXml(mWorkXmlString, NodeRunDate, makeNode(NodeRunDate, null));
	}

	/**
	 * 日時を設定<br/>
	 * ※DB内の構成がスケジュールサービスのレスポンスと異なるため、このメソッドは呼出必須
	 *
	 * @param execDateTimestamp
	 */
	public void setScheduleStartDateTime(final long execDateTimestamp, final long hour, final long minute, final String timeZoneId) {
//		log.info("[TEST] setScheduleStartDateTime(): execDateTimestamp=" + execDateTimestamp + " hour=" + hour + " minute=" + minute + " tz=" + timeZoneId);
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+0")); // [Rm#3617] execDateTimestampはGMTベースで年月日がセットされている
		cal.setTimeInMillis(execDateTimestamp);
		final int year = cal.get(Calendar.YEAR);
		final int month = cal.get(Calendar.MONTH) + 1;
		final int mday = cal.get(Calendar.DAY_OF_MONTH);
		final String ymd = String.format("%04d%02d%02d", year, month, mday);
//		log.info("[TEST] setScheduleStartDateTime(): ymd=" + ymd);
//		log.info("[TEST] setScheduleStartDateTime(): xml before=" + mWorkXmlString);
		mWorkXmlString = replaceXml(mWorkXmlString, NodeRunStartDate, makeNode(NodeRunStartDate, ymd));
//		log.info("[TEST] setScheduleStartDateTime(): xml after=" + mWorkXmlString);

		mWorkXmlString = replaceXml(mWorkXmlString, NodeTimeZone, makeNode(NodeTimeZone, timeZoneId));

		StringBuilder sb = new StringBuilder();
		sb.append(makeNode(NodeSelect, "EVEN_TIME_FIXED"));
//		sb.append(makeNode(NodeTimeList, makeNode(NodeE, makeNode(NodeTime, (makeNode(NodeHour, hour) + makeNode(NodeMinute, minute))))));
		sb.append(makeNode(NodeTimeList, makeNode(NodeTime, (makeNode(NodeHour, hour) + makeNode(NodeMinute, minute)))));
		mWorkXmlString = replaceXml(mWorkXmlString, NodeRunTime, makeNode(NodeRunTime, sb.toString()));
//		log.info("[TEST] setScheduleStartDateTime(): xml last=" + mWorkXmlString);
	}

	/**
	 * 毎日実行する
	 */
	public void setScheduleEveryday() {
		mWorkXmlString = replaceXml(mWorkXmlString, NodeRunDate, makeNode(NodeRunDate, makeNode(NodeSelect, "EVEN_DAY") + makeNode(NodePeriod, 1)));
	}

//	public void setScheduleWeek(final String[] wday) {
//		setScheduleWeek(Arrays.asList(wday));
//	}
	/**
	 * 曜日指定で実行する
	 *
	 * @param wday
	 */
	public void setScheduleWeek(final List<String> wday) {
		StringBuilder sb = new StringBuilder();
		sb.append(makeNode(NodeSelect, "EVEN_WEEK"));
		if (wday == null || wday.isEmpty() == true) {
			for (String wk: NodeWeek) {
				sb.append(makeNode(wk, false));
			}
		} else {
			for (String wk: NodeWeek) {
				sb.append(makeNode(wk, wday.contains(wk.toUpperCase())));
			}
		}
		mWorkXmlString = replaceXml(mWorkXmlString, NodeRunDate, makeNode(NodeRunDate, sb.toString()));
	}

//	public void setScheduleMonth(final Long[] mday) {
//		setScheduleMonth(Arrays.asList(mday));
//	}
	/**
	 * 日指定
	 * ※DB内の構成がスケジュールサービスのレスポンスと異なるため、日指定の場合、このメソッドは呼出必須
	 *
	 * @param mday
	 */
	public void setScheduleMonth(final List<Long> mday) {
		StringBuilder sb = new StringBuilder();
		sb.append(makeNode(NodeSelect, "EVEN_MONTH_FIXED"));
		if (mday == null || mday.isEmpty() == true) {
			sb.append(makeNode(NodeDay, null));
		} else {
			StringBuilder sb2 = new StringBuilder();
			for (Long d: mday) {
				if (d != null) {
					sb2.append(makeNode(NodeE, d));
				}
			}
			sb.append(makeNode(NodeDay, sb2.toString()));
		}
		mWorkXmlString = replaceXml(mWorkXmlString, NodeRunDate, makeNode(NodeRunDate, sb.toString()));
	}




	/**
	 *
	 * @param node ノード名
	 * @param value 値
	 * @return
	 */
	private String makeNode(final String node, final Object value) {
		StringBuilder sb = new StringBuilder();
		if (value != null) {
			sb.append("<").append(node).append(">").append(value).append("</").append(node).append(">");
		} else {
			sb.append("<").append(node).append("/>");
		}
		return sb.toString();
	}

	/**
	 *
	 * @param src 元のXML文字列: "&lt;aa&gt;&lt;bb&gt;test&lt;/bb&gt;&lt;/aa&gt;"
	 * @param node 対象ノード名: "b"
	 * @param with 置換するXML文字列(対象ノード名含む): "&lt;bb&gt;hello&lt;/bb&gt;"
	 * @return
	 */
	private String replaceXml(final String src, final String node, final String replacedWith) {
		String xml = src;
		// <node>～</node>を置換する
		int beginIndex = xml.indexOf("<" + node + ">");
		int endIndex = -1;
		if (beginIndex >= 0) {
			endIndex = xml.indexOf("</" + node + ">", beginIndex + ("<" + node + ">").length());
			if (endIndex >= 0) {
				endIndex += ("</" + node + ">").length();
			}
		} else {
			// <node/>を置換する
			beginIndex = xml.indexOf("<" + node + "/>");
			if (beginIndex >= 0) {
				endIndex = beginIndex + ("<" + node + "/>").length();
			}
		}
		if (beginIndex >= 0 && endIndex >= 0) {
			xml = xml.substring(0, beginIndex) + replacedWith + xml.substring(endIndex);
		}
		return xml;
	}



}
