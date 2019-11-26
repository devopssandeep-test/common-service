package srdm.cloud.commonService.domain.repository.account;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import com.srdm.mongodb.constants.GlobalStrings;
import com.srdm.mongodb.service.IDBRequest;
import com.srdm.mongodb.serviceImpl.DBPropertyEntity;
import com.srdm.mongodb.serviceImpl.DBQuery;
import com.srdm.mongodb.serviceImpl.DBRequest;

import srdm.cloud.commonService.app.bean.OrderBy;
import srdm.cloud.commonService.app.bean.SimpleFilter;
import srdm.cloud.commonService.domain.model.Account;
import srdm.cloud.commonService.domain.model.EditAccount;
import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.SimpleAccount;
import srdm.cloud.commonService.util.OxmProcessor;
import srdm.cloud.commonService.util.SrdmIdGenerator;
import srdm.cloud.commonService.util.SrdmPasswordEncoder;
import srdm.common.constant.SrdmConstants;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

@Repository
public class AccountRepositoryImpl implements AccountRepository {

	String DB_ACCOUNT_PATH = "account";
	String DB_ACCOUNT_ID_PATH = "account.accountId";
	String DB_DOMAIN_ID_PATH = "account.domainId";
	String DB_ACCOUNTNAME_PATH = "account.accountName";
	String DB_ACCOUNT_STATUS_PATH = "account.accountStatus";

	private static final Logger logger = LoggerFactory.getLogger(AccountRepositoryImpl.class);

	@Autowired
	OxmProcessor oxmProcessor;

	@Autowired
	SrdmIdGenerator srdmIdGenerator;

	@Autowired
	SrdmPasswordEncoder passwordEncoder;

	// アカウントロック時間（単位：分）
	@Value("${srdm.auth.lockedTime}")
	private long lockedTime;

	/**
	 * アカウント情報の取得（単一） （accountStatusが"deleted"のものは除く）
	 *
	 * @throws SrdmDataAccessException
	 * @throws SrdmDataNotFoundException
	 */
	@Override
	public Account findOne(String accountId) throws SrdmDataAccessException, SrdmDataNotFoundException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbSourceObj = new BasicDBObject();
		dbSourceObj.append(DB_ACCOUNT_PATH, 1).append("_id", 0);

		// Conditions
		BasicDBObject accountIdObj = new BasicDBObject(DB_ACCOUNT_ID_PATH, String.valueOf(accountId));
		BasicDBObject accountStatusObj = new BasicDBObject(DB_ACCOUNT_STATUS_PATH,
				new BasicDBObject(GlobalStrings.OPERATOR_NOT_EQUAL, SrdmConstants.ACCOUNT_STATUS_DELETED));

		BasicDBList conditionListObj = new BasicDBList();
		conditionListObj.add(accountIdObj);
		conditionListObj.add(accountStatusObj);
		BasicDBObject whereConditionObject = new BasicDBObject(GlobalStrings.OPERATOR_AND, conditionListObj);

		Account account = null;
		try {

			// DBからデータ取得
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.COMMON_ACCOUNT, 0, 0, whereConditionObject,
					dbSourceObj, new BasicDBObject());

			if (listResult.size() != 0) {
				ObjectMapper mapper = new ObjectMapper();
				String listResult1 = listResult.toString();
				JSONArray arr = new JSONArray(listResult1);
				Object arrStr = arr.get(0);
				JSONObject jsonObj = new JSONObject(arrStr.toString());
				String domainStr = jsonObj.get("account").toString();
				account = mapper.readValue(domainStr, Account.class);

			} else {
				// 指定account無し
				logger.warn("[findOne] Account not found. accountId[{}]", accountId);
				throw new SrdmDataNotFoundException("Account Not found!!.");
			}

		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[findOne] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[findOne] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (SrdmDataNotFoundException e) {
			throw new SrdmDataNotFoundException("Account Not found!!.");
		} catch (Exception e) {
			logger.error("[findOne] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		return account;
	}

	/**
	 * domainId, accountNameをキーにアカウント情報取得（単一） （accountStatusが"deleted"を含まない）
	 *
	 * @throws SrdmDataNotFoundException
	 * @throws SrdmDataAccessException
	 */
	@Override
	public Account findOneByName(String domainId, String accountName)
			throws SrdmDataNotFoundException, SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbSourceObj = new BasicDBObject();
		dbSourceObj.append(DB_ACCOUNT_PATH, 1).append("_id", 0);

		// Conditions
		BasicDBObject domainIdObj = new BasicDBObject(DB_DOMAIN_ID_PATH, String.valueOf(domainId));
		BasicDBObject accountNameObj = new BasicDBObject(DB_ACCOUNTNAME_PATH, String.valueOf(accountName));
		BasicDBObject accountStatusObj = new BasicDBObject(DB_ACCOUNT_STATUS_PATH,
				new BasicDBObject(GlobalStrings.OPERATOR_NOT_EQUAL, SrdmConstants.ACCOUNT_STATUS_DELETED));

		BasicDBList conditionListObj = new BasicDBList();
		conditionListObj.add(domainIdObj);
		conditionListObj.add(accountNameObj);
		conditionListObj.add(accountStatusObj);

		BasicDBObject whereConditionObject = new BasicDBObject(GlobalStrings.OPERATOR_AND, conditionListObj);

		Account account = null;
		try {

			// DBからデータ取得
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.COMMON_ACCOUNT, 0, 0, whereConditionObject,
					dbSourceObj, new BasicDBObject());

			if (listResult.size() != 0) {
				ObjectMapper mapper = new ObjectMapper();
				JSONArray arr = new JSONArray(listResult.toString());
				Object arrStr = arr.get(0);
				JSONObject jsonObj = new JSONObject(arrStr.toString());
				String domainStr = jsonObj.get("account").toString();
				account = mapper.readValue(domainStr, Account.class);
			} else {
				// 指定account無し
				logger.warn("[findOneByName] Account not found. domainId[{}] accountName[{}]", domainId, accountName);
				throw new SrdmDataNotFoundException("Account Not found!!.");
			}
		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[findOneByName] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[findOneByName] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (SrdmDataNotFoundException e) {
			throw new SrdmDataNotFoundException("Account Not found!!.");
		} catch (Exception e) {
			logger.error("[findOneByName] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		return account;
	}

	/**
	 * アカウントの件数を返す （accountStatusが"deleted"のものは、除く）
	 *
	 * @throws SrdmDataNotFoundException
	 * @throws SrdmDataAccessException
	 */
	@Override
	public String count(GetListReq getListReq) throws SrdmDataNotFoundException, SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Conditions
		BasicDBObject domainIdObj = new BasicDBObject(DB_DOMAIN_ID_PATH, getListReq.getKeyMap().get("domainId"));
		BasicDBObject accountStatusObj = new BasicDBObject(DB_ACCOUNT_STATUS_PATH,
				new BasicDBObject(GlobalStrings.OPERATOR_NOT_EQUAL, SrdmConstants.ACCOUNT_STATUS_DELETED));
		BasicDBObject accountIdObj = new BasicDBObject(DB_ACCOUNT_ID_PATH, new BasicDBObject(
				GlobalStrings.OPERATOR_NOT_EQUAL, getListReq.getKeyListMap().get("ignoreAccountIdList")));

		BasicDBList conditionListObj = new BasicDBList();
		conditionListObj.add(domainIdObj);
		conditionListObj.add(accountStatusObj);
		conditionListObj.add(accountIdObj);
		/*
		 * Simple Filter
		 */

		if (getListReq.getSimpleFilter().isEmpty() == false) {
			for (SimpleFilter filter : getListReq.getSimpleFilter()) {
				if (filter != null) {
					String value = filter.getValue();
					BasicDBObject filterExpression = new BasicDBObject();
					filterExpression.put(GlobalStrings.OPERATOR_REGEX, value);
					if(!filter.getKey().equalsIgnoreCase("accountStatus")){
					filterExpression.put(GlobalStrings.OPERATOR_OPTIONS, GlobalStrings.SIGN_CASE_INSENSITIVE);}
					BasicDBObject valueObj = new BasicDBObject("account." + filter.getKey(), filterExpression);
					conditionListObj.add(valueObj);
				}
			}
		}

		BasicDBObject whereConditionObject = new BasicDBObject(GlobalStrings.OPERATOR_AND, conditionListObj);

		String count = null;
		try {
			count = String.valueOf(dbRequest.getTotalCount(GlobalStrings.COMMON_ACCOUNT, whereConditionObject));

			// DBからデータ取得
			if (count == null || count.isEmpty()) {
				// データが無い場合
				logger.warn("[count] Domain or account List not found. domainId[{}]",
						getListReq.getKeyMap().get("domainId"));
				count = "0";
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
	 * アカウントリスト取得 （accountStatusが"deleted"のものは除く)
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public List<SimpleAccount> findAllWithPagable(GetListReq getListReq) throws SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbSourceObj = new BasicDBObject();
		dbSourceObj.append(DB_ACCOUNT_PATH, 1).append("_id", 0);

		List<SimpleAccount> list = new ArrayList<SimpleAccount>();

		// Conditions
		BasicDBObject domainIdObj = new BasicDBObject(DB_DOMAIN_ID_PATH, getListReq.getKeyMap().get("domainId"));
		BasicDBObject accountStatusObj = new BasicDBObject(DB_ACCOUNT_STATUS_PATH,
				new BasicDBObject(GlobalStrings.OPERATOR_NOT_EQUAL, SrdmConstants.ACCOUNT_STATUS_DELETED));
		BasicDBObject accountIdObj = new BasicDBObject(DB_ACCOUNT_ID_PATH, new BasicDBObject(
				GlobalStrings.OPERATOR_NOT_EQUAL, getListReq.getKeyListMap().get("ignoreAccountIdList")));

		BasicDBList conditionListObj = new BasicDBList();
		conditionListObj.add(domainIdObj);
		conditionListObj.add(accountStatusObj);
		conditionListObj.add(accountIdObj);

		/*
		 * Simple Filter
		 */

		if (getListReq.getSimpleFilter().isEmpty() == false) {
			for (SimpleFilter filter : getListReq.getSimpleFilter()) {
				if (filter != null) {
					String value = filter.getValue();
					BasicDBObject filterExpression = new BasicDBObject();
					filterExpression.put(GlobalStrings.OPERATOR_REGEX, value);
					if(!filter.getKey().equalsIgnoreCase("accountStatus")){
					filterExpression.put(GlobalStrings.OPERATOR_OPTIONS, GlobalStrings.SIGN_CASE_INSENSITIVE);}
					BasicDBObject valueObj = new BasicDBObject("account." + filter.getKey(), filterExpression);
					conditionListObj.add(valueObj);
				}
			}
		}

		BasicDBObject whereConditionObject = new BasicDBObject(GlobalStrings.OPERATOR_AND, conditionListObj);

		BasicDBObject sortFields = new BasicDBObject();
		/// Order by
		if (getListReq.getOrderBy().isEmpty() == false) {

			for (OrderBy orderBy : getListReq.getOrderBy()) {
				if (orderBy.getOrder().equalsIgnoreCase("ascending")) {
					sortFields.put("account." + orderBy.getKey(), 1);
				} else {
					sortFields.put("account." + orderBy.getKey(), -1);
				}

			}
		}

		try {
			// DBからデータ取得
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.COMMON_ACCOUNT, 0, 0, whereConditionObject,
					dbSourceObj, sortFields);

			if (listResult.size() != 0) {
				for (int i = 0; i < listResult.size(); i++) {
					SimpleAccount account = new SimpleAccount();
					String strResult = listResult.get(i);
					JSONObject jsonObj = new JSONObject(strResult);
					String accountStatus = jsonObj.getJSONObject("account").getString("accountStatus");

					if (accountStatus.equalsIgnoreCase(SrdmConstants.ACCOUNT_STATUS_LOCKED)) {
						long latestErrorTimestamp = Long
								.parseLong(jsonObj.getJSONObject("account").getString("latestErrorTimestamp"));
						long lokedTime = lockedTime * 60 * 1000;
						if ((latestErrorTimestamp + lokedTime) > System.currentTimeMillis()) {
							account.setAccountStatus(jsonObj.getJSONObject("account").getString("accountStatus"));

						} else {
							account.setAccountStatus(SrdmConstants.ACCOUNT_STATUS_ACTIVE);
						}
					} else {
						account.setAccountStatus(jsonObj.getJSONObject("account").getString("accountStatus"));
					}
					String roleId = jsonObj.getJSONObject("account").getString("roleId");
					List<String> roleIdList = DBQuery.fetch("amroleName")
							.where(DBPropertyEntity.withName("cmnRoleId").isEqualTo(roleId)
									.and(DBPropertyEntity.withName("cmnDomainId")
											.isEqualTo(getListReq.getKeyMap().get("domainId"))))
							.skip(0).limit(0).getAsList();
					if (roleIdList.size() != 0) {
						for (String str : roleIdList) {
							JSONObject jsonObject = new JSONObject(str);
							JSONArray roleJsonArray = jsonObject.getJSONObject("domain").getJSONObject("roleList")
									.getJSONArray("role");
							for (int r = 0; r < roleJsonArray.length(); r++) {
								JSONObject roleJsonObj = roleJsonArray.getJSONObject(r);
								String roleIdStr = roleJsonObj.getString("roleId");
								if (roleIdStr.equalsIgnoreCase(roleId)) {
									account.setRoleName(roleJsonObj.getString("roleName"));

								}
							}
						}

					}
					String accountId = jsonObj.getJSONObject("account").getString("accountId");
					account.setAccountId(accountId);
					boolean permanentAccountFlag = jsonObj.getJSONObject("account").getBoolean("isPermanentAccount");

					if (permanentAccountFlag) {
						account.setCanDelete(false);
					} else {
						if (!accountId.equalsIgnoreCase(getListReq.getKeyMap().get("loginAccountId"))) {
							account.setCanDelete(true);
						}
					}
					account.setAccountName(jsonObj.getJSONObject("account").getString("accountName"));
					list.add(account);

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
	 * アカウントの追加
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public String create(Account account) throws SrdmDataAccessException {

		// accountId Set
		String id = srdmIdGenerator.generateAccountId();
		account.setAccountId(id);
		String password;
		// password難読化
		try {
			password = passwordEncoder.getSafetyPassword(account.getPassword(), account.getAccountId());
			account.setPassword(password);
		} catch (UnsupportedEncodingException e1) {
			logger.error("[create] Password encode error.", e1);
			throw new SrdmDataAccessException("Password encode error.", e1);
		}

		// 初期値の設定
		account.setErrorCount(SrdmConstants.DEFAULT_ACCOUNT_ERROR_COUNT);
		account.setLatestErrorTimestamp(SrdmConstants.DEFAULT_ACCOUNT_LATEST_ERROR_TIMESTAMP);
		account.setAccountStatus(SrdmConstants.ACCOUNT_STATUS_ACTIVE);

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbSourceObj = new BasicDBObject();
		dbSourceObj.append(DB_ACCOUNT_PATH, 1).append("_id", 0);

		// input data
		BasicDBObject insertJsonData = new BasicDBObject();
		insertJsonData.put("accountId", id);
		insertJsonData.put("accountType", account.getAccountType());
		insertJsonData.put("accountName", account.getAccountName());
		insertJsonData.put("password", password);
		insertJsonData.put("isPermanentAccount", account.isPermanentAccount());
		insertJsonData.put("domainId", account.getDomainId());
		insertJsonData.put("roleId", account.getRoleId());
		insertJsonData.put("timeZoneSpecifingType", account.getTimeZoneSpecifingType());
		insertJsonData.put("timeZone", account.getTimeZone());
		insertJsonData.put("language", account.getLanguage());
		insertJsonData.put("dateTimeFormat", account.getDateTimeFormat());
		insertJsonData.put("homeGroupId", account.getHomeGroupId());
		insertJsonData.put("accountStatus", SrdmConstants.ACCOUNT_STATUS_ACTIVE);
		insertJsonData.put("errorCount", SrdmConstants.DEFAULT_ACCOUNT_ERROR_COUNT);
		insertJsonData.put("latestErrorTimestamp", SrdmConstants.DEFAULT_ACCOUNT_LATEST_ERROR_TIMESTAMP);

		BasicDBObject insertData = new BasicDBObject();
		insertData.put("account", insertJsonData);

		Map<String, BasicDBObject> insertDataMap = new LinkedHashMap<>();
		insertDataMap.put(GlobalStrings.COMMON_ACCOUNT, insertData);
		try {
			dbRequest.insertIntoDB(insertDataMap);

		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[create] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		}  catch (MongoException e) {
			logger.error("[create] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		return id;
	}

	/**
	 * アカウントの存在チェック （accountStatusが"deleted"のものは、存在しないアカウントとして扱う）
	 * true:指定アカウントが存在／false:指定アカウントが存在しない。
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public boolean isExist(String accountId) throws SrdmDataAccessException {

		boolean bRet = false;

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbSourceObj = new BasicDBObject();
		dbSourceObj.append(DB_ACCOUNT_PATH, 1).append("_id", 0);

		// Conditions
		BasicDBObject domainIdObj = new BasicDBObject(DB_ACCOUNT_ID_PATH, String.valueOf(accountId));
		BasicDBObject accountStatusObj = new BasicDBObject(DB_ACCOUNT_STATUS_PATH,
				new BasicDBObject(GlobalStrings.OPERATOR_NOT_EQUAL, SrdmConstants.ACCOUNT_STATUS_DELETED));

		BasicDBList conditionListObj = new BasicDBList();
		conditionListObj.add(domainIdObj);
		conditionListObj.add(accountStatusObj);

		BasicDBObject whereConditionObject = new BasicDBObject(GlobalStrings.OPERATOR_AND, conditionListObj);

		try {

			// DBからデータ取得
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.COMMON_ACCOUNT, 0, 0, whereConditionObject,
					dbSourceObj, new BasicDBObject());

			bRet = Boolean.parseBoolean(listResult.get(0)) ? false : true;
		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[isExist] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		}  catch (MongoException e) {
			logger.error("[isExist] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}
		return bRet;
	}

	/**
	 * 同一アカウント名のチェック excludeAccountIdに指定したaccountIdを除いてチェックする。
	 * （アカウント編集時、自身のアカウント名を除くために使用） （accountStatusが"deleted"のものはチェック対象としない）
	 * true:同一名のアカウントが存在／false:同一名のアカウントが存在しない
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public boolean isExistAccoutName(String domainId, String accountName, String exculdeAccountId)
			throws SrdmDataAccessException {

		boolean bRet = false;
		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbSourceObj = new BasicDBObject();
		dbSourceObj.append(DB_ACCOUNT_PATH, 1).append("_id", 0);

		// Conditions
		BasicDBObject domainIdObj = new BasicDBObject(DB_DOMAIN_ID_PATH, String.valueOf(domainId));
		BasicDBObject accountNameObj = new BasicDBObject(DB_ACCOUNTNAME_PATH, String.valueOf(accountName));
		BasicDBObject accountIdObj = new BasicDBObject(DB_ACCOUNT_ID_PATH,
				new BasicDBObject(GlobalStrings.OPERATOR_NOT_EQUAL, exculdeAccountId));
		BasicDBObject accountStatusObj = new BasicDBObject(DB_ACCOUNT_STATUS_PATH,
				new BasicDBObject(GlobalStrings.OPERATOR_NOT_EQUAL, SrdmConstants.ACCOUNT_STATUS_DELETED));

		BasicDBList conditionListObj = new BasicDBList();
		conditionListObj.add(domainIdObj);
		conditionListObj.add(accountNameObj);
		conditionListObj.add(accountStatusObj);
		conditionListObj.add(accountIdObj);

		BasicDBObject whereConditionObject = new BasicDBObject(GlobalStrings.OPERATOR_AND, conditionListObj);
		try {

			// DBからデータ取得
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.COMMON_ACCOUNT, 0, 0, whereConditionObject,
					dbSourceObj, new BasicDBObject());
			if (listResult.size() != 0) {
				bRet = true;
			}

			// bRet = Boolean.parseBoolean(listResult.get(0)) ? false : true;
		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[isExistAccoutName] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		}  catch (MongoException e) {
			logger.error("[isExistAccoutName] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}
		return bRet;
	}

	/**
	 * アカウント情報の更新
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public void update(EditAccount account) throws SrdmDataAccessException {

		/**
		 * 更新データ作成時に更新対象アカウントの情報を取得している為、ここでは存在チェックをしない。
		 */
		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Conditions
		BasicDBObject accountIdObj = new BasicDBObject(DB_ACCOUNT_ID_PATH, account.getAccountId());
		BasicDBObject accountStatusObj = new BasicDBObject(DB_ACCOUNT_STATUS_PATH,
				new BasicDBObject(GlobalStrings.OPERATOR_NOT_EQUAL, SrdmConstants.ACCOUNT_STATUS_DELETED));

		BasicDBList conditionListObj = new BasicDBList();
		conditionListObj.add(accountIdObj);
		conditionListObj.add(accountStatusObj);

		BasicDBObject whereConditionObject = new BasicDBObject(GlobalStrings.OPERATOR_AND, conditionListObj);

		// update value
		BasicDBObject updateFields = new BasicDBObject();
		updateFields.put("account.accountName", account.getAccountName());
		updateFields.put("account.roleId", account.getRoleId());
		updateFields.put("account.language", account.getLanguage());
		updateFields.put("account.dateTimeFormat", account.getDateTimeFormat());
		updateFields.put("account.timeZone", account.getTimeZoneId());
		updateFields.put("account.timeZoneSpecifingType", account.getTimeZoneSpecifingType());
		updateFields.put("account.accountName", account.getAccountName());
		updateFields.put("account.accountName", account.getAccountName());
		updateFields.put("account.isPermanentAccount",account.isPermanentAccount());
		updateFields.put("account.homeGroupId", account.getHomeGroupId());

		if (account.isChangePasswordFlag() == true) {
			try {
				updateFields.put("account.password",
						passwordEncoder.getSafetyPassword(account.getPassword(), account.getAccountId()));
			} catch (UnsupportedEncodingException e) {
				logger.warn("[update] password encode error. accountName[{}]", account.getAccountId());
				throw new SrdmDataAccessException("Password Encode error.", e);
			}
		}

		BasicDBObject setQueryObj = new BasicDBObject();
		setQueryObj.append(GlobalStrings.OPERATOR_SET, updateFields);

		Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
		collectionsMap.put(GlobalStrings.COMMON_ACCOUNT, setQueryObj);

		try {
			dbRequest.updateDB(whereConditionObject, collectionsMap, false, false);
		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[update] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		}  catch (MongoException e) {
			logger.error("[update] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}
	}

	/**
	 * 認証エラー情報設定
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public void setAuthErrorInfo(String accountId) throws SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbSourceObj = new BasicDBObject();
		dbSourceObj.append(DB_ACCOUNT_PATH, 1).append("_id", 0).append("account.errorCount", 1);

		// Conditions
		BasicDBObject accountIdObj = new BasicDBObject(DB_ACCOUNT_ID_PATH, accountId);
		BasicDBObject accountStatusObj = new BasicDBObject(DB_ACCOUNT_STATUS_PATH,
				new BasicDBObject(GlobalStrings.OPERATOR_NOT_EQUAL, SrdmConstants.ACCOUNT_STATUS_DELETED));

		BasicDBList conditionListObj = new BasicDBList();
		conditionListObj.add(accountIdObj);
		conditionListObj.add(accountStatusObj);
		BasicDBObject whereConditionObject = new BasicDBObject(GlobalStrings.OPERATOR_AND, conditionListObj);

		long errorCount = 0;
		try {

			// DBからデータ取得
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.COMMON_ACCOUNT, 0, 0, whereConditionObject,
					dbSourceObj, new BasicDBObject());

			if (listResult.size() != 0) {
				String strResult = listResult.get(0);
				JSONObject jsonObj = new JSONObject(strResult);
				errorCount = Long.parseLong(jsonObj.getJSONObject("account").getString("errorCount"));
			}
		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[setAuthErrorInfo] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		}  catch (MongoException e) {
			logger.error("[setAuthErrorInfo] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[setAuthErrorInfo] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		IDBRequest dbReqst = new DBRequest();

		// update value
		BasicDBObject updateFields = new BasicDBObject();
		Long errorCountValue = errorCount + 1;
		updateFields.put("account.errorCount", errorCountValue);
		updateFields.put("account.latestErrorTimestamp", String.valueOf(System.currentTimeMillis()));
		if (errorCountValue >= 5) {
			updateFields.put("account.accountStatus", SrdmConstants.ACCOUNT_STATUS_LOCKED);
		}

		BasicDBObject setQueryObj = new BasicDBObject();
		setQueryObj.append(GlobalStrings.OPERATOR_SET, updateFields);

		Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
		collectionsMap.put(GlobalStrings.COMMON_ACCOUNT, setQueryObj);

		try {
			dbReqst.updateDB(whereConditionObject, collectionsMap, false, false);
		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[setAuthErrorInfo] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		}  catch (MongoException e) {
			logger.error("[setAuthErrorInfo] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[setAuthErrorInfo] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}
	}

	/**
	 * 最新認証エラー日時更新
	 */
	@Override
	public void updateLatestErrorTimestamp(String accountId) throws SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Conditions
		BasicDBObject accountIdObj = new BasicDBObject(DB_ACCOUNT_ID_PATH, accountId);
		BasicDBObject accountStatusObj = new BasicDBObject(DB_ACCOUNT_STATUS_PATH,
				new BasicDBObject(GlobalStrings.OPERATOR_NOT_EQUAL, SrdmConstants.ACCOUNT_STATUS_DELETED));

		BasicDBList conditionListObj = new BasicDBList();
		conditionListObj.add(accountIdObj);
		conditionListObj.add(accountStatusObj);

		BasicDBObject whereConditionObject = new BasicDBObject(GlobalStrings.OPERATOR_AND, conditionListObj);

		// update value
		BasicDBObject updateFields = new BasicDBObject();
		updateFields.put("account.latestErrorTimestamp", String.valueOf(System.currentTimeMillis()));

		BasicDBObject setQueryObj = new BasicDBObject();
		setQueryObj.append(GlobalStrings.OPERATOR_SET, updateFields);

		Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
		collectionsMap.put(GlobalStrings.COMMON_ACCOUNT, setQueryObj);

		try {
			dbRequest.updateDB(whereConditionObject, collectionsMap, false, false);

		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[updateLatestErrorTimestamp] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		}  catch (MongoException e) {
			logger.error("[updateLatestErrorTimestamp] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[updateLatestErrorTimestamp] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}
	}

	/**
	 * 認証エラー情報クリア
	 */
	@Override
	public void clearAuthErrorInfo(List<String> accountIdList) throws SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbSourceObj = new BasicDBObject();
		dbSourceObj.append(DB_ACCOUNT_PATH, 1).append("_id", 0);

		// Conditions
		StringBuffer accountIdStrBuffer = new StringBuffer();
		for (int i = 0; i < accountIdList.size(); i++) {
			accountIdStrBuffer.append(accountIdList.get(i)).append(",");
		}
		String accIdList = accountIdStrBuffer.toString();
		accIdList = accIdList.substring(0, accIdList.length() - 1);

		BasicDBObject accountIdObj = new BasicDBObject(DB_ACCOUNT_ID_PATH,
				new BasicDBObject(GlobalStrings.OPERATOR_IN, accountIdList));
		BasicDBObject accountStatusObj = new BasicDBObject(DB_ACCOUNT_STATUS_PATH,
				new BasicDBObject(GlobalStrings.OPERATOR_NOT_EQUAL, SrdmConstants.ACCOUNT_STATUS_DELETED));

		BasicDBList conditionListObj = new BasicDBList();
		conditionListObj.add(accountIdObj);
		conditionListObj.add(accountStatusObj);
		BasicDBObject whereConditionObject = new BasicDBObject(GlobalStrings.OPERATOR_AND, conditionListObj);

		BasicDBObject updateFields = new BasicDBObject();
		updateFields.append("account.errorCount", SrdmConstants.DEFAULT_ACCOUNT_ERROR_COUNT);
		updateFields.append("account.latestErrorTimestamp", SrdmConstants.DEFAULT_ACCOUNT_LATEST_ERROR_TIMESTAMP);
		updateFields.append("account.accountStatus", SrdmConstants.ACCOUNT_STATUS_ACTIVE);

		BasicDBObject setQueryObj = new BasicDBObject();
		setQueryObj.append(GlobalStrings.OPERATOR_SET, updateFields);
		Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
		collectionsMap.put(GlobalStrings.COMMON_ACCOUNT, setQueryObj);

		try {

			// queryの実行
			dbRequest.updateDB(whereConditionObject, collectionsMap, true, true);

		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[clearAuthErrorInfo] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[clearAuthErrorInfo] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}
	}

	/**
	 * パスワードチェック
	 */
	@Override
	public boolean checkAuth(String accountId, String rawPassword)
			throws SrdmDataNotFoundException, SrdmDataAccessException {

		boolean bRet = false;

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbSourceObj = new BasicDBObject();
		dbSourceObj.append(DB_ACCOUNT_PATH, 1).append("_id", 0).append("account.password", 1);

		// Conditions
		BasicDBObject accountIdObj = new BasicDBObject(DB_ACCOUNT_ID_PATH, String.valueOf(accountId));
		BasicDBObject accountStatusObj = new BasicDBObject(DB_ACCOUNT_STATUS_PATH,
				new BasicDBObject(GlobalStrings.OPERATOR_NOT_EQUAL, SrdmConstants.ACCOUNT_STATUS_DELETED));

		BasicDBList conditionListObj = new BasicDBList();
		conditionListObj.add(accountIdObj);
		conditionListObj.add(accountStatusObj);
		BasicDBObject whereConditionObject = new BasicDBObject(GlobalStrings.OPERATOR_AND, conditionListObj);

		String password = null;
		try {

			// DBからデータ取得
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.COMMON_ACCOUNT, 0, 0, whereConditionObject,
					dbSourceObj, new BasicDBObject());

			// O/XMapping(xml to object)
			if (listResult.size() != 0) {
				String strResult = listResult.get(0);
				JSONObject jsonObj = new JSONObject(strResult);
				password = jsonObj.getJSONObject("account").getString("password");

			} else {
				// 指定account無し
				logger.warn("[checkAuth] Account not found. accountId[{}]", accountId);
				throw new SrdmDataNotFoundException("Account Not found!!.");
			}
		}  catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[checkAuth] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		}  catch (MongoException e) {
			logger.error("[checkAuth] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (SrdmDataNotFoundException e) {
			throw new SrdmDataNotFoundException("Account Not found!!.");
		} catch (Exception e) {
			logger.error("[checkAuth] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		String encodePassword;
		if (password.startsWith(SrdmConstants.OBFUSCATION_LEVEL_NO1)) {

			// Salt + Stretching (SRDM2.4.x以降)
			/**
			 * Salt + Stretchingの処理は、Java標準のAPIと独自のロジックで実施。 ただ、Spring
			 * Securityにもパスワードの難読化（Salt+Stretichng）の仕組みがあり、 必要に応じて検討すること。
			 */
			try {
				encodePassword = passwordEncoder.getSafetyPassword(rawPassword, accountId);
			} catch (UnsupportedEncodingException e) {
				// Salt＋straching化失敗
				logger.warn("[checkAuth] password encode error. accountName[{}]", accountId);
				return bRet;
			}
		} else {

			// SHA-1によるハッシュ化（SRDM2.3.x以前）
			encodePassword = "";
		}

		/*
		 * logger.debug("[TEST]raw password   :" + rawPassword); logger.debug(
		 * "[TEST]encode password:" + encodePassword); logger.debug(
		 * "[TEST]db password    :" + password);
		 */

		if (password.equals(encodePassword)) {
			bRet = true;
		}
		return bRet;
	}

	/**
	 * アカウント削除（accountStatusを"deleted"に変更）
	 */
	@Override
	public void updateAccountStatusToDeleted(List<String> accountIdList) throws SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Conditions

		BasicDBObject accountIdListObj = new BasicDBObject(DB_ACCOUNT_ID_PATH,
				new BasicDBObject(GlobalStrings.OPERATOR_IN, accountIdList.toArray()));

		BasicDBObject updateFields = new BasicDBObject();
		updateFields.append("account.accountStatus", SrdmConstants.ACCOUNT_STATUS_DELETED);

		BasicDBObject setQueryObj = new BasicDBObject();
		setQueryObj.append(GlobalStrings.OPERATOR_SET, updateFields);
		Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
		collectionsMap.put(GlobalStrings.COMMON_ACCOUNT, setQueryObj);

		try {

			// queryの実行
			dbRequest.updateDB(accountIdListObj, collectionsMap, false, true);
		}  catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[updateAccountStatusToDeleted] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		}  catch (MongoException e) {
			logger.error("[updateAccountStatusToDeleted] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}
	}

	/**
	 * ドメイン内のアカウントから指定roleIdを削除
	 */
	@Override
	public void clearRoleId(String domainId, List<String> roleIdList) throws SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbSourceObj = new BasicDBObject();
		dbSourceObj.append(DB_ACCOUNT_PATH, 1).append("_id", 0);

		// Conditions

		BasicDBObject roleListIdObj = new BasicDBObject("account.roleId", new BasicDBObject(GlobalStrings.OPERATOR_IN, roleIdList));

		BasicDBObject domainIdObj = new BasicDBObject(DB_DOMAIN_ID_PATH, domainId);
		BasicDBObject accountStatusObj = new BasicDBObject(DB_ACCOUNT_STATUS_PATH,
				new BasicDBObject(GlobalStrings.OPERATOR_NOT_EQUAL, SrdmConstants.ACCOUNT_STATUS_DELETED));

		BasicDBList conditionListObj = new BasicDBList();
		conditionListObj.add(roleListIdObj);
		conditionListObj.add(domainIdObj);
		conditionListObj.add(accountStatusObj);
		BasicDBObject whereConditionObject = new BasicDBObject(GlobalStrings.OPERATOR_AND, conditionListObj);

		BasicDBObject updateFields = new BasicDBObject();
		updateFields.append("account.roleId", "");

		BasicDBObject setQueryObj = new BasicDBObject();
		setQueryObj.append(GlobalStrings.OPERATOR_SET, updateFields);
		Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
		collectionsMap.put(GlobalStrings.COMMON_ACCOUNT, setQueryObj);

		try {

			// queryの実行
			dbRequest.updateDB(whereConditionObject, collectionsMap, false, true);
		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[clearRoleId] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[clearRoleId] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}
	}

	/**
	 * アカウントリスト取得 （accountStatusが"deleted"のものも含め全て）
	 */
	@Override
	public List<Account> findAllByDomainId(String domainId) throws SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbSourceObj = new BasicDBObject();
		dbSourceObj.append("account", 1).append("_id", 0);

		// Conditions
		BasicDBObject domainIdObj = new BasicDBObject("account.domainId", domainId);

		List<Account> accountList = null;
		try {
			// DBからデータ取得
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.COMMON_ACCOUNT, 0, 0, domainIdObj, dbSourceObj,
					new BasicDBObject());

			if (listResult.size() != 0) {
				accountList = new ArrayList<Account>();
				ObjectMapper mapper = new ObjectMapper();
				String listResult1 = listResult.toString();
				JSONArray arr = new JSONArray(listResult1);
				for (int i = 0; i < arr.length(); i++) {
					Object arrStr = arr.get(i);
					JSONObject jsonObj = new JSONObject(arrStr.toString());
					String accountStr = jsonObj.get("account").toString();
					Account account = mapper.readValue(accountStr, Account.class);
					accountList.add(account);
				}
			}
		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[findAllByDomainId] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[findAllByDomainId] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[findAllByDomainId] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		return accountList;
	}

	/**
	 * アカウント削除（DBから削除。accountId指定） （未使用）
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public void delete(List<String> accountIdList) throws SrdmDataAccessException {

		for (String accountId : accountIdList) {

			IDBRequest dbRequest = new DBRequest();

			BasicDBObject accountIdObj = new BasicDBObject("account.accountId", accountId);

			Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
			collectionsMap.put(GlobalStrings.COMMON_ACCOUNT, accountIdObj);

			try {
				dbRequest.deleteFromDB(collectionsMap);

			} catch (MongoQueryException me) {
				int errNum = me.getErrorCode();
				String errorMsg = me.getMessage();
				logger.error("[delete] DB access error.", errorMsg);
				if (errNum == 96) {
					throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
				} else {
					throw new SrdmDataAccessException(errorMsg.toString(), me);
				}
			} catch (MongoException e) {
				logger.error("[delete] DB access error.", e);
				throw new SrdmDataAccessException("DB access error.", e);
			} catch (Exception e) {
				logger.error("[delete] DB access error.", e);
				throw new SrdmDataAccessException("DB access error.", e);
			}
		}
	}

	/**
	 * アカウント削除（DBから削除。domainId指定）
	 */
	@Override
	public void deleteByDomainId(List<String> domainIdList) throws SrdmDataAccessException {

		for (String domainId : domainIdList) {

			IDBRequest dbRequest = new DBRequest();

			BasicDBObject domainIdObj = new BasicDBObject("account.domainId", domainId);

			Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
			collectionsMap.put(GlobalStrings.COMMON_ACCOUNT, domainIdObj);

			try {
				dbRequest.deleteFromDB(collectionsMap);

			} catch (MongoQueryException me) {
				int errNum = me.getErrorCode();
				String errorMsg = me.getMessage();
				logger.error("[deleteByDomainId] DB access error.", errorMsg);
				if (errNum == 96) {
					throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
				} else {
					throw new SrdmDataAccessException(errorMsg.toString(), me);
				}
			}  catch (MongoException e) {
				logger.error("[deleteByDomainId] DB access error.", e);
				throw new SrdmDataAccessException("DB access error.", e);
			} catch (Exception e) {
				logger.error("[deleteByDomainId] DB access error.", e);
				throw new SrdmDataAccessException("DB access error.", e);
			}
		}
	}

	/**
	 * アカウントロックのセット（manual）
	 *
	 * @param accountIdList
	 * @throws SrdmDataAccessException
	 */
	@Override
	public void updateAccountStatusToManualLock(List<String> accountIdList) throws SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbSourceObj = new BasicDBObject();
		dbSourceObj.append(DB_ACCOUNT_PATH, 1).append("_id", 0);

		// Conditions
		BasicDBObject accountIdObj = new BasicDBObject(DB_ACCOUNT_ID_PATH,
				new BasicDBObject(GlobalStrings.OPERATOR_IN, accountIdList));
		BasicDBObject accountStatusObj = new BasicDBObject(DB_ACCOUNT_STATUS_PATH,
				new BasicDBObject(GlobalStrings.OPERATOR_NOT_EQUAL, SrdmConstants.ACCOUNT_STATUS_DELETED));

		BasicDBList conditionListObj = new BasicDBList();
		conditionListObj.add(accountIdObj);
		conditionListObj.add(accountStatusObj);
		BasicDBObject whereConditionObject = new BasicDBObject(GlobalStrings.OPERATOR_AND, conditionListObj);

		BasicDBObject updateFields = new BasicDBObject();
		updateFields.append("account.accountStatus", SrdmConstants.ACCOUNT_STATUS_MANUAL_LOCKED);

		BasicDBObject setQueryObj = new BasicDBObject();
		setQueryObj.append(GlobalStrings.OPERATOR_SET, updateFields);
		Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
		collectionsMap.put(GlobalStrings.COMMON_ACCOUNT, setQueryObj);

		try {
			// queryの実行
			dbRequest.updateDB(whereConditionObject, collectionsMap, true, true);

		}  catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[updateAccountStatusToManualLock] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		}  catch (MongoException e) {
			logger.error("[updateAccountStatusToManualLock] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[updateAccountStatusToManualLock] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}
	}

	/**
	 * manualLocked状態の解除
	 *
	 * @param accountIdList
	 * @throws SrdmDataAccessException
	 */
	@Override
	public void clearManualLock(List<String> accountIdList) throws SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbSourceObj = new BasicDBObject();
		dbSourceObj.append(DB_ACCOUNT_PATH, 1).append("_id", 0);

		// Conditions
		StringBuffer accountIdStrBuffer = new StringBuffer();
		for (int i = 0; i < accountIdList.size(); i++) {
			accountIdStrBuffer.append(accountIdList.get(i)).append(",");
		}
		String accIdList = accountIdStrBuffer.toString();
		accIdList = accIdList.substring(0, accIdList.length() - 1);

		BasicDBObject accountIdObj = new BasicDBObject(DB_ACCOUNT_ID_PATH, accIdList);
		BasicDBObject accountStatusObj = new BasicDBObject(DB_ACCOUNT_STATUS_PATH,
				SrdmConstants.ACCOUNT_STATUS_MANUAL_LOCKED);

		BasicDBList conditionListObj = new BasicDBList();
		conditionListObj.add(accountIdObj);
		conditionListObj.add(accountStatusObj);
		BasicDBObject whereConditionObject = new BasicDBObject(GlobalStrings.OPERATOR_AND, conditionListObj);

		BasicDBObject updateFields = new BasicDBObject();
		updateFields.append("account.accountStatus", SrdmConstants.ACCOUNT_STATUS_ACTIVE);

		BasicDBObject setQueryObj = new BasicDBObject();
		setQueryObj.append(GlobalStrings.OPERATOR_SET, updateFields);
		Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
		collectionsMap.put(GlobalStrings.COMMON_ACCOUNT, setQueryObj);

		try {
			// queryの実行
			dbRequest.updateDB(whereConditionObject, collectionsMap, false, true);

		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[clearManualLock] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[clearManualLock] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[clearManualLock] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}
	}

	/**
	 * 指定したロールIDを持つアカウントリストの取得
	 */
	@Override
	public List<Account> findAllByRoleId(String roleId) throws SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbSourceObj = new BasicDBObject();
		dbSourceObj.append("account", 1).append("_id", 0);

		// Conditions
		BasicDBObject roleIdObj = new BasicDBObject("account.roleId", roleId);
		BasicDBObject accountStatusObj = new BasicDBObject(DB_ACCOUNT_STATUS_PATH,
				new BasicDBObject(GlobalStrings.OPERATOR_NOT_EQUAL, SrdmConstants.ACCOUNT_STATUS_DELETED));
		BasicDBList conditionListObj = new BasicDBList();
		conditionListObj.add(roleIdObj);
		conditionListObj.add(accountStatusObj);
		BasicDBObject whereConditionObject = new BasicDBObject(GlobalStrings.OPERATOR_AND, conditionListObj);

		List<Account> accountList = null;
		try {
			// DBからデータ取得
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.COMMON_ACCOUNT, 0, 0, whereConditionObject,
					dbSourceObj, new BasicDBObject());

			if (listResult.size() != 0) {
				accountList = new ArrayList<Account>();
				ObjectMapper mapper = new ObjectMapper();
				String listResult1 = listResult.toString();
				JSONArray arr = new JSONArray(listResult1);
				for (int i = 0; i < arr.length(); i++) {
					Object arrStr = arr.get(i);
					JSONObject jsonObj = new JSONObject(arrStr.toString());
					String accountStr = jsonObj.get("account").toString();
					Account account = mapper.readValue(accountStr, Account.class);
					accountList.add(account);
				}
			}

		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[findAllByRoleId] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[findAllByRoleId] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[findAllByRoleId] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		return accountList;
	}

}
