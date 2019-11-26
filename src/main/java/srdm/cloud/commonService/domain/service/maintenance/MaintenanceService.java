package srdm.cloud.commonService.domain.service.maintenance;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXB;

import org.codehaus.jettison.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.srdm.mongodb.constants.GlobalStrings;
import com.srdm.mongodb.service.IDBRequest;
import com.srdm.mongodb.serviceImpl.DBRequest;

import srdm.cloud.commonService.app.bean.maintenance.GetMaintenanceInfoResBean;
import srdm.cloud.commonService.app.bean.maintenance.GetMaintenanceStatusResBean;
import srdm.cloud.commonService.app.bean.maintenance.GetScheduledMaintenanceSettingsResBean;
import srdm.cloud.commonService.app.bean.maintenance.SetMaintenanceInfoReqBean;
import srdm.cloud.commonService.app.bean.maintenance.SetMaintenanceInfoResBean;
import srdm.cloud.commonService.app.bean.maintenance.SetScheduledMaintenanceSettingsReqBean;
import srdm.cloud.commonService.app.bean.maintenance.SetScheduledMaintenanceSettingsResBean;
import srdm.cloud.commonService.domain.model.LogItem;
import srdm.cloud.commonService.domain.model.comm.scheduleService.Schedule;
import srdm.cloud.commonService.domain.repository.session.SrdmSessionRepository;
import srdm.cloud.commonService.domain.service.log.SysMgtLogWriteService;
import srdm.cloud.commonService.util.optimization.ScheduleServiceUtil;
import srdm.cloud.commonService.util.optimization.ScheduledMaintenanceUtil;
import srdm.cloud.shared.system.MaintenanceStatus;
import srdm.common.constant.SrdmConstants;
import srdm.common.constant.SrdmLogConstants;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmParameterValidationException;

@Service
public class MaintenanceService {
	private static final Logger logger = LoggerFactory.getLogger(MaintenanceService.class);

	public static final String SERVICE_MODE = "service";
	public static final String MAINTENANCE_MODE = "maintenance";
	public static final long MTCODE_OK = 0;
	public static final long MTCODE_RECOVERED = 1;
	public static final long MTCODE_RUNNING = 101;
	public static final long MTCODE_FAILED = 102;

	String DB_MAINTENANCE_INFO_MSG_PATH = "systemSettings.maintenanceInfo.message";
	String DB_MAINTENANCE_INFO_PATH = "systemSettings.scheduledMaintenanceInfo";

	@Autowired
	SrdmSessionRepository srdmSessionRepository;
	@Autowired
	SysMgtLogWriteService sysMgtLogWriteService;

	/**
	 * メンテナンス状態を返す
	 *
	 * @return
	 */
	public GetMaintenanceStatusResBean getMaintenanceStatus() {
		final int statusCode = MaintenanceStatus.getStatusCode();

		String status;
		long code;
		switch (statusCode) {
		case MaintenanceStatus.IDLE:
			status = SERVICE_MODE;
			code = MTCODE_OK;
			break;
		case MaintenanceStatus.RUNNING:
			status = MAINTENANCE_MODE;
			code = MTCODE_RUNNING;
			break;
		case MaintenanceStatus.ERROR:
			status = SERVICE_MODE;
			code = MTCODE_RECOVERED;
			break;
		case MaintenanceStatus.FATAL:
			status = MAINTENANCE_MODE;
			code = MTCODE_FAILED;
			break;
		default:
			status = "unknown";
			code = 999;
			logger.error("getMaintenanceStatusMessage(): Status=" + statusCode);
			break;
		}

		GetMaintenanceStatusResBean resBean = new GetMaintenanceStatusResBean();
		resBean.setStatus(status);
		resBean.setMaintenanceCode(code);
		return resBean;
	}

	/**
	 * メンテナンス情報を返す
	 *
	 * @return
	 * @throws SrdmDataAccessException
	 */
	public GetMaintenanceInfoResBean getMaintenanceInfo() throws SrdmDataAccessException {
		GetMaintenanceInfoResBean resBean = new GetMaintenanceInfoResBean();

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		BasicDBObject searchCondition = new BasicDBObject();
		searchCondition.append("systemSettings.maintenanceInfo", new BasicDBObject("$exists", true));

		// Form DB request query
		BasicDBObject dbOutputObj = new BasicDBObject();
		dbOutputObj.append("_id", 0).append(DB_MAINTENANCE_INFO_MSG_PATH, 1);

		String message = null;
		try {
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.IPAU_SYSTEM_SETTINGS_COLLECTION, 0, 0, searchCondition,
					dbOutputObj, new BasicDBObject());
			if (listResult.size() > 0) {
				JSONArray jsonarray = new JSONArray(listResult.toString());
				message = jsonarray.getJSONObject(0).getJSONObject("systemSettings").getJSONObject("maintenanceInfo")
						.get("message").toString();
			} else {
				message = "";
			}
		} catch (MongoException e) {
			logger.error("getMaintenanceInfo(): " + e);
			throw new SrdmDataAccessException("Maintenance message get error.");
		} catch (Exception e) {
			logger.error("getMaintenanceInfo(): " + e);
			throw new SrdmDataAccessException("Maintenance message get error.");
		}
		resBean.setMessage(message);
		return resBean;
	}

	/**
	 * メンテナンス情報を登録する
	 *
	 * @param reqDto
	 * @return
	 * @throws SrdmDataAccessException
	 */
	public SetMaintenanceInfoResBean setMaintenanceInfo(SetMaintenanceInfoReqBean reqBean)
			throws SrdmDataAccessException {
		final String valueMessage = reqBean.getMessage().trim(); // reqBean,
																	// reqBean.messageは、呼出元でnullチェック済み
		SetMaintenanceInfoResBean resBean = new SetMaintenanceInfoResBean();

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbOutputObj = new BasicDBObject();
		dbOutputObj.append("_id", 0);

		BasicDBObject searchCondition = new BasicDBObject();
		searchCondition.append("systemSettings.maintenanceInfo", new BasicDBObject("$exists", true));

		// update value
		BasicDBObject updateFields = new BasicDBObject(DB_MAINTENANCE_INFO_MSG_PATH, valueMessage);

		BasicDBObject setQueryObj = new BasicDBObject();
		setQueryObj.append(GlobalStrings.OPERATOR_SET, updateFields);

		Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
		collectionsMap.put(GlobalStrings.IPAU_SYSTEM_SETTINGS_COLLECTION, setQueryObj);

		try {

			dbRequest.updateDB(searchCondition, collectionsMap, false, false);
		} catch (MongoException e) {
			logger.error("setMaintenanceInfo(): " + e);
			throw new SrdmDataAccessException("Maintenance message set error.", e);
		} catch (Exception e) {
			logger.error("setMaintenanceInfo(): " + e);
			throw new SrdmDataAccessException("Maintenance message set error.", e);
		}
		return resBean;
	}

	/**
	 * 定期メンテナンス設定情報を取得する
	 *
	 * @return
	 * @throws SrdmDataAccessException
	 */
	public GetScheduledMaintenanceSettingsResBean getScheduledMaintenanceSettings() throws SrdmDataAccessException {
		GetScheduledMaintenanceSettingsResBean resBean = new GetScheduledMaintenanceSettingsResBean();

		// DB object creation
		IDBRequest dbRequest = new DBRequest();
		BasicDBObject searchCondition = new BasicDBObject();
		searchCondition.append("systemSettings.scheduledMaintenanceInfo", new BasicDBObject("$exists", true));

		// Form DB request query
		BasicDBObject dbOutputObj = new BasicDBObject();
		dbOutputObj.append("_id", 0).append(DB_MAINTENANCE_INFO_PATH, 1);
		ScheduledMaintenanceUtil smi;

		try {
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.IPAU_SYSTEM_SETTINGS_COLLECTION, 0, 0, searchCondition,
					dbOutputObj, new BasicDBObject());

			if (listResult.size() > 0) {
				ObjectMapper mapper = new ObjectMapper();
				JSONArray jsonarray = new JSONArray(listResult.toString());
				String scheduledMaintenanceInfo = jsonarray.getJSONObject(0).getJSONObject("systemSettings")
						.getJSONObject("scheduledMaintenanceInfo").toString();

				smi = mapper.readValue(scheduledMaintenanceInfo, ScheduledMaintenanceUtil.class);

			} else {
				smi = new ScheduledMaintenanceUtil();
				smi.setExecFlag(false);
				smi.setSendFlag(false);
				smi.setExecType("weekDay");
				ArrayList<String> weekDayList = new ArrayList<String>();
				weekDayList.add("SUNDAY");
				smi.setWeekDay(weekDayList.toArray(new String[weekDayList.size()]));
				smi.setMonthDate("");
				smi.setExecDateTimestamp(System.currentTimeMillis());
				smi.setExecTimeHour(7l);
				smi.setExecTimeMinute(30l);
				smi.setTimeZone(SrdmConstants.getDefaultTimeZoneId());
				smi.setDateTimeFormat(SrdmConstants.getDefaultDateTimeFormat());
				smi.setLanguage(SrdmConstants.getDefaultLanguage());
				smi.setToAddress("");
				smi.setCcAddress("");
				smi.setBccAddress("");

				logger.warn("systemSettings.scheduledMaintenanceInfo is not exsists. responsed fixed vale."
						+ " and create systemSettings.scheduledMaintenanceInfo node.");
				logger.warn("systemSettings.scheduledMaintenanceInfo[" + smi + "]");

				// search condition
				BasicDBObject updateCondition = new BasicDBObject();
				updateCondition.append("systemSettings", new BasicDBObject("$exists", true));

				// update value
				BasicDBObject setObj = new BasicDBObject();
				setObj.append("execFlag", smi.getExecFlag());
				setObj.append("sendFlag", smi.isSendFlag());
				setObj.append("execType", smi.getExecType());
				setObj.append("weekDay", smi.getWeekDay());
				setObj.append("monthDate", smi.getMonthDate());
				setObj.append("execDateTimestamp", smi.getExecDateTimestamp());
				setObj.append("execTimeHour", smi.getExecTimeHour());
				setObj.append("execTimeMinute", smi.getExecTimeMinute());
				setObj.append("timeZone", smi.getTimeZone());
				setObj.append("dateTimeFormat", smi.getDateTimeFormat());
				setObj.append("language", smi.getLanguage());
				setObj.append("toAddress", smi.getToAddress());
				setObj.append("ccAddress", smi.getCcAddress());
				setObj.append("bccAddress", smi.getBccAddress());

				BasicDBObject updateFields = new BasicDBObject(DB_MAINTENANCE_INFO_PATH, setObj);

				BasicDBObject setQueryObj = new BasicDBObject();
				setQueryObj.append(GlobalStrings.OPERATOR_SET, updateFields);

				Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
				collectionsMap.put(GlobalStrings.IPAU_SYSTEM_SETTINGS_COLLECTION, setQueryObj);

				try {

					dbRequest.updateDB(updateCondition, collectionsMap, true, false);
				} catch (MongoException e) {
					logger.error("getScheduledMaintenanceSettings(): ", e);
					throw new SrdmDataAccessException("ScheduleMaintenance default value set error.", e);
				} catch (Exception e) {
					logger.error("getScheduledMaintenanceSettings(): ", e);
					throw new SrdmDataAccessException("ScheduleMaintenance default value set error.", e);
				}

			}

			resBean.setExecFlag(smi.execFlag);
			resBean.setSendFlag(smi.sendFlag);
			resBean.setExecType(smi.execType);
			if (smi.weekDay != null) {
				String[] tmp = smi.weekDay;
				List<String> list = new ArrayList<String>();
				for (String s : tmp) {
					if (s == null || s.trim().length() == 0) {
						continue;
					}
					list.add(s);
				}
				resBean.setWeekDay(list);
			}
			if (smi.monthDate != null) {
				String[] tmp = smi.monthDate.split(" ");
				List<Long> list = new ArrayList<Long>();
				for (String s : tmp) {
					if (s == null || s.trim().length() == 0) {
						continue;
					}
					try {
						list.add(Long.parseLong(s));
					} catch (NumberFormatException e) {
						continue;
					}
				}
				resBean.setMonthDate(list);
			}
			resBean.setExecDateTimestamp(smi.execDateTimestamp);
			resBean.setExecTimeHour(smi.execTimeHour);
			resBean.setExecTimeMinute(smi.execTimeMinute);
			resBean.setTimeZoneId(smi.timeZone);
			resBean.setDateTimeFormat(smi.dateTimeFormat);
			resBean.setLanguage(smi.language);
			resBean.setToAddress(smi.toAddress);
			resBean.setCcAddress(smi.ccAddress);
			resBean.setBccAddress(smi.bccAddress);
		} catch (MongoException e) {
			logger.error("getScheduledMaintenanceSettings(): " + e);
			throw new SrdmDataAccessException("ScheduledMaintenanceSetting get error.", e);
		} catch (Exception e) {
			logger.error("getScheduledMaintenanceSettings(): " + e);
			throw new SrdmDataAccessException("ScheduledMaintenanceSetting get error.", e);
		}

		return resBean;
	}

	/**
	 * 定期メンテナンス設定情報を設定する
	 *
	 * @param sessionId
	 * @param reqBean
	 * @return
	 * @throws SrdmParameterValidationException
	 * @throws SrdmDataAccessException
	 */
	public SetScheduledMaintenanceSettingsResBean setScheduledMaintenanceSettings(String sessionId,
			SetScheduledMaintenanceSettingsReqBean reqBean)
			throws SrdmDataAccessException, SrdmParameterValidationException {
		SetScheduledMaintenanceSettingsReqBean req = reqBean;

		if (req.getWeekDay() != null && req.getWeekDay().size() > 0) {
			// ユニーク化
			List<String> listWeekDay = SrdmConstants.getListWeekDay();
			List<String> listUniq = new ArrayList<String>();
			for (String wday : listWeekDay) {
				if (req.getWeekDay().contains(wday) == true) {
					listUniq.add(wday);
				}
			}
			req.setWeekDay(listUniq);
		}
		if (req.getMonthDate() != null && req.getMonthDate().size() > 0) {
			// ユニーク化＆ソート
			List<Long> listUniq = new ArrayList<Long>();
			for (long d = 1; d <= 31; d++) {
				if (req.getMonthDate().contains(d) == true) {
					listUniq.add(d);
				}
			}
			req.setMonthDate(listUniq);
		}
		if (isBlankString(req.getTimeZoneId()) == true) {
			req.setTimeZoneId(SrdmConstants.getDefaultTimeZoneId());
		}
		if (isBlankString(req.getLanguage()) == true) {
			req.setLanguage(SrdmConstants.getDefaultLanguage());
		}
		if (isBlankString(req.getDateTimeFormat()) == true) {
			req.setDateTimeFormat(SrdmConstants.getDefaultDateTimeFormat());
		}
		if (req.getToAddress() != null) {
			req.setToAddress(req.getToAddress().trim());
		}
		if (req.getCcAddress() != null) {
			req.setCcAddress(req.getCcAddress().trim());
		}
		if (req.getBccAddress() != null) {
			req.setBccAddress(req.getBccAddress().trim());
		}

		SetScheduledMaintenanceSettingsResBean resBean = null;
		try {
			resBean = updateScheduleService(sessionId, req);
			resBean = updateSysSettings(req);

			List<LogItem> itemList = new ArrayList<LogItem>();
			boolean execFlag = Boolean.parseBoolean(reqBean.getExecFlag());
			String isEnable = (execFlag ? SrdmLogConstants.SYSMGT_ITEM_VALUE_ENABLE : SrdmLogConstants.SYSMGT_ITEM_VALUE_DISABLE);
			itemList.add(new LogItem(SrdmLogConstants.SYSMGT_ITEM_NAME_OPTMIZE_SETTING_STATE, isEnable));
				sysMgtLogWriteService.writeSystemManagementLog(
						sessionId,
						SrdmLogConstants.SYSMGT_OPERATION_DB_OPTIMIZE_SETTING,
						SrdmLogConstants.SYSMGT_CODE_NORMAL,
						itemList);
		} catch (SrdmDataAccessException e) {
			sysMgtLogWriteService.writeSystemManagementLog(sessionId,
					SrdmLogConstants.SYSMGT_OPERATION_DB_OPTIMIZE_SETTING, 201); // "02xx"はDB最適化設定のエラーコード
			throw e;
		}

		return resBean;
	}

	/**
	 * iPAUSysSettingsDBへの登録
	 *
	 * @param reqBean
	 * @return
	 * @throws SrdmDataAccessException
	 */
	private SetScheduledMaintenanceSettingsResBean updateSysSettings(SetScheduledMaintenanceSettingsReqBean reqBean)
			throws SrdmDataAccessException {
		SetScheduledMaintenanceSettingsResBean res = new SetScheduledMaintenanceSettingsResBean();

		boolean execFlag = Boolean.parseBoolean(reqBean.getExecFlag());
		boolean sendFlag = Boolean.parseBoolean(reqBean.getSendFlag());

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbOutputObj = new BasicDBObject();
		dbOutputObj.append("_id", 0);

		BasicDBObject searchCondition = new BasicDBObject();
		searchCondition.append("systemSettings.scheduledMaintenanceInfo", new BasicDBObject("$exists", true));

		BasicDBObject updateFields = new BasicDBObject();

		updateFields.append("systemSettings.scheduledMaintenanceInfo.execFlag", execFlag);

		if (isBlankString(reqBean.getExecType()) == false) {
			updateFields.append("systemSettings.scheduledMaintenanceInfo.execType", reqBean.getExecType());
		} else {
			updateFields.append("systemSettings.scheduledMaintenanceInfo.execType", "");
		}
		if (isBlankListString(reqBean.getWeekDay()) == false && reqBean.getWeekDay().size() > 0) {
			List<String> arrWeekDay = reqBean.getWeekDay();
			updateFields.append("systemSettings.scheduledMaintenanceInfo.weekDay", arrWeekDay);
		} else {
			List<String> arrWeekDay = new ArrayList<>();
			updateFields.append("systemSettings.scheduledMaintenanceInfo.weekDay", arrWeekDay);
		}
		if (isBlankListLong(reqBean.getMonthDate()) == false && reqBean.getMonthDate().size() > 0) {
			updateFields.append("systemSettings.scheduledMaintenanceInfo.monthDate", reqBean.getMonthDate());
		} else {
			updateFields.append("systemSettings.scheduledMaintenanceInfo.monthDate", "");
		}
		if (reqBean.getExecDateTimestamp() != null) {
			updateFields.append("systemSettings.scheduledMaintenanceInfo.execDateTimestamp",
					reqBean.getExecDateTimestamp());
		} else {
			updateFields.append("systemSettings.scheduledMaintenanceInfo.execDateTimestamp", "");
		}
		if (reqBean.getExecTimeHour() != null && reqBean.getExecTimeMinute() != null) {
			// hourとminuteの両方がセットされている場合のみ更新する
			updateFields.append("systemSettings.scheduledMaintenanceInfo.execTimeHour", reqBean.getExecTimeHour());
			updateFields.append("systemSettings.scheduledMaintenanceInfo.execTimeMinute", reqBean.getExecTimeMinute());
		} else {
			updateFields.append("systemSettings.scheduledMaintenanceInfo.execTimeHour", "");
			updateFields.append("systemSettings.scheduledMaintenanceInfo.execTimeMinute", "");
		}
		if (isBlankString(reqBean.getTimeZoneId()) == false) {
			updateFields.append("systemSettings.scheduledMaintenanceInfo.timeZone", reqBean.getTimeZoneId());
		} else {
			// デフォルトがセットされているため、ここには入らないはず
			updateFields.append("systemSettings.scheduledMaintenanceInfo.timeZone", "");
		}
		if (isBlankString(reqBean.getDateTimeFormat()) == false) {
			updateFields.append("systemSettings.scheduledMaintenanceInfo.dateTimeFormat", reqBean.getDateTimeFormat());
		} else {
			// デフォルトがセットされているため、ここには入らないはず
			updateFields.append("systemSettings.scheduledMaintenanceInfo.dateTimeFormat", "");
		}
		if (isBlankString(reqBean.getLanguage()) == false) {
			updateFields.append("systemSettings.scheduledMaintenanceInfo.language", reqBean.getLanguage());
		} else {
			// デフォルトがセットされているため、ここには入らないはず
			updateFields.append("systemSettings.scheduledMaintenanceInfo.language", "");
		}
		if (reqBean.getToAddress() != null) {
			updateFields.append("systemSettings.scheduledMaintenanceInfo.toAddress", reqBean.getToAddress());
		} else {
			updateFields.append("systemSettings.scheduledMaintenanceInfo.toAddress", "");
		}
		if (reqBean.getCcAddress() != null) {
			updateFields.append("systemSettings.scheduledMaintenanceInfo.ccAddress", reqBean.getCcAddress());
		} else {
			updateFields.append("systemSettings.scheduledMaintenanceInfo.ccAddress", "");
		}
		if (reqBean.getBccAddress() != null) {
			updateFields.append("systemSettings.scheduledMaintenanceInfo.bccAddress", reqBean.getBccAddress());
		} else {
			updateFields.append("systemSettings.scheduledMaintenanceInfo.bccAddress", "");
		}
		updateFields.append("systemSettings.scheduledMaintenanceInfo.sendFlag", sendFlag);

		BasicDBObject setQueryObj = new BasicDBObject();
		setQueryObj.append(GlobalStrings.OPERATOR_SET, updateFields);
		Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
		collectionsMap.put(GlobalStrings.IPAU_SYSTEM_SETTINGS_COLLECTION, setQueryObj);
		try {
			dbRequest.updateDB(searchCondition, collectionsMap, false, false);
		} catch (MongoException e) {
			logger.error("setScheduledMaintenanceSettings(): " + e);
			throw new SrdmDataAccessException("ScheduledMaintenanceSettings set error.", e);
		} catch (Exception e) {
			logger.error("setScheduledMaintenanceSettings(): " + e);
			throw new SrdmDataAccessException("ScheduledMaintenanceSettings set error.", e);
		}
		return res;
	}

	/**
	 * スケジュールサービスへの登録(iPAUScheduleDB)
	 *
	 * @param sessionId
	 * @param reqBean
	 * @return
	 * @throws SrdmDataAccessException
	 * @throws SrdmParameterValidationException
	 */
	private SetScheduledMaintenanceSettingsResBean updateScheduleService(String sessionId,
			SetScheduledMaintenanceSettingsReqBean reqBean)
			throws SrdmDataAccessException, SrdmParameterValidationException {
		SetScheduledMaintenanceSettingsResBean resBean = new SetScheduledMaintenanceSettingsResBean();

		boolean execFlag = Boolean.parseBoolean(reqBean.getExecFlag());
		final Long groupId = 0L; // Top Group固定
		ScheduleServiceUtil ssu = new ScheduleServiceUtil(sessionId);
		String xml = null;
		int mode = ScheduleServiceUtil.MODE_REPEAT;
		if (execFlag == true) {
			// スケジュールサービス経由で取得
			xml = ssu.getSchedule();

			if (xml == null) {
				// デフォルトのスケジュールを作成
				Schedule s = new Schedule();
				s.setScheduleId(""); // スケジュールサービスでセットする
				s.setScheduleName(ScheduleServiceUtil.ScheduleName);
				s.setGroupId(groupId);
				s.setTimeZone(SrdmConstants.getDefaultTimeZoneId());
				s.setSystemId(ScheduleServiceUtil.SystemId);
				s.setStatus(""); // スケジュールサービスでセットする
				s.setNoWait(true);
				s.setExternalApiJsonParam(""); // パラメータなし
				s.setExternalApiPath("/commonService/maintenance/startOptimization/");
				s.setRunStartDate(""); // 後でセットする
				s.setRunDate(new Schedule.RunDate()); // 後で書き換える
				s.setRunTime(new Schedule.RunTime()); // 後で書き換える
				s.setLoginInfo(new Schedule.LoginInfo());
				s.getLoginInfo().setDomainId(srdmSessionRepository.getDomainId(sessionId));
				s.getLoginInfo().setAccountId(srdmSessionRepository.getAccountId(sessionId));
				// XMLに変換
				StringWriter sw = new StringWriter();
				JAXB.marshal(s, sw);
				xml = sw.toString();
			} else {
				if (xml.contains("[SCHEDULE SERVICE]") && xml.contains("errorCode") && xml.contains("errorMessage")) { // TODO:
																														// 暫定→errorList参照
					logger.error("updateScheduleService(): Schedule Service Read Error: " + xml);
					throw new SrdmDataAccessException("SS", "R", "Schedule Service Read Error.");
				}
			}

			// 編集
			ssu.setXML(xml);
			// runStartDate, runTime, timeZoneを書き換える
			ssu.setScheduleStartDateTime(reqBean.getExecDateTimestamp(), reqBean.getExecTimeHour(),
					reqBean.getExecTimeMinute(), reqBean.getTimeZoneId());
			// runDateを書き換える
			if ("once".equals(reqBean.getExecType()) == true) {
				ssu.setScheduleOnce();
				mode = ScheduleServiceUtil.MODE_ONCE;
			} else if ("everyday".equals(reqBean.getExecType()) == true) {
				ssu.setScheduleEveryday();
			} else if ("weekDay".equals(reqBean.getExecType()) == true) {
				List<String> strWeekDay = reqBean.getWeekDay();
				ssu.setScheduleWeek(strWeekDay);
			} else if ("monthDate".equals(reqBean.getExecType()) == true) {
				ssu.setScheduleMonth(reqBean.getMonthDate());
			} else {
				// パラメータチェック実施済みのため、ここには入らないはず
				logger.error("updateScheduleService(): invalid execType=" + reqBean.getExecType());
				throw new SrdmParameterValidationException("EXECTYPE", reqBean.getExecType(), "Range error");
			}
			xml = ssu.getXML();
		} else {
			mode = ScheduleServiceUtil.MODE_STOP;
		}

		// スケジュールサービス反映
		boolean bResult = ssu.updateSchedule(xml, mode);
		if (bResult == false) {
			// プログラムエラー、DBエラー、スケジュールサービス通信エラー等
			logger.error("updateScheduleService(): Schedule Service Write Error: " + xml);
			throw new SrdmDataAccessException("SS", "W", "Schedule Service Write Error.");
		}

		return resBean;
	}

	/**
	 * null、""、"(空白)"であればtrueを返す
	 *
	 * @param value
	 * @return
	 */
	private boolean isBlankString(final String value) {
		return ((value == null) || (value.trim().length() == 0));
	}

	private boolean isBlankListString(final List<String> list) {
		return (list == null || list.isEmpty() == true);
	}

	private boolean isBlankListLong(final List<Long> list) {
		return (list == null || list.isEmpty() == true);
	}
}
