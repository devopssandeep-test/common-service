package srdm.cloud.commonService.util.optimization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.srdm.mongodb.constants.GlobalStrings;
import com.srdm.mongodb.serviceImpl.DBRequest;

import lombok.ToString;
import srdm.cloud.shared.system.MaintenanceInfoFSS;

/**
 * DB最適化処理の後処理
 *
 */
public class FssOptimization {

	private Logger logSimple = null;
	private Logger logDetail = null;
	DBRequest dbRequest = null;

	private static Object mLock = new Object();

	private List<String> mListErrorDB = new ArrayList<String>();

	// Remote Diag
	private static final String DiagExecMfpResultUnknown = "diagExecMfpResultUnknown"; // SIM実行の最中にDB最適化が実行されたため、MFP側での実行結果が不明
	// Firmware Update
	private static final String CancelResultUnknown = "cancelResultUnknown"; // ファームウェアアップデートのキャンセル中にDB最適化が実行されたため、キャンセル結果不明
	private static final String TimeChangeResultUnknown = "timeChangeResultUnknown"; // ファームウェアアップデート予約日時変更の最中にDB最適化が実行されたため、変更できたか不明
	private static final String AbortReservation = "abortReservation"; // ファイル転送中にDB最適化が実行されたため、ファイル転送を中断=予約中止
	// Firmware Update History
	private static final String Aborted = "aborted"; // ファイル転送中にDB最適化が実行されたため、ファイル転送を中断

	public FssOptimization(final Logger simpleLogger, final Logger detailLogger) {
		logSimple = simpleLogger;
		logDetail = detailLogger;
	}

	@ToString
	private static class FssDeviceInfo {
		public String machineName;
		public String serialNumber;
		public String magicCode;
	}

	/**
	 * machineName, serialNumber, magicCodeを共有メモリに保持する
	 *
	 */
	public void loadDeviceInfo() {

		dbRequest = new DBRequest();
		BasicDBObject deviceInfoCondition = new BasicDBObject("device.deviceInfo",
				new BasicDBObject(GlobalStrings.OPERATOR_EXISTS, true));
		BasicDBObject projectFields = new BasicDBObject();
		projectFields.append("device.deviceInfo.machineName", 1).append("device.deviceInfo.serialNumber", 1)
				.append("device.deviceInfo.magicCode", 1).append("_id", 0);

		MaintenanceInfoFSS mifss = MaintenanceInfoFSS.getInstance();
		mifss.clearDeviceInfo();
		try {
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.FSS_DEVICELIST_COLLECTION, 0, 0,
					deviceInfoCondition, projectFields, new BasicDBObject());
			if (listResult.size() != 0) {
				ObjectMapper mapper = new ObjectMapper();
				JSONArray jsonarray = new JSONArray(listResult.toString());
				for (int i = 0; i < jsonarray.length(); i++) {
					if (jsonarray.get(i) == null) {
						continue;
					}
					String deviceData = jsonarray.getJSONObject(i).getJSONObject("device").getJSONObject("deviceInfo")
							.toString();
					FssDeviceInfo deviceInfo = mapper.readValue(deviceData, FssDeviceInfo.class);

					if (deviceInfo == null) {
						continue;
					}
					mifss.addDeviceInfo(deviceInfo.machineName, deviceInfo.serialNumber, deviceInfo.magicCode);
				}
			}

			// printDeviceInfo("Load");
		} catch (MongoException e) {
			writeSimpleErrorLog("FSS: Initialization Error.");
			writeDetailErrorLog("FSS: Initialization Error: " + e, e.getStackTrace());
		} catch (Exception e) {
			writeSimpleErrorLog("FSS: Initialization Error.");
			writeDetailErrorLog("FSS: Initialization Error: " + e, e.getStackTrace());
		}
	}

	/**
	 * 共有メモリに保持しているデバイス情報を削除する
	 */
	public void clearDeviceInfo() {
		MaintenanceInfoFSS mifss = MaintenanceInfoFSS.getInstance();
		mifss.clearDeviceInfo();
		// printDeviceInfo("Clear");
	}

	// private void printDeviceInfo(final String title) { // for debugging
	// final String prefix = "[TEST] FSS: deviceInfo: " + title + ": ";
	// List<String[]> listDeviceInfo =
	// MaintenanceInfoFSS.getInstance().getDeviceInfoList();
	// writeDetailInfoLog(prefix + "size=" + listDeviceInfo.size());
	// for (String[] array: listDeviceInfo) {
	// if (array == null) {
	// writeDetailErrorLog(prefix + "null");
	// continue;
	// }
	// if (array.length != 3) {
	// if (array.length == 1) {
	// writeDetailErrorLog(prefix + "length=" + array.length + " " + array[0]);
	// } else if (array.length == 2) {
	// writeDetailErrorLog(prefix + "length=" + array.length + " " + array[0] +
	// " " + array[1]);
	// } else {
	// writeDetailErrorLog(prefix + "length=" + array.length);
	// }
	// continue;
	// }
	// writeDetailInfoLog(prefix + "machineName=" + array[0] + " serialNumber="
	// + array[1] + " magicCode=" + array[2]);
	// }
	// }

	/**
	 * FSSのDBのステータス/結果を調整する<br/>
	 * DBのアクセスブロックを開始する前にloadDeviceInfo()を呼び出しておくこと
	 *
	 * @return
	 */
	public boolean update() {
		synchronized (mLock) {
			boolean bRet = true;

			DBCheckUtil dbcUtil = new DBCheckUtil();
			if (updateFssDeviceList(dbcUtil) == false) {
				mListErrorDB.add(GlobalStrings.FSS_DEVICELIST_COLLECTION);
				bRet = false;
			}
			if (updateFssRemoteDiagHistory(dbcUtil) == false) {
				mListErrorDB.add(GlobalStrings.FSS_REMOTEDIAG_HISTORY_COLLECTION);
				bRet = false;
			}
			if (updateFssFWUpdateHistory(dbcUtil) == false) {
				mListErrorDB.add(GlobalStrings.FSS_FWUPDATE_HISTORY_COLLECTION);
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
		synchronized (mLock) {
			return mListErrorDB;
		}
	}

	/**
	 * fssDeviceListを更新する<br/>
	 *
	 * @param dbcUtil
	 * @return
	 */
	private boolean updateFssDeviceList(final DBCheckUtil dbcUtil) {
		final String dbName = GlobalStrings.FSS_DEVICELIST_COLLECTION;
		boolean bRet = true;
		if (updateFssDeviceListRemoteDiagInfo(dbName) == false) {
			bRet = false;
		}
		if (updateFssDeviceListFirmwareUpdateInfo(dbName) == false) {
			bRet = false;
		}
		return bRet;
	}

	/**
	 * fssDeviceListのremoteDiagInfoのlastRemoteDiagResult/
	 * runningRemoteDiagStatusを更新する<br/>
	 *
	 * @param dbName
	 * @return
	 */
	private boolean updateFssDeviceListRemoteDiagInfo(final String dbName) {
		writeSimpleInfoLog("FSS: Target DB: " + dbName + " (RemoteDiag)");
		writeDetailInfoLog("FSS: Target DB: " + dbName + " (RemoteDiag)");

		dbRequest = new DBRequest();
		BasicDBObject runningRemoteStatusCondition = new BasicDBObject(
				"device.status.remoteDiagInfo.runningRemoteDiagStatus", "ready");
		BasicDBObject lastRemoteResultCondition = new BasicDBObject("device.status.remoteDiagInfo.lastRemoteDiagResult",
				"ready");

		BasicDBList searchBothConditionList = new BasicDBList();
		searchBothConditionList.add(runningRemoteStatusCondition);
		searchBothConditionList.add(lastRemoteResultCondition);

		BasicDBObject searchBothConditionObj = new BasicDBObject();
		searchBothConditionObj.append(GlobalStrings.OPERATOR_OR, searchBothConditionList);

		BasicDBObject projectFields = new BasicDBObject();
		projectFields.append("device.deviceInfo.machineName", 1).append("device.deviceInfo.serialNumber", 1)
				.append("device.deviceInfo.groupId", 1)
				.append("device.status.remoteDiagInfo.runningRemoteDiagStatus", 1)
				.append("device.status.remoteDiagInfo.lastRemoteDiagResult", 1).append("_id", 0);

		List<String> listResult;
		try {
			listResult = dbRequest.readFromDB(dbName, 0, 0, searchBothConditionObj, projectFields, new BasicDBObject());
			if (listResult.size() > 0) {
//				ObjectMapper mapper = new ObjectMapper();
				JSONArray jsonarray = new JSONArray(listResult.toString());
				for (int i = 0; i < jsonarray.length(); i++) {
					JSONObject jsonDevice = jsonarray.getJSONObject(i).getJSONObject("device");
					JSONObject jsonDeviceInfo = jsonDevice.getJSONObject("deviceInfo");
					String machineName = jsonDeviceInfo.getString("machineName");
					String serialNumber = jsonDeviceInfo.getString("serialNumber");
					String groupId = jsonDeviceInfo.getString("groupId");
					JSONObject jsonRemoteDiagInfo = jsonDevice.getJSONObject("status").getJSONObject("remoteDiagInfo");
					String runningRemoteDiagStatus = jsonRemoteDiagInfo.getString("runningRemoteDiagStatus");
					String lastRemoteDiagResult = jsonRemoteDiagInfo.getString("lastRemoteDiagResult");
					writeDetailInfoLog("FSS: " + dbName + ": Target Device: machineName=["
							+ machineName + "] serialNumber=[" + serialNumber + "] groupId=[" + groupId
							+ "] runningRemoteDiagStatus=[" + runningRemoteDiagStatus + "]->[" + DiagExecMfpResultUnknown
							+ "] lastRemoteDiagResult=[" + lastRemoteDiagResult + "]->[" + DiagExecMfpResultUnknown + "]");

					dbRequest = new DBRequest();

					Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
					BasicDBObject updateFieldsObj = new BasicDBObject();
					if (runningRemoteDiagStatus.equalsIgnoreCase("ready")) {
						updateFieldsObj.append("device.status.remoteDiagInfo.runningRemoteDiagStatus",
								DiagExecMfpResultUnknown);
					}
					if (lastRemoteDiagResult.equalsIgnoreCase("ready")) {
						updateFieldsObj.append("device.status.remoteDiagInfo.lastRemoteDiagResult",
								DiagExecMfpResultUnknown);
					}

					BasicDBObject setQueryObj = new BasicDBObject();
					setQueryObj.append(GlobalStrings.OPERATOR_SET, updateFieldsObj);
					collectionsMap.put(dbName, setQueryObj);
					dbRequest.updateDB(searchBothConditionObj, collectionsMap, false, true);
				}
			}
		} catch (MongoException e) {
			writeSimpleErrorLog("FSS: DB Error: " + dbName + " (RemoteDiag)");
			writeDetailErrorLog("FSS: DB Error: " + dbName + " (RemoteDiag): " + e, e.getStackTrace());
			return false;
		} catch (Exception e) {
			writeSimpleErrorLog("FSS: DB Error: " + dbName + " (RemoteDiag)");
			writeDetailErrorLog("FSS: DB Error: " + dbName + " (RemoteDiag): " + e, e.getStackTrace());
			return false;
		}
		return true;
	}

	/**
	 * fssDeviceListのfirmwareUpdateInfoのcurrentInfoを更新する<br/>
	 * 最新のupdateStatusを元に、新しいデータを追加する。<br/>
	 * ・updateStatus=startSending,sending,waitForSend →
	 * updateStatus=abortReservationのデータを追加<br/>
	 * ・updateStatus=startCancelling → updateStatus=cancelResultUnknownのデータを追加
	 * <br/>
	 * ・updateStatus=startTimeChanging → timeChangeResultUnknownのデータを追加<br/>
	 *
	 * @return
	 */
	private boolean updateFssDeviceListFirmwareUpdateInfo(final String dbName) {
		final String[] updateStatus    = { "startSending",   "sending",        "waitForSend",    "startCancelling",   "startTimeChanging" };
		final String[] newUpdateStatus = { AbortReservation, AbortReservation, AbortReservation, CancelResultUnknown, TimeChangeResultUnknown };
		writeSimpleInfoLog("FSS: Target DB: " + dbName + " (FirmwareUpdate)");
		writeDetailInfoLog("FSS: Target DB: " + dbName + " (FirmwareUpdate)");

		dbRequest = new DBRequest();
		boolean bRet = true;
		for (int i = 0; i < updateStatus.length; i++) {
			// "$match"
			BasicDBObject matchCondition1 = new BasicDBObject(GlobalStrings.OPERATOR_MATCH,
					new BasicDBObject("device.status.firmwareUpdateInfo.currentInfo.updateStatusChangeHistory.updateStatusChange.updateStatus", updateStatus[i]));
			// "$project"
			BasicDBList sliceList = new BasicDBList();
			sliceList.add("$device.status.firmwareUpdateInfo.currentInfo.updateStatusChangeHistory.updateStatusChange");
			sliceList.add(-1);
			BasicDBObject projectFields = new BasicDBObject(GlobalStrings.OPERATOR_PROJECT,
				new BasicDBObject("machineName", "$device.deviceInfo.machineName")
					.append("serialNumber", "$device.deviceInfo.serialNumber")
					.append("groupId", "$device.deviceInfo.groupId")
					.append("updateStatusChange", new BasicDBObject(GlobalStrings.OPERATOR_SLICE, sliceList)) // array
					.append("reservationDateTime", "$device.deviceInfo.groupId")
					.append("_id", 1));
			// "$match"
			BasicDBObject matchCondition2 = new BasicDBObject(GlobalStrings.OPERATOR_MATCH,
					new BasicDBObject("updateStatusChange.updateStatus", updateStatus[i]));
			// pipeline
			List<BasicDBObject> pipelineObj = new ArrayList<BasicDBObject>();
			pipelineObj.add(matchCondition1);
			pipelineObj.add(projectFields);
			pipelineObj.add(matchCondition2);
			List<BasicDBObject> listResult;
			try {
				listResult = dbRequest.readDatawithAggregate(dbName, pipelineObj);
				if (listResult.size() > 0) {
					final String newStatus = newUpdateStatus[i];
					for (BasicDBObject result: listResult) {
						if (result == null) {
							continue;
						}
						String machineName = result.getString("machineName");
						String serialNumber = result.getString("serialNumber");
						String groupId = result.getString("groupId");
						ObjectId id = result.getObjectId("_id");
						@SuppressWarnings("unchecked")
						List<BasicDBObject> listUpdateStatusChange = (List<BasicDBObject>)result.get("updateStatusChange");
						String tmpUpdateStatus = "[unknown]";
						String tmpReservationDateTime = "197001010000";
						long tmpFileXferTimestamp = 0;
						int tmpTransferredFileNumber = 0;
						if (listUpdateStatusChange != null && listUpdateStatusChange.size() > 0) {
							BasicDBObject updateStatusChange = listUpdateStatusChange.get(0);
							if (updateStatusChange != null) {
								tmpUpdateStatus = updateStatusChange.getString("updateStatus");
								tmpReservationDateTime = updateStatusChange.getString("reservationDateTime");
								tmpFileXferTimestamp = updateStatusChange.getLong("fileXferTimestamp", 0);
								tmpTransferredFileNumber = updateStatusChange.getInt("transferredFileNumber", 0);
							}
						}
						writeDetailInfoLog("FSS: " + dbName + " (FirmwareUpdate): Target Device: machineName=["
								+ machineName +"] serialNumber=[" + serialNumber + "] groupId=[" + groupId
								+"] updateStatus=[" + tmpUpdateStatus + "]->[" + newStatus + "]");

						BasicDBObject updateStatusChangeCondition = new BasicDBObject();
						updateStatusChangeCondition.append("_id", id);
//						updateStatusChangeCondition
//							.append("device.deviceInfo.machineName", machineName)
//							.append("device.deviceInfo.serialNumber", serialNumber);

						Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
						BasicDBObject updateStatusChangeObj = new BasicDBObject();
						updateStatusChangeObj.append("updateStatusTimestamp", System.currentTimeMillis());
						updateStatusChangeObj.append("updateStatus", newStatus);
						updateStatusChangeObj.append("reservationDateTime", tmpReservationDateTime);
						updateStatusChangeObj.append("fileXferTimestamp", tmpFileXferTimestamp);
						updateStatusChangeObj.append("transferredFileNumber", tmpTransferredFileNumber);
						updateStatusChangeObj.append("fwupdCommandCode", 0);
						updateStatusChangeObj.append("fwupdCommandResultCode", 0);
						updateStatusChangeObj.append("fwupdCommandResultCodeDetail", 0);
						BasicDBObject updateFieldsObj = new BasicDBObject();
						updateFieldsObj.append("device.status.firmwareUpdateInfo.currentInfo.updateStatusChangeHistory",
								new BasicDBObject("updateStatusChange", updateStatusChangeObj));
						BasicDBObject setQueryObj = new BasicDBObject();
						setQueryObj.append(GlobalStrings.OPERATOR_ADDTOSET, updateFieldsObj);
						collectionsMap.put(dbName, setQueryObj);

						dbRequest.updateDB(updateStatusChangeCondition, collectionsMap, false, false);
					}
				}
			} catch (MongoException e) {
				writeSimpleErrorLog("FSS: DB Error: " + dbName + " (FirmwareUpdate)");
				writeDetailErrorLog("FSS: DB Error: " + dbName + " (FirmwareUpdate): " + e, e.getStackTrace());
				bRet = false;
			} catch (Exception e) {
				writeSimpleErrorLog("FSS: DB Error: " + dbName + " (FirmwareUpdate)");
				writeDetailErrorLog("FSS: DB Error: " + dbName + " (FirmwareUpdate): " + e, e.getStackTrace());
				bRet = false;
			}
		}
		return bRet;
	}

	/**
	 * fssRemoteDiagHistoryを更新<br/>
	 * result=ready→resultUnknown
	 *
	 * @param dbcUtil
	 * @return
	 */
	private boolean updateFssRemoteDiagHistory(final DBCheckUtil dbcUtil) {
		final String dbName = GlobalStrings.FSS_REMOTEDIAG_HISTORY_COLLECTION;
		writeSimpleInfoLog("FSS: Target DB: " + dbName);
		writeDetailInfoLog("FSS: Target DB: " + dbName);

		dbRequest = new DBRequest();
		BasicDBObject resultCondition = new BasicDBObject("device.data.dataInfo.result", "ready");
		BasicDBObject timestampCondition = new BasicDBObject("device.data.resultInfo.diagExecTimestamp",
				new BasicDBObject(GlobalStrings.OPERATOR_EXISTS, false));

		BasicDBList searchBothConditionList = new BasicDBList();
		searchBothConditionList.add(resultCondition);
		searchBothConditionList.add(timestampCondition);

		BasicDBObject searchBothConditionObj = new BasicDBObject();
		searchBothConditionObj.append(GlobalStrings.OPERATOR_AND, searchBothConditionList);

		BasicDBObject projectFields = new BasicDBObject();
		projectFields.append("device.deviceInfo.machineName", 1).append("device.deviceInfo.serialNumber", 1)
				.append("device.deviceInfo.groupId", 1).append("device.data.dataInfo.result", 1)
				.append("_id", 0);

		List<String> listResult;
		try {
			listResult = dbRequest.readFromDB(dbName, 0, 0, searchBothConditionObj, projectFields, new BasicDBObject());
			if (listResult.size() > 0) {
				final String newResult = DiagExecMfpResultUnknown;
				for (String buf : listResult) {
					JSONObject jsonObject = new JSONObject(buf);
					JSONObject jsonDevice = jsonObject.getJSONObject("device");
					if (jsonDevice == null) {
						continue;
					}
					JSONObject jsonDeviceInfo = jsonDevice.getJSONObject("deviceInfo");
					if (jsonDeviceInfo == null) {
						continue;
					}
					JSONObject jsonData = jsonDevice.getJSONObject("data");
					if (jsonData == null) {
						continue;
					}
					JSONObject jsonDataInfo = jsonData.getJSONObject("dataInfo");
					if (jsonDataInfo == null) {
						continue;
					}
					String machineName = jsonDeviceInfo.getString("machineName");
					String serialNumber = jsonDeviceInfo.getString("serialNumber");
					String groupId = jsonDeviceInfo.getString("groupId");
					String result = jsonDataInfo.getString("result");
					writeDetailInfoLog("FSS: " + dbName + " (RemoteDiag): Target Device: machineName=["
							+ machineName + "] serialNumber=[" + serialNumber + "] groupId=[" + groupId
							+ "] result=[" + result + "]->[" + newResult + "]");
				}

				Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
				BasicDBObject updateFieldsObj = new BasicDBObject();
				updateFieldsObj.append("device.data.dataInfo.result", newResult);

				BasicDBObject setQueryObj = new BasicDBObject();
				setQueryObj.append(GlobalStrings.OPERATOR_SET, updateFieldsObj);
				collectionsMap.put(dbName, setQueryObj);
				dbRequest.updateDB(searchBothConditionObj, collectionsMap, false, true);

			}
		} catch (MongoException e) {
			writeSimpleErrorLog("FSS: DB Error: " + dbName);
			writeDetailErrorLog("FSS: DB Error: " + dbName + ": " + e, e.getStackTrace());
			return false;
		} catch (Exception e) {
			writeSimpleErrorLog("FSS: DB Error: " + dbName);
			writeDetailErrorLog("FSS: DB Error: " + dbName + ": " + e, e.getStackTrace());
			return false;
		}
		return true;
	}

	/**
	 * fssFWUpdateHistoryを更新する<br/>
	 * statusがwaitForSend(*)でexecResult=readyの場合、<br/>
	 * statusはabortReservationに、execResultはabortedに変更する。<br/>
	 * cancelは結果(成功/失敗)のみ登録されるため(処理途中の状態が登録されないため)、調整は不要。<br/>
	 * (*) fssServerの動作として、sendingがDBに登録されるケースはない。
	 *     またポーリング時にstartSending状態に遷移するが履歴DBは更新していない。
	 *     このため、実際にstatusにセットされる文字列は"waitForSend"のみとなる。
	 * @param dbcUtil
	 * @return
	 */
	private boolean updateFssFWUpdateHistory(final DBCheckUtil dbcUtil) {
		final String dbName = GlobalStrings.FSS_FWUPDATE_HISTORY_COLLECTION;
		writeSimpleInfoLog("FSS: Target DB: " + dbName);
		writeDetailInfoLog("FSS: Target DB: " + dbName);

// SRDM2.6: 2.5まではstartSendingも対象としていたが、実際にはwaitForSendしかセットされないため、クエリーの条件を修正。
//		List<String> statusCondList = new ArrayList<>();
//		statusCondList.add("startSending");
//		statusCondList.add("waitForSend");

		dbRequest = new DBRequest();
//		BasicDBObject statusCondition = new BasicDBObject("device.data.dataInfo.resultInfo.status",
//				new BasicDBObject(GlobalStrings.OPERATOR_IN, statusCondList));
		BasicDBObject statusCondition = new BasicDBObject("device.data.dataInfo.resultInfo.status", "waitForSend");
		BasicDBObject execResultCondition = new BasicDBObject("device.data.dataInfo.resultInfo.execResult", "ready");

		BasicDBList searchBothConditionList = new BasicDBList();
		searchBothConditionList.add(statusCondition);
		searchBothConditionList.add(execResultCondition);

		BasicDBObject searchBothConditionObj = new BasicDBObject();
		searchBothConditionObj.append(GlobalStrings.OPERATOR_AND, searchBothConditionList);

		BasicDBObject projectFields = new BasicDBObject();
		projectFields.append("device.deviceInfo.machineName", 1).append("device.deviceInfo.serialNumber", 1)
				.append("device.deviceInfo.groupId", 1).append("device.data.dataInfo.resultInfo.status", 1)
				.append("device.data.dataInfo.resultInfo.execResult", 1).append("_id", 0);

		List<String> listResult;
		try {
			listResult = dbRequest.readFromDB(dbName, 0, 0, searchBothConditionObj, projectFields, new BasicDBObject());
			if (listResult.size() > 0) {
				final String newStatus = AbortReservation;
				final String newResult = Aborted;
				for (String buf : listResult) {
					JSONObject jsonObject = new JSONObject(buf);
					JSONObject jsonDevice = jsonObject.getJSONObject("device");
					if (jsonDevice == null) {
						continue;
					}
					JSONObject jsonDeviceInfo = jsonDevice.getJSONObject("deviceInfo");
					if (jsonDeviceInfo == null) {
						continue;
					}
					JSONObject jsonData = jsonDevice.getJSONObject("data");
					if (jsonData == null) {
						continue;
					}
					JSONObject jsonDataInfo = jsonData.getJSONObject("dataInfo");
					if (jsonDataInfo == null) {
						continue;
					}
					JSONObject jsonResultInfo = jsonDataInfo.getJSONObject("resultInfo");
					if (jsonResultInfo == null) {
						continue;
					}
					String machineName = jsonDeviceInfo.getString("machineName");
					String serialNumber = jsonDeviceInfo.getString("serialNumber");
					String groupId = jsonDeviceInfo.getString("groupId");
					String status = jsonResultInfo.getString("status");
					String execResult = jsonResultInfo.getString("execResult");
					writeDetailInfoLog("FSS: " + dbName + ": Target Device: machineName=["
							+ machineName + "] serialNumber=[" + serialNumber + "] groupId=[" + groupId
							+ "] status=[" + status + "]->[" + newStatus + "] execResult=[" + execResult
							+ "]->["+ Aborted + "]");
				}
				Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
				BasicDBObject updateFieldsObj = new BasicDBObject();
				updateFieldsObj.append("device.data.dataInfo.resultInfo.status", newStatus);
				updateFieldsObj.append("device.data.dataInfo.resultInfo.execResult", newResult);

				BasicDBObject setQueryObj = new BasicDBObject();
				setQueryObj.append(GlobalStrings.OPERATOR_SET, updateFieldsObj);
				collectionsMap.put(dbName, setQueryObj);
				dbRequest.updateDB(searchBothConditionObj, collectionsMap, false, true);
			}
		} catch (MongoException e) {
			writeSimpleErrorLog("FSS: DB Error: " + dbName);
			writeDetailErrorLog("FSS: DB Error: " + dbName + ": " + e, e.getStackTrace());
			return false;
		} catch (Exception e) {
			writeSimpleErrorLog("FSS: DB Error: " + dbName);
			writeDetailErrorLog("FSS: DB Error: " + dbName + ": " + e, e.getStackTrace());
			return false;
		}
		return true;
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
}
