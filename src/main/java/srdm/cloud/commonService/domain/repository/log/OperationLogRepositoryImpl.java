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
import com.mongodb.BasicDBList;
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
import srdm.cloud.commonService.domain.model.OperationLog;
import srdm.cloud.commonService.util.OxmProcessor;
import srdm.common.constant.SrdmConstants;
import srdm.common.constant.SrdmLogConstants;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

@Repository
public class OperationLogRepositoryImpl implements OperationLogRepository {

	private static final Logger logger = LoggerFactory.getLogger(OperationLogRepositoryImpl.class);

	@Autowired
	OxmProcessor oxmProcessor;

	// アカウントロック時間（単位：分）
	@Value("${srdm.auth.lockedTime}")
	private long lockedTime;

	/**
	 * 操作ログ取得（単一）
	 *
	 * @throws SrdmDataAccessException
	 * @throws SrdmDataNotFoundException
	 */
	@Override
	public OperationLog finedOne(List<String> domainIdList, String logId)
			throws SrdmDataAccessException, SrdmDataNotFoundException {

		IDBRequest dbRequest = new DBRequest();
		BasicDBObject projectFields = new BasicDBObject();
		projectFields.append("opeLogList", 1).append("_id", 0);
		BasicDBObject targetDomainIdObj = new BasicDBObject("opeLogList.opeLog.targetDomainId",
				new BasicDBObject(GlobalStrings.OPERATOR_IN, domainIdList));
		BasicDBObject logIdObj = new BasicDBObject("opeLogList.opeLog.logId", logId);
		BasicDBList ConditionList = new BasicDBList();
		ConditionList.add(targetDomainIdObj);
		ConditionList.add(logIdObj);
		BasicDBObject searchFields = new BasicDBObject(GlobalStrings.OPERATOR_AND, ConditionList);
		BasicDBObject match = new BasicDBObject(GlobalStrings.OPERATOR_MATCH, searchFields);
		BasicDBObject unwind = new BasicDBObject(GlobalStrings.OPERATOR_UNWIND, "$" + GlobalStrings.COMMON_OPELOG_LIST);

		// group operation
		BasicDBObject groupFields = new BasicDBObject("_id", null);
		groupFields.put(GlobalStrings.COMMON_OPELOG_LIST,
				new BasicDBObject(GlobalStrings.OPERATOR_PUSH, "$" + GlobalStrings.COMMON_OPELOG_LIST));
		BasicDBObject group = new BasicDBObject("$group", groupFields);
		BasicDBObject projection = new BasicDBObject(GlobalStrings.OPERATOR_PROJECT, projectFields);
		List<BasicDBObject> opeLogPipeline = new ArrayList<BasicDBObject>();
		opeLogPipeline.add(unwind);
		opeLogPipeline.add(match);
		opeLogPipeline.add(group);
		opeLogPipeline.add(projection);

		OperationLog log = new OperationLog();
		try {

			// DBからデータ取得
			List<String> listResult = dbRequest.readLogData(GlobalStrings.COMMON_OPERATION_LOG, opeLogPipeline);

			ObjectMapper mapper = new ObjectMapper();
			if (listResult.size() != 0) {
				String resultData = "{\"opeLogList\":" + listResult.toString() + "}";
				JSONObject jObject = new JSONObject(resultData);
				JSONArray jArray = jObject.getJSONArray("opeLogList");
				String opeLog = new JSONObject(jArray.get(0).toString()).getJSONObject("opeLog").toString();
				log = mapper.readValue(opeLog, OperationLog.class);

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
		} catch (MongoException e) {
			logger.error("[finedOne] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}  catch (SrdmDataNotFoundException e) {
			throw new SrdmDataNotFoundException("log Not found!!.");
		}	catch (Exception e) {
			logger.error("[finedOne] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		return log;
	}

	/**
	 * 操作ログ取得（リスト表示用） （複数ドメインの操作ログを取得）
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public List<LogForView> findAllWithPagable(GetListReq getListReq) throws SrdmDataAccessException {

		List<String> domainIdList = getListReq.getKeyListMap().get("domainIdList");
		List<LogForView> list = new ArrayList<LogForView>();
		try {
			// for (String domainId : domainIdList) {

			IDBRequest dbRequest = new DBRequest();

			BasicDBObject searchFields = new BasicDBObject(GlobalStrings.OPERATOR_MATCH,
					new BasicDBObject("opeLogList",
							new BasicDBObject(GlobalStrings.OPERATOR_ELEMMATCH,
									new BasicDBObject("opeLog.targetDomainId",
											new BasicDBObject(GlobalStrings.OPERATOR_IN, domainIdList)))));

			// unwind
			BasicDBObject unwind = new BasicDBObject(GlobalStrings.OPERATOR_UNWIND,
					"$" + GlobalStrings.COMMON_OPELOG_LIST);

			// match operation
			BasicDBObject match = new BasicDBObject(GlobalStrings.OPERATOR_MATCH, new BasicDBObject(
					"opeLogList.opeLog.targetDomainId", new BasicDBObject(GlobalStrings.OPERATOR_IN, domainIdList)));

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
			projectionFields.append("opeLogList.opeLog", 1).append("_id", 0);

			// Group operation
			BasicDBObject groupFields = new BasicDBObject("_id", null);
			groupFields.put(GlobalStrings.COMMON_OPELOG_LIST,
					new BasicDBObject(GlobalStrings.OPERATOR_PUSH, "$" + GlobalStrings.COMMON_OPELOG_LIST));
			BasicDBObject group = new BasicDBObject("$group", groupFields);

			// sort operation
			/// Order by
			BasicDBObject sortFields = new BasicDBObject();
			if (getListReq.getOrderBy().isEmpty() == false) {
				for (OrderBy orderBy : getListReq.getOrderBy()) {
					if (orderBy.getOrder().equalsIgnoreCase("ascending")) {
						sortFields.put("opeLogList.opeLog." + orderBy.getKey(), 1);
					} else {
						sortFields.put("opeLogList.opeLog." + orderBy.getKey(), -1);
					}

				}
			}
			BasicDBObject sort = new BasicDBObject(GlobalStrings.OPERATOR_SORT, sortFields);
			BasicDBObject projection = new BasicDBObject(GlobalStrings.OPERATOR_PROJECT, projectionFields);

			List<BasicDBObject> opeLogPipeline = new ArrayList<BasicDBObject>();
			opeLogPipeline.add(searchFields);
			opeLogPipeline.add(unwind);
			opeLogPipeline.add(sort);
			opeLogPipeline.add(match);
			opeLogPipeline.add(skipFields);
			opeLogPipeline.add(limitFields);
			opeLogPipeline.add(group);
			opeLogPipeline.add(projection);

			List<String> listResult = dbRequest.readLogData(GlobalStrings.COMMON_OPERATION_LOG, opeLogPipeline);
			if (listResult.size() != 0) {
				String resultData = "{\"opeLogList\":" + listResult.toString() + "}";
				JSONObject jObject = new JSONObject(resultData);
				JSONArray opeLogListArray = jObject.getJSONArray("opeLogList");
				ObjectMapper mapper = new ObjectMapper();
				Map<String, String> accountInfoMap = new HashMap<String, String>();
				for (int i = 0; i < opeLogListArray.length(); i++) {
					String opeLog = new JSONObject(opeLogListArray.get(i).toString()).getJSONObject("opeLog")
							.toString();
					LogForView logView = new LogForView();
					logView = mapper.readValue(opeLog, LogForView.class);
					
					
					String accountId = new JSONObject(opeLogListArray.get(i).toString()).getJSONObject("opeLog")
							.getString("accountId");

					if (accountInfoMap.containsKey(accountId)) {
						logView.setAccountStatus(accountInfoMap.get(accountId));
					} else {

						// Form DB request query
						BasicDBObject dbSourceObj = new BasicDBObject();
						dbSourceObj.append("account", 1).append("_id", 0).append("account.accountStatus", 1).append("account.latestErrorTimestamp", 1);

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

							if (accountStatus.equalsIgnoreCase(SrdmConstants.ACCOUNT_STATUS_LOCKED)) {
								long latestErrorTimestamp = Long
										.parseLong(jsonObj.getJSONObject("account").getString("latestErrorTimestamp"));
								long lokedTime = lockedTime * 60 * 1000;
								if ((latestErrorTimestamp + lokedTime) > System.currentTimeMillis()) {
									logView.setAccountStatus(jsonObj.getJSONObject("account").getString("accountStatus"));
									accountInfoMap.put(accountId,
											jsonObj.getJSONObject("account").getString("accountStatus"));

								} else {
									logView.setAccountStatus(SrdmConstants.ACCOUNT_STATUS_ACTIVE);
									accountInfoMap.put(accountId,
											jsonObj.getJSONObject("account").getString("accountStatus"));
								}
							} else {
								logView.setAccountStatus(jsonObj.getJSONObject("account").getString("accountStatus"));
								accountInfoMap.put(accountId,
										jsonObj.getJSONObject("account").getString("accountStatus"));
							}
						}
					}				
					
					
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
	 * 操作ログ追加
	 */
	@Override
	public void add(OperationLog log) throws SrdmDataAccessException {

		if (log.getTargetDomainId() == null || (log.getTargetDomainId().isEmpty() == true)) {
			// 通常は、あり得ないがtargetDomainIdが設定されていない場合は、エラーにする。
			logger.error("[add] targetDomainId not set.");
			throw new SrdmDataAccessException("targetDomainId not set.");
		}

		// logId
		String id = UUID.randomUUID().toString();
		log.setLogId(id);

		// kind
		if (SrdmLogConstants.ListOpeLogKind.contains(log.getKind()) == false) {
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
		if (SrdmLogConstants.ListOpeLogOperation.contains(log.getOperation()) == false) {
			log.setOperation(SrdmLogConstants.OPERATION_UNKNOWN);
		}

		// code
		if (log.getCode() == null) {
			log.setCode(SrdmLogConstants.CODE_UNKNOWN);
		}

		try {
			IDBRequest dbRequest = new DBRequest();
			ObjectMapper mapper = new ObjectMapper();
			BasicDBObject inserOpeLogObj = (BasicDBObject) JSON.parse(mapper.writeValueAsString(log));

			// DBに追加
			dbRequest.insertLogData(GlobalStrings.COMMON_OPERATION_LOG, inserOpeLogObj);

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
	 * 操作ログ削除（domainId指定）
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public void deleteByDomainIds(List<String> domainIds) throws SrdmDataAccessException {

		IDBRequest dbRequest = new DBRequest();
		BasicDBObject deleteCondition = new BasicDBObject(GlobalStrings.COMMON_OPELOG_LIST,
				new BasicDBObject("opeLog.targetDomainId", new BasicDBObject(GlobalStrings.OPERATOR_IN, domainIds)));

		try {
			dbRequest.deleteLogData(GlobalStrings.COMMON_OPERATION_LOG, deleteCondition);
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
	 * 操作ログ削除（timestamp指定）
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public void deleteByTimestamp(long timestamp) throws SrdmDataAccessException {

		IDBRequest dbRequest = new DBRequest();
		BasicDBObject deleteCondition = new BasicDBObject(GlobalStrings.COMMON_OPELOG_LIST,
				new BasicDBObject("opeLog.timestamp", new BasicDBObject(GlobalStrings.OPERATOR_LESS_THAN, timestamp)));

		try {

			// queryの実行
			dbRequest.deleteLogData(GlobalStrings.COMMON_OPERATION_LOG, deleteCondition);
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
	 * 操作ログ件数
	 *
	 * @throws SrdmDataAccessException
	 * @throws SrdmDataNotFoundException
	 */
	@Override
	public Long count(GetListReq getListReq) throws SrdmDataAccessException, SrdmDataNotFoundException {

		Long count = 0L;
		IDBRequest dbRequest = new DBRequest();
		try {

			List<String> domainIdList = getListReq.getKeyListMap().get("domainIdList");
			// unwind
			BasicDBObject unwind = new BasicDBObject(GlobalStrings.OPERATOR_UNWIND,
					"$" + GlobalStrings.COMMON_OPELOG_LIST);

			// match operation
			BasicDBObject match = new BasicDBObject(GlobalStrings.OPERATOR_MATCH, new BasicDBObject(
					"opeLogList.opeLog.targetDomainId", new BasicDBObject(GlobalStrings.OPERATOR_IN, domainIdList)));

			// Group operation
			BasicDBObject groupFields = new BasicDBObject("_id", null);
			groupFields.put(GlobalStrings.COMMON_OPELOG_LIST,
					new BasicDBObject(GlobalStrings.OPERATOR_PUSH, "$" + GlobalStrings.COMMON_OPELOG_LIST));
			BasicDBObject group = new BasicDBObject("$group", groupFields);

			List<BasicDBObject> opeLogCountPipeline = new ArrayList<BasicDBObject>();
			opeLogCountPipeline.add(unwind);
			opeLogCountPipeline.add(match);
			opeLogCountPipeline.add(group);
			// project operation
			BasicDBObject fields = new BasicDBObject("totalCount",
					new BasicDBObject(GlobalStrings.OPERATOR_SIZE, "$opeLogList.opeLog")).append("_id", 0);
			BasicDBObject project = new BasicDBObject(GlobalStrings.OPERATOR_PROJECT, fields);
			opeLogCountPipeline.add(project);
			count = Long
					.valueOf(dbRequest.getOperationLogCount(GlobalStrings.COMMON_OPERATION_LOG, opeLogCountPipeline));
			if (count == 0) {

				// データが無い場合
				logger.warn("[count] Operation Log not found. domainId[{}]",
						getListReq.getKeyListMap().get("domainIds"));
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
	 * 操作ログのエクスポート（全てのログを出力）
	 * （nowTimeは、実行時間。出力処理中にaccountStatusが変化しないように、呼出し元で現在時刻を指定）
	 */
	@Override
	public List<String> export(List<String> domainIdList, long nowTime, long startIndex, long endIndex)
			throws SrdmDataAccessException {
		List<String> opelogList = new ArrayList<String>();
		List<String> logList = new ArrayList<String>();
		try {

			IDBRequest dbRequest = new DBRequest();

			BasicDBObject searchFields = new BasicDBObject(GlobalStrings.OPERATOR_MATCH,
					new BasicDBObject("opeLogList",
							new BasicDBObject(GlobalStrings.OPERATOR_ELEMMATCH,
									new BasicDBObject("opeLog.targetDomainId",
											new BasicDBObject(GlobalStrings.OPERATOR_IN, domainIdList)))));

			// unwind
			BasicDBObject unwind = new BasicDBObject(GlobalStrings.OPERATOR_UNWIND,
					"$" + GlobalStrings.COMMON_OPELOG_LIST);

			// match operation
			BasicDBObject match = new BasicDBObject(GlobalStrings.OPERATOR_MATCH, new BasicDBObject(
					"opeLogList.opeLog.targetDomainId", new BasicDBObject(GlobalStrings.OPERATOR_IN, domainIdList)));

			BasicDBObject projectionFields = new BasicDBObject();
			projectionFields.append("opeLogList.opeLog", 1).append("_id", 0);

			// Group operation
			BasicDBObject groupFields = new BasicDBObject("_id", null);
			groupFields.put(GlobalStrings.COMMON_OPELOG_LIST,
					new BasicDBObject(GlobalStrings.OPERATOR_PUSH, "$" + GlobalStrings.COMMON_OPELOG_LIST));
			BasicDBObject group = new BasicDBObject("$group", groupFields);

			BasicDBObject projection = new BasicDBObject(GlobalStrings.OPERATOR_PROJECT, projectionFields);

			BasicDBObject skipFields = new BasicDBObject(GlobalStrings.OPERATOR_SKIP, 0);

			List<BasicDBObject> opeLogPipeline = new ArrayList<BasicDBObject>();
			opeLogPipeline.add(searchFields);
			opeLogPipeline.add(unwind);
			opeLogPipeline.add(match);
			opeLogPipeline.add(skipFields);
			opeLogPipeline.add(group);
			opeLogPipeline.add(projection);

			List<String> listResult = dbRequest.readLogData(GlobalStrings.COMMON_OPERATION_LOG, opeLogPipeline);
			if (listResult.size() != 0) {
				String resultData = "{\"opeLogList\":" + listResult.toString() + "}";
				JSONObject jObject = new JSONObject(resultData);
				JSONArray opeLogListArray = jObject.getJSONArray("opeLogList");
				ObjectMapper mapper = new ObjectMapper();
				Map<String, String> accountInfoMap = new HashMap<String, String>();

				for (int i = 0; i < opeLogListArray.length(); i++) {
					String opeLog = new JSONObject(opeLogListArray.get(i).toString()).getJSONObject("opeLog")
							.toString();
					OperationLog log = new OperationLog();
					log = mapper.readValue(opeLog, OperationLog.class);

					String accountId = new JSONObject(opeLogListArray.get(i).toString()).getJSONObject("opeLog")
							.getString("accountId");

					if (accountInfoMap.containsKey(accountId)) {
						log.setAccountStatus(accountInfoMap.get(accountId));
					} else {

						// Form DB request query
						BasicDBObject dbSourceObj = new BasicDBObject();
						dbSourceObj.append("account", 1).append("_id", 0).append("account.accountStatus", 1).append("account.latestErrorTimestamp", 1);

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

							if (accountStatus.equalsIgnoreCase(SrdmConstants.ACCOUNT_STATUS_LOCKED)) {
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
					BasicDBObject opeLogObj = new BasicDBObject("opeLog", logData);

					opelogList.add(opeLogObj.toString());
				}

				BasicDBObject opeLogObj1 = new BasicDBObject("opelogList", opelogList.toString());
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