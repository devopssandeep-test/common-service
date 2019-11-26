package srdm.cloud.commonService.domain.repository.schedule;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import net.sf.json.JSONObject;
import srdm.cloud.commonService.domain.model.DeleteLogSetting;
import srdm.cloud.commonService.domain.model.comm.CommonResponse;
import srdm.cloud.commonService.domain.model.comm.scheduleService.CreateRequest;
import srdm.cloud.commonService.domain.model.comm.scheduleService.CreateResponse;
import srdm.cloud.commonService.domain.model.comm.scheduleService.CreateSchedule;
import srdm.cloud.commonService.domain.model.comm.scheduleService.GetRequest;
import srdm.cloud.commonService.domain.model.comm.scheduleService.GetResponse;
import srdm.cloud.commonService.domain.model.comm.scheduleService.Schedule;
import srdm.cloud.commonService.domain.model.comm.scheduleService.SetRequest;
import srdm.cloud.commonService.domain.model.comm.scheduleService.ShutdownRequest;
import srdm.cloud.commonService.domain.model.comm.scheduleService.StartStopRequest;
import srdm.cloud.commonService.util.ScheduleServerInfo;
import srdm.common.constant.SrdmConstants;
import srdm.common.constant.SrdmLogConstants;
import srdm.common.constant.SrdmLogConstants.LogType;
import srdm.common.exception.SrdmDataAccessException;

@Repository
public class ScheduleRepositoryImpl implements ScheduleRepository {

	private static final Logger logger = LoggerFactory.getLogger(ScheduleRepositoryImpl.class);

	@Autowired
	ScheduleServerInfo scheduleServerInfo;

	@Autowired
	RestOperations restOperations;

	/**
	 * 定期ログ削除設定取得
	 */
	@Override
	public DeleteLogSetting findDeleteLogSchedule(LogType logType) throws SrdmDataAccessException {

		String url = "http://" + scheduleServerInfo.getServer() + ":" + scheduleServerInfo.getPort() + "/SrdmCloudScheduleService/getSchedule/";

		// ScheduleServiceへのリクエスト生成
		StringBuilder in = new StringBuilder();
		in.append("/ipau/scheduleList/schedule[systemId='COMMON' and scheduleId='");
		if(logType == SrdmLogConstants.LogType.SYSTEM) {
			in.append(SrdmConstants.SCHEDULEID_DELETE_SYSMGTLOG);			// システム管理ログ
		} else {
			in.append(SrdmConstants.SCHEDULEID_DELETE_OPELOG);				// 操作ログ
		}
		in.append("']")
		.append("");

		GetRequest request = new GetRequest();
		request.setSessionId("dummy-session");
		request.setRClause("$i");
		request.addFcause("$i", in.toString());

		logger.debug("[findDeleteLogSchedule] RequestURL:[{}]",url);
		logger.debug("[findDeleteLogSchedule] RequestParam:[{}]", request.toString());

		GetResponse response;
		try {
			response = restOperations.postForObject(
					url,
					request,
					GetResponse.class);
		} catch (RestClientException e) {
			logger.error("[findDeleteLogSchedule] ScheduleService access error.", e);
			throw new SrdmDataAccessException("ScheduleService access error.", e);
		}

		logger.debug("[findDeleteLogSchedule] response:[{}]",response.toString());

		if(response.getCommon().getErrorList().length > 0) {
			logger.error("[findDeleteLogSchedule] ScheduleService error response.[GET]");
			throw new SrdmDataAccessException("ScheduleService error response.[GET]");
		}

		if(response.getResultData().isEmpty() == true) {
			logger.error("[findDeleteLogSchedule] Schedule not found.[GET]");
			throw new SrdmDataAccessException("Schedule not found.[GET]");
		}
		Schedule schedule = response.getResultData().get(0).getSchedule();
		String period = schedule.getExternalApiJsonParam();
		period = period.substring(period.indexOf(":") + ":".length());
		period = period.substring(0,period.indexOf("&"));
		long startHour = schedule.getRunTime().getTimeList()[0].getTime().getHour().longValue();
		long startMinute = schedule.getRunTime().getTimeList()[0].getTime().getMinute().longValue();
		
		String timeZoneId = schedule.getTimeZone();
		JSONObject hourminuteObj = getConvertedTime(timeZoneId, "Etc/UTC", startHour, startMinute);
		
		boolean execFlag = (SrdmConstants.SCHEDULE_STATUS_RUNNING.equals(schedule.getStatus()) ? true : false);

		DeleteLogSetting deleteLogSetting = new DeleteLogSetting();
		deleteLogSetting.setPeriod(Long.parseLong(period));				// 期間
		deleteLogSetting.setStartHour(hourminuteObj.getLong("hour"));						// 開始時刻（時）
		deleteLogSetting.setStartMinute(hourminuteObj.getLong("minute"));					// 開始時刻（分）
		deleteLogSetting.setExecFlag(execFlag);							// 実行フラグ

		return deleteLogSetting;
	}

	/**
	 * 定期ログ削除設定更新
	 * （get → set → 有効／無効切り替え）
	 */
	@Override
	public void updateDeleteLogSchedule(LogType logType, DeleteLogSetting deleteLogSetting) throws SrdmDataAccessException {

		String baseUrl = "http://" + scheduleServerInfo.getServer() + ":" + scheduleServerInfo.getPort() + "/SrdmCloudScheduleService/";
		String getUrl = baseUrl + "getSchedule/";
		String setUrl = baseUrl + "setSchedule/";
		String scheduleId;
		if(logType == SrdmLogConstants.LogType.SYSTEM) {
			scheduleId = SrdmConstants.SCHEDULEID_DELETE_SYSMGTLOG;			// システム管理ログ
		} else {
			scheduleId = SrdmConstants.SCHEDULEID_DELETE_OPELOG;			// 操作ログ
		}

		// スケジュール取得
		StringBuilder in = new StringBuilder();
		in.append("/ipau/scheduleList/schedule[systemId='COMMON' and scheduleId='")
		.append(scheduleId)
		.append("']")
		.append("");

		GetRequest getRequest = new GetRequest();
		getRequest.setSessionId("dummy-session");
		getRequest.addFcause("$i", in.toString());
		getRequest.setRClause("$i");

		logger.debug("[updateDeleteLogSchedule] RequestURL:[{}]",getUrl);
		logger.debug("[updateDeleteLogSchedule] RequestParam:[{}]", getRequest.toString());

		GetResponse getResponse;
		try {
			getResponse = restOperations.postForObject(
					getUrl,
					getRequest,
					GetResponse.class);
		} catch (RestClientException e) {
			logger.error("[updateDeleteLogSchedule] ScheduleService access error.[GET]", e);
			throw new SrdmDataAccessException("ScheduleService access error.[GET]", e);
		}

		logger.debug("[updateDeleteLogSchedule] response:[{}]",getResponse.toString());

		if(getResponse.getCommon().getErrorList().length > 0) {
			logger.error("[updateDeleteLogSchedule] ScheduleService error response.[GET]");
			throw new SrdmDataAccessException("ScheduleService error response.[GET]");
		}

		// 更新内容反映
		if(getResponse.getResultData().isEmpty() == true) {
			logger.error("[updateDeleteLogSchedule] Schedule not found.[GET]");
			throw new SrdmDataAccessException("Schedule not found.[GET]");
		}
		Schedule schedule = getResponse.getResultData().get(0).getSchedule();
		String timeZoneId = schedule.getTimeZone();
		JSONObject hourminuteObj = getConvertedTime("Etc/UTC", timeZoneId, deleteLogSetting.getStartHour(), deleteLogSetting.getStartMinute());	
		schedule.getRunTime().getTimeList()[0].getTime().setHour(hourminuteObj.getLong("hour"));
		schedule.getRunTime().getTimeList()[0].getTime().setMinute(hourminuteObj.getLong("minute"));
		String externalApiJsonParam = curryBracket(quote("period") + ":" + Long.toString(deleteLogSetting.getPeriod()));
		logger.debug("[updateDeleteLogSchedule] Delete Log Schedule. externalApiJsonParam[{}]", externalApiJsonParam);
		schedule.setExternalApiJsonParam(externalApiJsonParam);
		schedule.getLoginInfo().setDomainId(deleteLogSetting.getDomainId());		// ログイン情報更新
		schedule.getLoginInfo().setAccountId(deleteLogSetting.getAccountId());

		// スケジュール更新
		StringBuilder where = new StringBuilder();
		where.append("$i/systemId='COMMON' and $i/scheduleId='")
		.append(scheduleId)
		.append("'")
		.append("");

		SetRequest setRequest = new SetRequest();
		setRequest.setSessionId("dummy-session");
		setRequest.setScheduleId(scheduleId);
		setRequest.addFcause("$i", "/ipau/scheduleList/schedule");
		setRequest.setWCause(where.toString());
		setRequest.addRcause("$i", schedule);

		logger.debug("[updateDeleteLogSchedule] RequestURL:[{}]",setUrl);
		logger.debug("[updateDeleteLogSchedule] RequestParam:[{}]", setRequest.toString());

		CommonResponse commonResponse;
		try {
			commonResponse = restOperations.postForObject(
					setUrl,
					setRequest,
					CommonResponse.class);
		} catch (RestClientException e) {
			logger.error("[updateDeleteLogSchedule] ScheduleService access error.[SET]", e);
			throw new SrdmDataAccessException("ScheduleService access error.[SET]", e);
		}

		logger.debug("[updateDeleteLogSchedule] response:[{}]",commonResponse.toString());

		if(commonResponse.getCommon().getErrorList().length > 0) {
			logger.error("[updateDeleteLogSchedule] ScheduleService error response.[SET]");
			throw new SrdmDataAccessException("ScheduleService error response.[SET]");
		}

		// スケジュールの開始／停止を設定
		String reqUrl;
		if(deleteLogSetting.isExecFlag() == true) {
			reqUrl = baseUrl + "startSchedule/";
		} else {
			reqUrl = baseUrl + "stopSchedule/";
		}
		StartStopRequest startStopRequest = new StartStopRequest();
		startStopRequest.setSessionId("dummy-session");
		startStopRequest.addScheduleId(scheduleId);

		logger.debug("[updateDeleteLogSchedule] RequestURL:[{}]",reqUrl);
		logger.debug("[updateDeleteLogSchedule] RequestParam:[{}]", startStopRequest.toString());

		try {
			commonResponse = restOperations.postForObject(
					reqUrl,
					startStopRequest,
					CommonResponse.class);
		} catch (RestClientException e) {
			logger.error("[updateDeleteLogSchedule] ScheduleService access error.[SET]", e);
			throw new SrdmDataAccessException("ScheduleService access error.[SET]", e);
		}

		logger.debug("[updateDeleteLogSchedule] response:[{}]",commonResponse.toString());

		if(commonResponse.getCommon().getErrorList().length > 0) {
			logger.error("[updateDeleteLogSchedule] ScheduleService error response.[SET]");
			throw new SrdmDataAccessException("ScheduleService error response.[SET]");
		}
	}

	/**
	 * ScheduleServiceのShtdown依頼
	 * (ScheduleServiceへのリクエストをまとめるときに、Repositoryから削除する）
	 */
	@Override
	public void shutdownScheduleService() throws SrdmDataAccessException {

		String reqUrl = "http://" + scheduleServerInfo.getServer() + ":" + scheduleServerInfo.getPort() + "/SrdmCloudScheduleService/shutdownScheduleService/";

		ShutdownRequest schutdownRequest = new ShutdownRequest();
		schutdownRequest.setSessionId("dummy-session");

		logger.debug("[shutdownScheduleService] RequestURL:[{}]",reqUrl);
		logger.debug("[shutdownScheduleService] RequestParam:[{}]", schutdownRequest.toString());

		CommonResponse commonResponse;
		try {
			commonResponse = restOperations.postForObject(
					reqUrl,
					schutdownRequest,
					CommonResponse.class);
		} catch (RestClientException e) {
			logger.error("[shutdownScheduleService] ScheduleService access error.[Schutdown]", e);
			throw new SrdmDataAccessException("ScheduleService access error.[Schutdown]", e);
		}

		logger.debug("[shutdownScheduleService] response:[{}]",commonResponse.toString());

		if(commonResponse.getCommon().getErrorList().length > 0) {
			logger.error("[shutdownScheduleService] ScheduleService error response.[Schutdown]");
			throw new SrdmDataAccessException("ScheduleService error response.[Schutdown]");
		}
	}

	/**
	 * MIBスケジュールの存在チェック
	 * @return true:スケジュールあり／false:スケジュールなし
	 */
	@Override
	public boolean isExistMibSchedule(String groupId) throws SrdmDataAccessException {

		String url = "http://" + scheduleServerInfo.getServer() + ":" + scheduleServerInfo.getPort() + "/SrdmCloudScheduleService/getSchedule/";

		// ScheduleServiceへのリクエスト生成
		StringBuilder in = new StringBuilder();
		in.append("/ipau/scheduleList/schedule[")
		.append("systemId='MIB' and scheduleName=('discovery','deviceInfo','deviceStatus') and groupId='")
		.append(groupId)
		.append("']")
		.append("");

		StringBuilder rClause = new StringBuilder();
		rClause.append("<scheudle>")
		.append("{$i/scheduleId}")
		.append("</scheudle>");

		GetRequest request = new GetRequest();
		request.setSessionId("dummy-session");
		request.setRClause(rClause.toString());
		request.addFcause("$i", in.toString());

		logger.debug("[isExistMibSchedule] RequestURL:[{}]",url);
		logger.debug("[isExistMibSchedule] RequestParam:[{}]", request.toString());

		GetResponse response;
		try {
			response = restOperations.postForObject(
					url,
					request,
					GetResponse.class);
		} catch (RestClientException e) {
			logger.error("[isExistMibSchedule] ScheduleService access error.", e);
			throw new SrdmDataAccessException("ScheduleService access error.", e);
		}

		logger.debug("[isExistMibSchedule] response:[{}]",response.toString());

		if(response.getCommon().getErrorList().length > 0) {
			logger.error("[isExistMibSchedule] ScheduleService error response.[GET]");
			throw new SrdmDataAccessException("ScheduleService error response.[GET]");
		}

		boolean bRet;
		if(response.getResultData().isEmpty() == true) {
			logger.info("[isExistMibSchedule] MIB Schedule not found. groupId[{}]", groupId);
			bRet = false;
		} else {
			logger.info("[isExistMibSchedule] MIB Schedule already registed. groupId[{}]", groupId);
			bRet = true;
		}

		return bRet;
	}

	/**
	 * MIBスケジュール追加
	 */
	@Override
	public void insertMibSchedule(String groupId, String domainId, String accountId) throws SrdmDataAccessException {

		CreateSchedule createSchedule[] = new CreateSchedule[3];

		// 共通内容の初期化
		for(int i = 0; i < createSchedule.length; i++) {
			createSchedule[i] = new CreateSchedule();
			// 共通
			createSchedule[i].setGroupId(groupId);
			createSchedule[i].setTimeZone(SrdmConstants.getDefaultTimeZoneId());
			createSchedule[i].setSystemId("MIB");
			createSchedule[i].setNoWait(true);

			// 共通：開始日
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
			Date date = new Date();
			String runStartDate = sdf1.format(date);
			createSchedule[i].setRunStartDate(runStartDate);

			// 共通：実行日
			createSchedule[i].getRunDate().setSelect("EVEN_DAY");
			createSchedule[i].getRunDate().setPeriod(Long.parseLong("1"));

			// 共通：実行時刻
			createSchedule[i].getRunTime().setSelect("EVEN_TIME_FIXED");
			createSchedule[i].addRunTimeTimeList(Long.parseLong("0"), Long.parseLong("0"));

			// 共通：認証情報
			createSchedule[i].getLoginInfo().setDomainId(domainId);
			createSchedule[i].getLoginInfo().setAccountId(accountId);
		}

		// デバイス検索
		int index = 0;
		createSchedule[index].setScheduleName("discovery");
		createSchedule[index].setExternalApiPath("/agentManager/discover/startDiscoveryForSchedule/");
		StringBuilder disocoveryParam = new StringBuilder();
		disocoveryParam.append(quote("groupId")).append(":").append(quote(groupId)).append(",")
		.append(quote("setAttribute")).append(":").append(quote("managed"));
		createSchedule[index].setExternalApiJsonParam(curryBracket(disocoveryParam.toString()));
		logger.debug("[insertMibSchedule] (discovery)externalApiJsonParam[{}]", createSchedule[index].getExternalApiJsonParam());
		createSchedule[index].setExecInStartingTimeFlag(false);

		// ステータス更新（update device Info)
		index = 1;
		createSchedule[index].setScheduleName("deviceInfo");
		createSchedule[index].setExternalApiPath("/agentManager/updateStatus/updateStatusItems/updateStatusForSchedule/");
		StringBuilder deviceInfoparam = new StringBuilder();
		deviceInfoparam.append(quote("groupId")).append(":").append(quote(groupId)).append(",")
		.append(quote("deviceIds")).append(":").append("[]").append(",")
		.append(quote("ItemList")).append(":").append("[")
		.append(quote("device")).append(",")
		.append(quote("interface")).append(",")
		.append(quote("service"))
		.append("]");
		createSchedule[index].setExternalApiJsonParam(curryBracket(deviceInfoparam.toString()));
		logger.debug("[insertMibSchedule] (deviceInfo)externalApiJsonParam[{}]", createSchedule[index].getExternalApiJsonParam());
		createSchedule[index].setExecInStartingTimeFlag(false);

		// ステータス更新（update device status)
		index = 2;
		createSchedule[index].setScheduleName("deviceStatus");
		createSchedule[index].setExternalApiPath("/agentManager/updateStatus/updateStatusItems/quickUpdateStatusForSchedule/");
		StringBuilder deviceStatusparam = new StringBuilder();
		deviceStatusparam.append(quote("groupId")).append(":").append(quote(groupId)).append(",")
		.append(quote("deviceIds")).append(":").append("[]");
		createSchedule[index].setExternalApiJsonParam(curryBracket(deviceStatusparam.toString()));
		logger.debug("[insertMibSchedule] (deviceStatus)externalApiJsonParam[{}]", createSchedule[index].getExternalApiJsonParam());
		createSchedule[index].setExecInStartingTimeFlag(false);

		// ScheduleServiceへリクエスト
		for(int i = 0; i < createSchedule.length; i++) {
			createSchedule(createSchedule[i]);
		}
	}

	// スケジュール登録（スケジュールサービスへリクエスト
	private void createSchedule(CreateSchedule createSchedule) throws SrdmDataAccessException {

		String url = "http://" + scheduleServerInfo.getServer() + ":" + scheduleServerInfo.getPort() + "/SrdmCloudScheduleService/createSchedule/";

		logger.info("[createSchedule] schaduleName[{}] groupId[{}]", createSchedule.getScheduleName(), createSchedule.getGroupId());

		CreateRequest request = new CreateRequest();
		request.setSessionId("dummy-session");
		request.addFcause("$i", "/ipau/scheduleList");
		request.addRcause(createSchedule, "$i");

		logger.debug("[createSchedule] RequestURL:[{}]",url);
		logger.debug("[createSchedule] RequestParam:[{}]", request.toString());

		CreateResponse response;
		try {
			response = restOperations.postForObject(
					url,
					request,
					CreateResponse.class);
		} catch (RestClientException e) {
			logger.error("[createSchedule] ScheduleService access error.[SET]", e);
			throw new SrdmDataAccessException("ScheduleService access error.[CREATE]", e);
		}

		logger.debug("[createSchedule] response:[{}]",response.toString());

		if(response.getCommon().getErrorList().length > 0) {
			logger.error("[createSchedule] ScheduleService error response.[SET]");
			throw new SrdmDataAccessException("ScheduleService error response.[CREATE]");
		}
		logger.info("[createSchedule] create schedule scheduleId[{}]", response.getScheduleId());
	}

	/**
	 * 文字列をダブルクォートのエスケープ文字で囲む。
	 *
	 * @param buf
	 * @return
	 */
	private String quote(final String buf) {
		return new StringBuilder().append("&quot;").append(buf).append("&quot;").toString();
	}

	/**
	 * 文字列を波括弧({})のエスケープ文字で囲む。
	 * @param buf
	 * @return
	 */
	private String curryBracket(final String buf) {
		return new StringBuilder().append("&#x7b;").append(buf).append("&#x7d;").toString();
	}

	/***
	 * 
	 * @param FrmTimeZone
	 * @param toTimeZone
	 * @param hours
	 * @param minute
	 * @return
	 */
		public JSONObject getConvertedTime(String FrmTimeZone, String toTimeZone, long hours, long minute) {
			JSONObject timeJso = new JSONObject();
			Calendar InitialTimeZoneTime = new GregorianCalendar(TimeZone.getTimeZone(FrmTimeZone));
			InitialTimeZoneTime.set(Calendar.HOUR_OF_DAY, (int)hours);
			InitialTimeZoneTime.set(Calendar.MINUTE, (int)minute);

			Calendar convertedTimeZoneTime = new GregorianCalendar(TimeZone.getTimeZone(toTimeZone));
			convertedTimeZoneTime.setTimeInMillis(InitialTimeZoneTime.getTimeInMillis());

			timeJso.put("hour", Long.valueOf(convertedTimeZoneTime.get(Calendar.HOUR_OF_DAY)));
			timeJso.put("minute", Long.valueOf(convertedTimeZoneTime.get(Calendar.MINUTE)));

			return timeJso;

		}
}
