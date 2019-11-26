package srdm.cloud.commonService.util.optimization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.srdm.mongodb.constants.GlobalStrings;
import com.srdm.mongodb.service.IDBRequest;
import com.srdm.mongodb.serviceImpl.DBRequest;

import lombok.ToString;

/**
 * DB最適化処理の後処理
 *
 */
public class TcoOptimization {

	private Logger logSimple = null;
	private Logger logDetail = null;
	IDBRequest dbRequest = null;
	BasicDBObject projectFields = null;
	private static Object mLock = new Object();

	private List<String> mListErrorDB = new ArrayList<String>();

	public TcoOptimization(final Logger simpleLogger, final Logger detailLogger) {
		logSimple = simpleLogger;
		logDetail = detailLogger;
	}

	/**
	 * TCOのDBのステータス/結果を調整する<br/>
	 *
	 * @return
	 */
	public boolean update() {
		synchronized (mLock) {
			boolean bRet = true;

			DBCheckUtil dbcUtil = new DBCheckUtil();
			if (updateTcoCloningFileInfoDB(dbcUtil) == false) {
				mListErrorDB.add(GlobalStrings.TCO_CLONING_FILE_INFO_COLLECTION);
				bRet = false;
			}

			return bRet;
		}
	}

	/**
	 * エラーが発生したDBのリストを返す(エラーなしの場合はsize()=0)
	 *
	 * @return
	 */
	public List<String> getErrorDBs() {
		return mListErrorDB;
	}

	/**
	 * tcoCloningFileInfoDBのステータス/結果を調整する
	 *
	 * @param dbcUtil
	 * @return
	 */
	private boolean updateTcoCloningFileInfoDB(final DBCheckUtil dbcUtil) {
		final String dbName = GlobalStrings.TCO_CLONING_FILE_INFO_COLLECTION;
		boolean bRet = true;
		if (updateTcoCloningFileInfoDBLatestExecutionStatus(dbName) == false) {
			bRet = false;
		}
		if (updateTcoCloningFileInfoDBScheduleStatus(dbName) == false) {
			bRet = false;
		}
		return bRet;
	}

	/**
	 * tcoCloningFileInfoDBのcloningFileInfoの一部
	 *
	 */
	@ToString
	private static class CloningFileInfo {
		public String groupId;
		public String fileId;
		public String fileName;
		public String latestExecutionStatus;

	}

	/**
	 * tcoCloningFileInfoDBのlatestExecutionStatusを更新する。<br/>
	 * <br/>
	 * latestExecutionStatus:<br/>
	 * 1(クローニング実行中) → 9(終了(メンテナンスによる中断))<br/>
	 *
	 * @return
	 */
	private boolean updateTcoCloningFileInfoDBLatestExecutionStatus(String dbName) {
		writeSimpleInfoLog("TCO: Target DB: " + dbName + " (ExecStatus)");
		writeDetailInfoLog("TCO: Target DB: " + dbName + " (ExecStatus)");
		dbRequest = new DBRequest();

		// latestExecutionStatusはStringの場合とIntの場合があるため、両方チェックする
		BasicDBObject latestExecutionStatusInt = new BasicDBObject("cloningFileInfo.latestExecutionStatus", 1);
		BasicDBObject latestExecutionStatusStr = new BasicDBObject("cloningFileInfo.latestExecutionStatus", "1");
		BasicDBList latestExecutionStatusList = new BasicDBList();
		latestExecutionStatusList.add(latestExecutionStatusInt);
		latestExecutionStatusList.add(latestExecutionStatusStr);
		BasicDBObject latestExecutionStatusCondition = new BasicDBObject(GlobalStrings.OPERATOR_OR, latestExecutionStatusList);

		projectFields = new BasicDBObject();
		projectFields.append("cloningFileInfo.groupId", 1).append("cloningFileInfo.fileId", 1)
				.append("cloningFileInfo.fileName", 1).append("cloningFileInfo.latestExecutionStatus", 1)
				.append("_id", 0);
		List<String> listResult = null;
		final int newStatus = 9;
		try {
			listResult = dbRequest.readFromDB(dbName, 0, 0, latestExecutionStatusCondition, projectFields,
					new BasicDBObject());

			if (listResult.size() != 0) {
				ObjectMapper mapper = new ObjectMapper();
				JSONArray jsonarray = new JSONArray(listResult.toString());
				for (int i = 0; i < jsonarray.length(); i++) {
					if (jsonarray.get(i) == null) {
						continue;
					}
					String cloningFileInfo = jsonarray.getJSONObject(i).getJSONObject("cloningFileInfo").toString();
					CloningFileInfo info = mapper.readValue(cloningFileInfo, CloningFileInfo.class);

					if (info == null) {
						continue;
					}
					writeDetailInfoLog("TCO: " + dbName + " (ExecStatus): Target File: groupId=" + info.groupId
							+ " fileId=" + info.fileId + " fileName=" + info.fileName + " latestExecutionStatus="
							+ info.latestExecutionStatus + "->" + newStatus);
				}
			}

		} catch (MongoException e) {
			writeSimpleErrorLog("TCO: DB Read Error: " + dbName + " (ExecStatus)");
			writeDetailErrorLog("TCO: DB Read Error: " + dbName + " (ExecStatus)" + ": " + e, e.getStackTrace());
			return false;
		} catch (Exception e) {
			writeSimpleErrorLog("TCO: DB Read Error: " + dbName + " (ExecStatus)");
			writeDetailErrorLog("TCO: DB Read Error: " + dbName + " (ExecStatus)" + ": " + e, e.getStackTrace());
			return false;
		}

		if (listResult != null && listResult.size() > 0) {

			dbRequest = new DBRequest();

			Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
			BasicDBObject updateFieldsObj = new BasicDBObject();
			updateFieldsObj.append("cloningFileInfo.latestExecutionStatus", newStatus);
			BasicDBObject setQueryObj = new BasicDBObject();
			setQueryObj.append(GlobalStrings.OPERATOR_SET, updateFieldsObj);
			collectionsMap.put(dbName, setQueryObj);

			try {
				dbRequest.updateDB(latestExecutionStatusCondition, collectionsMap, false, true);
			} catch (MongoException e) {
				writeSimpleErrorLog("TCO: DB Update Error: " + dbName + " (ExecStatus)");
				writeDetailErrorLog("TCO: DB Update Error: " + dbName + " (ExecStatus)" + ": " + e, e.getStackTrace());
				return false;
			}
		}

		return true;
	}

	/**
	 * tcoCloningFileInfoDBのscheduleStatusを更新する。<br/>
	 * <br/>
	 * scheduleStatus:<br/>
	 * 1(実行中), 3(リトライ待機中), 4(リトライ中) → 9(メンテナンスによる中断)<br/>
	 * リトライ待機中の場合は、スケジュールサービスからも削除する<br/>
	 *
	 * @param dbName
	 * @return
	 */
	private boolean updateTcoCloningFileInfoDBScheduleStatus(final String dbName) {
		writeSimpleInfoLog("TCO: Target DB: " + dbName + " (ScheduleStatus)");
		writeDetailInfoLog("TCO: Target DB: " + dbName + " (ScheduleStatus)");
		dbRequest = new DBRequest();

		// $match
		// scheduleStatusはStringの場合とIntの場合があるため、両方チェックする
		BasicDBList scheduleStatusCondList = new BasicDBList();
		scheduleStatusCondList.add(1);
		scheduleStatusCondList.add(3);
		scheduleStatusCondList.add(4);
		scheduleStatusCondList.add("1");
		scheduleStatusCondList.add("3");
		scheduleStatusCondList.add("4");
		BasicDBObject matchFieldObj = new BasicDBObject("cloningFileInfo.cloningSettingInfoList.cloningSettingInfo.scheduleStatus",
				new BasicDBObject(GlobalStrings.OPERATOR_IN, scheduleStatusCondList));
		BasicDBObject matchObj = new BasicDBObject(GlobalStrings.OPERATOR_MATCH, matchFieldObj);
		// $unwind
		BasicDBObject unwindObj = new BasicDBObject(GlobalStrings.OPERATOR_UNWIND, "$cloningFileInfo.cloningSettingInfoList");
		// $project
		BasicDBObject projectObj = new BasicDBObject(GlobalStrings.OPERATOR_PROJECT,
			new BasicDBObject("groupId", "$cloningFileInfo.groupId")
				.append("fileId", "$cloningFileInfo.fileId")
				.append("fileName", "$cloningFileInfo.fileName")
				.append("executionId", "$cloningFileInfo.cloningSettingInfoList.cloningSettingInfo.executionId")
				.append("scheduleId", "$cloningFileInfo.cloningSettingInfoList.cloningSettingInfo.scheduleId")
				.append("scheduleStatus", "$cloningFileInfo.cloningSettingInfoList.cloningSettingInfo.scheduleStatus")
				.append("_id", 0));
		// pipeline
		List<BasicDBObject> pipelineObj = new ArrayList<BasicDBObject>();
		pipelineObj.add(matchObj);
		pipelineObj.add(unwindObj);
		pipelineObj.add(matchObj);
		pipelineObj.add(projectObj);

		List<BasicDBObject> listResult = null;
		List<String> listScheduleId = new ArrayList<String>();
		final int newStatus = 9;
		long numOfTarget = 0;
		try {
			listResult = dbRequest.readDatawithAggregate(dbName, pipelineObj);
			if (listResult.size() > 0) {
				for (BasicDBObject result: listResult) {
					if (result == null) {
						continue;
					}
					String groupId = result.getString("groupId");
					String fileId = result.getString("fileId");
					String fileName = result.getString("fileName");
					String executionId = result.getString("executionId");
					String scheduleId = result.getString("scheduleId");
					int scheduleStatus = -1;
					try {
						String scheduleStatusStr = result.getString("scheduleStatus");
						if (scheduleStatusStr != null) {
							try {
								scheduleStatus = Integer.parseInt(scheduleStatusStr);
							} catch (NumberFormatException e) {
								// ここでは何もしない
							}
						}
					} catch (IllegalArgumentException e) {
						scheduleStatus = result.getInt("scheduleStatus", -1);
					}
					if (scheduleStatus == -1) {
						continue;
					}
					if (scheduleId == null || scheduleId.trim().length() == 0) {
						continue;
					}
					if (scheduleStatus == 3) { // リトライ待機中
						listScheduleId.add(scheduleId);
					}
					writeDetailInfoLog("TCO: " + dbName + " (ScheduleStatus): Target Schedule: groupId=" + groupId
							+ " fileId=" + fileId + " fileName=" + fileName + " executionId="
							+ executionId + " scheduleId=" + scheduleId + " scheduleStatus="
							+ scheduleStatus + "->" + newStatus);
					numOfTarget ++;
				}
			}
		} catch (MongoException e) {
			writeSimpleErrorLog("TCO: DB Read Error: " + dbName + " (ScheduleStatus)");
			writeDetailErrorLog("TCO: DB Read Error: " + dbName + " (ScheduleStatus): " + e, e.getStackTrace());
			return false;
		} catch (Exception e) {
			writeSimpleErrorLog("TCO: DB Read Error: " + dbName + " (ScheduleStatus)");
			writeDetailErrorLog("TCO: DB Read Error: " + dbName + " (ScheduleStatus): " + e, e.getStackTrace());
			return false;
		}

		if (numOfTarget > 0) {
			dbRequest = new DBRequest();
			BasicDBObject updateFieldsObj = new BasicDBObject();
			updateFieldsObj.append("cloningFileInfo.cloningSettingInfoList.$.cloningSettingInfo.scheduleStatus", newStatus);
			BasicDBObject setQueryObj = new BasicDBObject();
			setQueryObj.append(GlobalStrings.OPERATOR_SET, updateFieldsObj);
			Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
			collectionsMap.put(dbName, setQueryObj);
			try {
				int rc = 0;
				int n = 0;
				// updateFieldObjの"$"は最初の1件のみ→全件更新するまで繰り返す
				// クエリーの間違い等、なんらかの理由によりupdateDBが-1を返さない状況に陥っても無限ループとならないように更新回数をチェックする
				while (rc == 0 && n < numOfTarget) {
					rc = dbRequest.updateDB(matchFieldObj, collectionsMap, false, true);
					if (rc == 0) {
						n ++;
					}
				}
			} catch (MongoException e) {
				writeSimpleErrorLog("TCO: DB Update Error: " + dbName + " (ScheduleStatus)");
				writeDetailErrorLog("TCO: DB Update Error: " + dbName + " (ScheduleStatus): " + e, e.getStackTrace());
				return false;
			} catch (Exception e) {
				writeSimpleErrorLog("TCO: DB Update Error: " + dbName + " (ScheduleStatus)");
				writeDetailErrorLog("TCO: DB Update Error: " + dbName + " (ScheduleStatus): " + e, e.getStackTrace());
				return false;
			}
		}

		if (listScheduleId.size() > 0) {
			// リトライ待機中のスケジュールを、停止・削除する
			removeSchedule(listScheduleId); // DBのエラーではないため、エラーが発生してもスルーする
		}

		return true;
	}

	/**
	 * 指定されたスケジュールID(複数)を削除する
	 *
	 * @param listScheduleId
	 * @return
	 */
	private boolean removeSchedule(List<String> listScheduleId) {
		boolean bRet = true;
		if (listScheduleId != null && listScheduleId.size() > 0) {
			ScheduleServiceUtil ssu = new ScheduleServiceUtil();
			for (String scheduleId : listScheduleId) {
				if (ssu.deleteSchedule(scheduleId) == false) {
					writeDetailWarningLog("TCO: Schedule Delete Failure: " + scheduleId);
					bRet = false;
				}
			}
		}
		return bRet;
	}

	private void writeSimpleInfoLog(Object message) {
		if (logSimple != null) {
			logSimple.info(message);
		}
	}

	private void writeSimpleErrorLog(Object message) {
		if (logSimple != null) {
			logSimple.error(message);
		}
	}

	private void writeDetailInfoLog(Object message) {
		if (logDetail != null) {
			logDetail.info(message);
		}
	}

	private void writeDetailErrorLog(Object message, StackTraceElement[] steList) {
		if (logDetail != null) {
			logDetail.error(message);
			if (steList != null) {
				for (StackTraceElement ste: steList) {
					logDetail.error(" " + ste);
				}
			}
		}
	}

	private void writeDetailWarningLog(Object message) {
		if (logDetail != null) {
			logDetail.warn(message);
		}
	}
}
