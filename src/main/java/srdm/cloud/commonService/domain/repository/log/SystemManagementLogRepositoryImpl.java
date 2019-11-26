package srdm.cloud.commonService.domain.repository.log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.MongoQueryException;
import com.mongodb.util.JSON;
import com.srdm.mongodb.constants.GlobalStrings;
import com.srdm.mongodb.service.IDBRequest;
import com.srdm.mongodb.serviceImpl.DBRequest;

import srdm.cloud.commonService.app.bean.OrderBy;
import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.LogForView;
import srdm.cloud.commonService.domain.model.SystemManagementLog;
import srdm.cloud.commonService.util.OxmProcessor;
import srdm.common.constant.SrdmConstants;
import srdm.common.constant.SrdmLogConstants;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

@Repository
public class SystemManagementLogRepositoryImpl implements SystemManagementLogRepository {

	private static final Logger logger = LoggerFactory.getLogger(SystemManagementLogRepositoryImpl.class);

	@Autowired
	OxmProcessor oxmProcessor;

	// アカウントロック時間（単位：分）
	@Value("${srdm.auth.lockedTime}")
	private long lockedTime;

	/**
	 * システム管理ログ取得（詳細項目用）
	 *
	 * @throws SrdmDataAccessException
	 * @throws SrdmDataNotFoundException
	 */
	@Override
	public SystemManagementLog finedOne(String logId) throws SrdmDataAccessException, SrdmDataNotFoundException {

		IDBRequest dbRequest = new DBRequest();
		BasicDBObject projectFields = new BasicDBObject();
		projectFields.append("sysMgtLogList", 1).append("_id", 0);
		BasicDBObject logIdObj = new BasicDBObject("sysMgtLogList.sysMgtLog.logId", logId);
		BasicDBObject match = new BasicDBObject(GlobalStrings.OPERATOR_MATCH, logIdObj);
		BasicDBObject unwind = new BasicDBObject(GlobalStrings.OPERATOR_UNWIND,
				"$" + GlobalStrings.COMMON_SYSMGTLOG_LIST);

		// group operation
		BasicDBObject groupFields = new BasicDBObject("_id", null);
		groupFields.put(GlobalStrings.COMMON_SYSMGTLOG_LIST,
				new BasicDBObject(GlobalStrings.OPERATOR_PUSH, "$" + GlobalStrings.COMMON_SYSMGTLOG_LIST));
		BasicDBObject group = new BasicDBObject("$group", groupFields);
		BasicDBObject projection = new BasicDBObject(GlobalStrings.OPERATOR_PROJECT, projectFields);
		List<BasicDBObject> opeLogPipeline = new ArrayList<BasicDBObject>();
		opeLogPipeline.add(unwind);
		opeLogPipeline.add(match);
		opeLogPipeline.add(group);
		opeLogPipeline.add(projection);

		SystemManagementLog log = new SystemManagementLog();
		try {

			// DBからデータ取得
			List<String> listResult = dbRequest.readLogData(GlobalStrings.IPAU_COMMON_SYSMGTLOG, opeLogPipeline);

			ObjectMapper mapper = new ObjectMapper();
			if (listResult.size() != 0) {
				String resultData = "{\"sysMgtLogList\":" + listResult.toString() + "}";
				JSONObject jObject = new JSONObject(resultData);
				JSONArray jArray = jObject.getJSONArray("sysMgtLogList");
				String sysLog = new JSONObject(jArray.get(0).toString()).getJSONObject("sysMgtLog").toString();
				log = mapper.readValue(sysLog, SystemManagementLog.class);
			} else {
				// 指定log無し
				logger.warn("[finedOne] log not found. logId[{}]", logId);
				throw new SrdmDataNotFoundException("log Not found!!.");
			}
		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[finedOne] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		}  catch (MongoException e) {
			logger.error("[finedOne] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (SrdmDataNotFoundException e) {
			throw new SrdmDataNotFoundException("log Not found!!.");
		} catch (Exception e) {
			logger.error("[finedOne] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		return log;
	}

	/**
	 * システム管理ログ取得（リスト表示用）
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public List<LogForView> findAllWithPagable(GetListReq getListReq) throws SrdmDataAccessException {

		List<LogForView> list = new ArrayList<LogForView>();

		// for (String domainId : domainIdList) {

		IDBRequest dbRequest = new DBRequest();
		
		// unwind
		BasicDBObject unwind = new BasicDBObject(GlobalStrings.OPERATOR_UNWIND,
				"$" + GlobalStrings.COMMON_SYSMGTLOG_LIST);

		long startIndex = 1;
		if ((getListReq.getStartIndex() > 0)) {
			startIndex = getListReq.getStartIndex();
		}

		startIndex = startIndex - 1;

		// endIndex指定
		long endIndex = 10;
		if ((getListReq.getCount() > 0)) {
			endIndex = getListReq.getCount();
		}

		BasicDBObject skipFields = new BasicDBObject(GlobalStrings.OPERATOR_SKIP, startIndex);
		BasicDBObject limitFields = new BasicDBObject(GlobalStrings.OPERATOR_LIMIT, (int) endIndex);

		BasicDBObject projectionFields = new BasicDBObject();
		projectionFields.append("sysMgtLogList.sysMgtLog", 1).append("_id", 0);

		// Group operation
		BasicDBObject groupFields = new BasicDBObject("_id", null);
		groupFields.put(GlobalStrings.COMMON_SYSMGTLOG_LIST,
				new BasicDBObject(GlobalStrings.OPERATOR_PUSH, "$" + GlobalStrings.COMMON_SYSMGTLOG_LIST));
		BasicDBObject group = new BasicDBObject("$group", groupFields);

		// sort operation
		/// Order by
		BasicDBObject sortFields = new BasicDBObject();
		if (getListReq.getOrderBy().isEmpty() == false) {
			for (OrderBy orderBy : getListReq.getOrderBy()) {
				if (orderBy.getOrder().equalsIgnoreCase("ascending")) {
					sortFields.put("sysMgtLogList.sysMgtLog." + orderBy.getKey(), 1);
				} else {
					sortFields.put("sysMgtLogList.sysMgtLog." + orderBy.getKey(), -1);
				}

			}
		}
		BasicDBObject sort = new BasicDBObject(GlobalStrings.OPERATOR_SORT, sortFields);
		BasicDBObject projection = new BasicDBObject(GlobalStrings.OPERATOR_PROJECT, projectionFields);

		List<BasicDBObject> opeLogPipeline = new ArrayList<BasicDBObject>();
		// opeLogPipeline.add(searchFields);
		opeLogPipeline.add(unwind);
		opeLogPipeline.add(sort);
		// opeLogPipeline.add(match);
		opeLogPipeline.add(skipFields);
		opeLogPipeline.add(limitFields);
		opeLogPipeline.add(group);
		opeLogPipeline.add(projection);
		try {
			List<String> listResult = dbRequest.readLogData(GlobalStrings.IPAU_COMMON_SYSMGTLOG, opeLogPipeline);
			if (listResult.size() != 0) {
				String resultData = "{\"sysMgtLogList\":" + listResult.toString() + "}";
				JSONObject jObject = new JSONObject(resultData);
				JSONArray opeLogListArray = jObject.getJSONArray("sysMgtLogList");
				ObjectMapper mapper = new ObjectMapper();
				for (int i = 0; i < opeLogListArray.length(); i++) {
					String opeLog = new JSONObject(opeLogListArray.get(i).toString()).getJSONObject("sysMgtLog")
							.toString();
					LogForView logView = new LogForView();
					logView = mapper.readValue(opeLog, LogForView.class);
					list.add(logView);

				}
			}

		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[findAllWithPagable] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[findAllWithPagable] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[findAllWithPagable] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		return list;
	}

	/**
	 * システム管理ログ追加
	 */
	@Override
	public void add(SystemManagementLog log) throws SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// logId
		String id = UUID.randomUUID().toString();
		log.setLogId(id);

		// kind
		if (SrdmLogConstants.ListSysMgtLogKind.contains(log.getKind()) == false) {
			log.setKind(SrdmLogConstants.KIND_UNKNOWN);
		}

		// timestamp
		if (log.getTimestamp() == 0) {
			log.setTimestamp(System.currentTimeMillis());
		}

		// accountId
		if (log.getAccountId() == null) {
			log.setAccountId(SrdmLogConstants.ACCOUNTID_NONE);
		}

		// accountName
		if (log.getAccountName() == null) {
			log.setAccountName(SrdmLogConstants.ACCOUNT_NAME_NONE);
		}

		// domainId
		if (log.getDomainId() == null) {
			log.setDomainId(SrdmLogConstants.DOMAINID_NONE);
		}

		// domainName
		if (log.getDomainName() == null) {
			log.setDomainName(SrdmLogConstants.DOMAIN_NAME_NONE);
		}

		// operation
		if (SrdmLogConstants.ListSysMgtLogOperation.contains(log.getOperation()) == false) {
			log.setOperation(SrdmLogConstants.OPERATION_UNKNOWN);
		}

		// code
		if (log.getCode() == null) {
			log.setCode(SrdmLogConstants.CODE_UNKNOWN);
		}

		try {

			ObjectMapper mapper = new ObjectMapper();
			BasicDBObject insertSysMgtLogObj = (BasicDBObject) JSON.parse(mapper.writeValueAsString(log));

			dbRequest.insertLogData(GlobalStrings.IPAU_COMMON_SYSMGTLOG, insertSysMgtLogObj);

		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[add] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[add] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[add] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

	}


	/**
	 * システム管理ログ削除（全削除）
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public void deleteAll() throws SrdmDataAccessException {

		IDBRequest dbRequest = new DBRequest();
		BasicDBObject deleteCondition = new BasicDBObject("sysMgtLogList",
				new BasicDBObject(GlobalStrings.OPERATOR_EXISTS, "true"));

		try {
			dbRequest.deleteLogData(GlobalStrings.IPAU_COMMON_SYSMGTLOG, deleteCondition);

		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[deleteAll] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[deleteAll] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[deleteAll] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}
	}

	/**
	 * システム管理ログ削除（timestamp指定）
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public void deleteByTimestamp(long timestamp) throws SrdmDataAccessException {

		IDBRequest dbRequest = new DBRequest();
		BasicDBObject deleteCondition = new BasicDBObject(GlobalStrings.COMMON_SYSMGTLOG_LIST, new BasicDBObject(
				"sysMgtLog.timestamp", new BasicDBObject(GlobalStrings.OPERATOR_LESS_THAN, timestamp)));

		try {
			// queryの実行
			dbRequest.deleteLogData(GlobalStrings.IPAU_COMMON_SYSMGTLOG, deleteCondition);

		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[deleteByTimestamp] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[deleteByTimestamp] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[deleteByTimestamp] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}
	}

	/**
	 * システム管理ログ件数
	 *
	 * @throws SrdmDataAccessException
	 * @throws SrdmDataNotFoundException
	 */
	@Override
	public Long count(GetListReq getListReq) throws SrdmDataAccessException, SrdmDataNotFoundException {

		Long count = 0L;
		IDBRequest dbRequest = new DBRequest();
		try {

			// unwind
			BasicDBObject unwind = new BasicDBObject(GlobalStrings.OPERATOR_UNWIND,
					"$" + GlobalStrings.COMMON_SYSMGTLOG_LIST);

			// Group operation
			BasicDBObject groupFields = new BasicDBObject("_id", null);
			groupFields.put(GlobalStrings.COMMON_SYSMGTLOG_LIST,
					new BasicDBObject(GlobalStrings.OPERATOR_PUSH, "$" + GlobalStrings.COMMON_SYSMGTLOG_LIST));
			BasicDBObject group = new BasicDBObject("$group", groupFields);

			List<BasicDBObject> sysMgtLogCountPipeline = new ArrayList<BasicDBObject>();
			sysMgtLogCountPipeline.add(unwind);
			sysMgtLogCountPipeline.add(group);
			// project operation
			BasicDBObject fields = new BasicDBObject("totalCount",
					new BasicDBObject(GlobalStrings.OPERATOR_SIZE, "$sysMgtLogList.sysMgtLog")).append("_id", 0);
			BasicDBObject project = new BasicDBObject(GlobalStrings.OPERATOR_PROJECT, fields);
			sysMgtLogCountPipeline.add(project);
			count = Long.valueOf(
					dbRequest.getOperationLogCount(GlobalStrings.IPAU_COMMON_SYSMGTLOG, sysMgtLogCountPipeline));
			if (count == 0) {

				// データが無い場合
				logger.warn("[count] SystemManagement Log not found.");
				count = 0L;
			}
		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[count] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[count] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[count] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		return count;
	}

	/**
	 * システム管理ログ削除（domainId指定） （未使用）
	 */
	@Override
	public void deleteByDomainIds(List<String> domainIds) throws SrdmDataAccessException {

		IDBRequest dbRequest = new DBRequest();
		BasicDBObject deleteCondition = new BasicDBObject(GlobalStrings.COMMON_SYSMGTLOG_LIST,
				new BasicDBObject("sysMgtLog.domainId", new BasicDBObject(GlobalStrings.OPERATOR_IN, domainIds)));

		try {
			dbRequest.deleteLogData(GlobalStrings.IPAU_COMMON_SYSMGTLOG, deleteCondition);

		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[deleteByDomainIds] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[deleteByDomainIds] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[deleteByDomainIds] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}
	}

	/**
	 * システム管理ログのexport（全てのログを出力する）
	 * （nowTimeは、実行時間。出力処理中にaccountStatusが変化しないように、呼出し元で現在時刻を指定）
	 */
	@Override
	public List<String> export(long nowTime, long startIndex, long endIndex) throws SrdmDataAccessException {

		List<String> sysMgtlogList = new ArrayList<String>();
		List<String> logList = new ArrayList<String>();
		try {

			IDBRequest dbRequest = new DBRequest();

			// unwind
			BasicDBObject unwind = new BasicDBObject(GlobalStrings.OPERATOR_UNWIND,
					"$" + GlobalStrings.COMMON_SYSMGTLOG_LIST);

			BasicDBObject projectionFields = new BasicDBObject();
			projectionFields.append("sysMgtLogList.sysMgtLog", 1).append("_id", 0);

			// Group operation
			BasicDBObject groupFields = new BasicDBObject("_id", null);
			groupFields.put(GlobalStrings.COMMON_SYSMGTLOG_LIST,
					new BasicDBObject(GlobalStrings.OPERATOR_PUSH, "$" + GlobalStrings.COMMON_SYSMGTLOG_LIST));
			BasicDBObject group = new BasicDBObject("$group", groupFields);

			BasicDBObject projection = new BasicDBObject(GlobalStrings.OPERATOR_PROJECT, projectionFields);

			BasicDBObject skipFields = new BasicDBObject(GlobalStrings.OPERATOR_SKIP, 0);
			List<BasicDBObject> opeLogPipeline = new ArrayList<BasicDBObject>();
			opeLogPipeline.add(unwind);
			opeLogPipeline.add(skipFields);
			opeLogPipeline.add(group);
			opeLogPipeline.add(projection);

			List<String> listResult = dbRequest.readLogData(GlobalStrings.IPAU_COMMON_SYSMGTLOG, opeLogPipeline);

			if (listResult.size() != 0) {
				String resultData = "{\"sysMgtLogList\":" + listResult.toString() + "}";
				JSONObject jObject = new JSONObject(resultData);
				JSONArray opeLogListArray = jObject.getJSONArray("sysMgtLogList");
				ObjectMapper mapper = new ObjectMapper();
				Map<String, String> accountInfoMap = new HashMap<String, String>();

				for (int i = 0; i < opeLogListArray.length(); i++) {
					String sysLog = new JSONObject(opeLogListArray.get(i).toString()).getJSONObject("sysMgtLog")
							.toString();
					SystemManagementLog log = new SystemManagementLog();
					log = mapper.readValue(sysLog, SystemManagementLog.class);

					String accountId = new JSONObject(opeLogListArray.get(i).toString()).getJSONObject("sysMgtLog")
							.getString("accountId");
					if (accountInfoMap.containsKey(accountId)) {
						log.setAccountStatus(accountInfoMap.get(accountId));
					} else if (accountId.equalsIgnoreCase("-")) {
						log.setAccountStatus(SrdmLogConstants.EMPTY_VALUE);
					} else {
						// Form DB request query
						BasicDBObject dbSourceObj = new BasicDBObject();
						dbSourceObj.append("account", 1).append("_id", 0).append("account.accountStatus", 1);

						// Conditions
						BasicDBObject accountIdObj = new BasicDBObject("account.accountId", String.valueOf(accountId));

						String accountStatus = null;

						// DBからデータ取得
						List<String> accountstatusResult = dbRequest.readFromDB(GlobalStrings.COMMON_ACCOUNT, 0, 0,
								accountIdObj, dbSourceObj, new BasicDBObject());

						// O/XMapping(xml to object)
						if (accountstatusResult.size() != 0) {
							String strResult = accountstatusResult.get(0);
							JSONObject jsonObj = new JSONObject(strResult);
							accountStatus = jsonObj.getJSONObject("account").getString("accountStatus");
							if (accountStatus.isEmpty()) {
								log.setAccountStatus(SrdmLogConstants.EMPTY_VALUE);
								accountInfoMap.put(accountId,
										jsonObj.getJSONObject("account").getString("accountStatus"));
							} else if (accountStatus.equalsIgnoreCase(SrdmConstants.ACCOUNT_STATUS_LOCKED)) {
								long latestErrorTimestamp = Long
										.parseLong(jsonObj.getJSONObject("account").getString("latestErrorTimestamp"));
								long lokedTime = lockedTime * 60 * 1000;
								if ((latestErrorTimestamp + lokedTime) > System.currentTimeMillis()) {
									log.setAccountStatus(jsonObj.getJSONObject("account").getString("accountStatus"));
									accountInfoMap.put(accountId,
											jsonObj.getJSONObject("account").getString("accountStatus"));

								} else {
									log.setAccountStatus(SrdmConstants.ACCOUNT_STATUS_ACTIVE);
									accountInfoMap.put(accountId,
											jsonObj.getJSONObject("account").getString("accountStatus"));
								}
							} else {
								log.setAccountStatus(jsonObj.getJSONObject("account").getString("accountStatus"));
								accountInfoMap.put(accountId,
										jsonObj.getJSONObject("account").getString("accountStatus"));
							}
						}
					}

					BasicDBObject logData = (BasicDBObject) JSON.parse(mapper.writeValueAsString(log));
					BasicDBObject sysLogObj = new BasicDBObject("sysMgtLog", logData);

					sysMgtlogList.add(sysLogObj.toString());
				}

				BasicDBObject opeLogObj1 = new BasicDBObject("sysMgtLogList", sysMgtlogList.toString());
				logList.add(opeLogObj1.toString());

			}
	
		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[export] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[export] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[export] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		return logList;		
	}

}
