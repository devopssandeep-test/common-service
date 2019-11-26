package srdm.cloud.commonService.domain.repository.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.MongoQueryException;
import com.mongodb.util.JSON;
import com.srdm.mongodb.constants.GlobalStrings;
import com.srdm.mongodb.service.IDBRequest;
import com.srdm.mongodb.serviceImpl.DBPropertyEntity;
import com.srdm.mongodb.serviceImpl.DBQuery;
import com.srdm.mongodb.serviceImpl.DBRequest;

import srdm.cloud.commonService.domain.model.Domain;
import srdm.cloud.commonService.domain.model.EditDomain;
import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.GetOnlyPermissionList;
import srdm.cloud.commonService.domain.model.Permission;
import srdm.cloud.commonService.domain.model.Role;
import srdm.cloud.commonService.domain.model.SimpleDomain;
import srdm.cloud.commonService.util.OxmProcessor;
import srdm.cloud.commonService.util.SrdmIdGenerator;
import srdm.common.constant.SrdmConstants;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

@Repository
public class DomainRepositoryImpl implements DomainRepository {
	private static final Logger logger = LoggerFactory.getLogger(DomainRepositoryImpl.class);

	String DB_DOMAIN_PATH = "domain";
	String DB_DOMAIN_ID_PATH = "domain.domainId";
	String DB_PARENT_DOMAIN_ID_PATH = "domain.parentDomainId";

	@Autowired
	OxmProcessor oxmProcessor;

	@Autowired
	SrdmIdGenerator srdmIdGenerator;

	/**
	 * ドメイン取得（単一）
	 * @throws SrdmDataNotFoundException
	 * @throws SrdmDataAccessException
	 */
	@Override
	public Domain findOne(String domainId) throws SrdmDataNotFoundException, SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbOutputObj = new BasicDBObject();
		dbOutputObj.append("_id", 0);

		// Conditions
		BasicDBObject domainIdObj = new BasicDBObject(DB_DOMAIN_ID_PATH, String.valueOf(domainId));

		Domain domain = null;
		try {

			// DBからデータ取得
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.COMMON_DOMAIN, 0, 0, domainIdObj, dbOutputObj,
					new BasicDBObject());

			if (listResult.size() != 0) {
				for (String str : listResult) {
					JSONObject jsonObj = new JSONObject(str);
					Object domainobj = jsonObj.get("domain");
					ObjectMapper mapper = new ObjectMapper();

					mapper.addMixIn(Domain.class, GetOnlyPermissionList.class);
					JsonNode root = mapper.readTree(domainobj.toString());
					domain = mapper.readValue(domainobj.toString(), Domain.class);
					domain.setRoleList(fetchRoleListFrom(root.findValue("roleList").findValue("role")));
				}

			} else {
				// 指定ドメイン無し
				logger.warn("[findOne] Domain not found. domainId[{}]", domainId);
				throw new SrdmDataNotFoundException("Domain Not found!!.");
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
		}  catch (MongoException e) {
			logger.error("[findOne] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (SrdmDataNotFoundException e) {
			throw new SrdmDataNotFoundException("Domain Not found!!.");
		} catch (Exception e) {
			logger.error("[findOne] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		return domain;
	}

	@Override
	public boolean isUnderDomain(String srcDomainId, String targetDomainId) throws SrdmDataAccessException {
		boolean bRet = false;

		if (srcDomainId.equals(targetDomainId)) {
			bRet = true;
		} else {
			// DB object creation
			IDBRequest dbRequest = new DBRequest();

			// Form DB request query
			BasicDBObject dbSourceObj = new BasicDBObject();
			dbSourceObj.append("_id", 0).append("domain.upperDomainList.domain.$", 1);

			// Conditions
			BasicDBObject srcDomainIdObj = new BasicDBObject("domain.upperDomainList.domain",
					new BasicDBObject(GlobalStrings.OPERATOR_ELEMMATCH, new BasicDBObject("domainId", srcDomainId)));
			BasicDBObject domainIdObj = new BasicDBObject("domain.domainId", targetDomainId);

			BasicDBList conditionListObj = new BasicDBList();
			conditionListObj.add(domainIdObj);
			conditionListObj.add(srcDomainIdObj);

			BasicDBObject whereCondition = new BasicDBObject(GlobalStrings.OPERATOR_AND, conditionListObj);

			try {
				List<String> listResult = dbRequest.readFromDB(GlobalStrings.COMMON_DOMAIN, 0, 0, whereCondition,
						dbSourceObj, new BasicDBObject());

				if (listResult.size() != 0) {

					 bRet = true;
				}

			} catch (MongoQueryException me) {
				int errNum = me.getErrorCode();
				String errorMsg = me.getMessage();
				logger.error("[isUnderDomain] DB access error.", errorMsg);
				if (errNum == 96) {
					throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
				} else {
					throw new SrdmDataAccessException(errorMsg.toString(), me);
				}
			} catch (MongoException e) {
				logger.error("[isUnderDomain] DB access error.", e);
				throw new SrdmDataAccessException("DB access error.", e);
			} catch (Exception e) {
				logger.error("[isUnderDomain] DB access error.", e);
				throw new SrdmDataAccessException("DB access error.", e);
			}
		}

		return bRet;
	}

	/**
	 * ドメインの存在チェック
	 * true:指定ドメインが存在／false:指定ドメインが存在しない。
	 * @throws SrdmDataAccessException
	 */

	// This method is not used.
	@Override
	public boolean isExistDomain(String domainId) throws SrdmDataAccessException {

		boolean bRet = false;

		try {
			List<String> listResult = DBQuery.fetch("amIsDomainExist")
					.where(DBPropertyEntity.withName("cmnDomainId").isEqualTo(domainId)).skip(0).limit(0).getAsList();

			bRet = Boolean.parseBoolean(listResult.get(0)) ? false : true;

		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[isExistDomain] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[isExistDomain] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[isExistDomain] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}
		return bRet;
	}

	/**
	 * Themeの更新
	 */
	@Override
	public void updateTheme(String domainId, String theme) throws SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbOutputObj = new BasicDBObject();
		dbOutputObj.append("_id", 0).append(DB_DOMAIN_ID_PATH, 1);

		// Conditions
		BasicDBObject domainIdObj = new BasicDBObject(DB_DOMAIN_ID_PATH, String.valueOf(domainId));
		//update value
		BasicDBObject updateFields = new BasicDBObject("domain.theme", theme);


		BasicDBObject setQueryObj = new BasicDBObject();
		setQueryObj.append(GlobalStrings.OPERATOR_SET, updateFields);

		Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
		collectionsMap.put(GlobalStrings.COMMON_DOMAIN, setQueryObj);

		try {

			// DBからデータ取得
			dbRequest.updateDB(domainIdObj, collectionsMap, false, false);
		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[updateTheme] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[updateTheme] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

	}


	/**
	 * 配下のdomainId一覧を取得
	 */
	@Override
	public List<String> findUnderDomainId(String domainId) throws SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbSourceObj = new BasicDBObject();
		dbSourceObj.append("_id", 0).append("domain.domainId", 1);

		// Conditions
		BasicDBObject domainIdObj = new BasicDBObject("domain.upperDomainList.domain",
				new BasicDBObject(GlobalStrings.OPERATOR_ELEMMATCH, new BasicDBObject("domainId", domainId)));

		List<String> domIdList = new ArrayList<String>();

		try {
			List<String> domainIdList = dbRequest.readFromDB(GlobalStrings.COMMON_DOMAIN, 0, 0, domainIdObj,
					dbSourceObj, new BasicDBObject());
			// DBQuery.fetch("amIsDomainExist").where(DBPropertyEntity.withName("cmnDomainId").isEqualTo(domainId)).skip(0).limit(0).getAsList();
			if (domainIdList.size() != 0) {

				for (int i = 0; i < domainIdList.size(); i++) {
					JSONObject jsonObject = new JSONObject(domainIdList.get(i));
					domIdList.add(jsonObject.getJSONObject("domain").getString("domainId"));
				}
			}

		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[findUnderDomainId] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[findUnderDomainId] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[findUnderDomainId] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}
		domIdList = domIdList.stream().distinct().collect(Collectors.toList());
		return domIdList;
	}

	/**
	 * parentDomainIdを指定してドメイン一覧を取得
	 */
	@Override
	public List<SimpleDomain> findAllByParentDomainIdWithPagable(GetListReq getListReq) throws SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbSourceObj = new BasicDBObject();
		dbSourceObj.append("_id", 0).append("domain", 1).append("domain.domainId", 1).append("domain.domainName", 1)
				.append("domain.parentDomainId", 1);

		// Conditions
		BasicDBObject parentDomainIdObj = new BasicDBObject("domain.parentDomainId",
				getListReq.getKeyMap().get("domainId"));

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

		List<SimpleDomain> list = null;
		try {
			// DBからデータ取得
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.COMMON_DOMAIN, 0,
					0, parentDomainIdObj, dbSourceObj, new BasicDBObject());
			list = new ArrayList<SimpleDomain>();
			if (listResult.size() != 0) {
				ObjectMapper mapper = new ObjectMapper();
				JSONArray arr = new JSONArray(listResult.toString());
				for (int i = 0; i < arr.length(); i++) {
					Object arrStr = arr.get(i);
					JSONObject jsonObj = new JSONObject(arrStr.toString());
					String domainStr = jsonObj.get("domain").toString();
					SimpleDomain domain = mapper.readValue(domainStr, SimpleDomain.class);
					list.add(domain);
				}
			}
		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[findAllByParentDomainIdWithPagable] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[findAllByParentDomainIdWithPagable] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[findAllByParentDomainIdWithPagable] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		return list;
	}

	/**
	 * parentDomainIdを指定して件数を取得
	 */
	@Override
	public long count(String parentDomainId) throws SrdmDataNotFoundException, SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		// Conditions
		BasicDBObject parentDomainIdObj = new BasicDBObject("domain.parentDomainId", parentDomainId);

		long count = 0;
		try {

			// DBからデータ取得
			count = dbRequest.getTotalCount(GlobalStrings.COMMON_DOMAIN, parentDomainIdObj);
			if (count <= 0) {
				// データが無い場合
				logger.warn("[count] Domain List not found. parentDomainId[{}]", parentDomainId);
				count = 0;
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
	 * ドメインを作成
	 */
	@Override
	public String create(Domain domain) throws SrdmDataAccessException {

		// accountId Set
		String id = srdmIdGenerator.generateDomainId();
		domain.setDomainId(id);

		/**
		 * 初期値設定
		 */
		domain.setTheme(SrdmConstants.DEFAULT_DOMAIN_AM_THEME);
		domain.setRoleList(new ArrayList<Role>());

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// input data
		BasicDBObject insertJsonData = new BasicDBObject();
		insertJsonData.put("domainId", id);
		insertJsonData.put("domainName", domain.getDomainName());
		insertJsonData.put("parentDomainId", domain.getParentDomainId());
		insertJsonData.put("theme", domain.getTheme());

		ObjectMapper mapper = new ObjectMapper();
		try {
			JSONArray upperDomainArrayList = new JSONArray();

			List<String> listResult = DBQuery.fetch("amIsDomainExist")
					.where(DBPropertyEntity.withName("cmnDomainId").isEqualTo(domain.getParentDomainId())).skip(0)
					.limit(0).getAsList();
			if (listResult.size() > 0) {

				for (int i = 0; i < listResult.size(); i++) {
					JSONObject jsonObject = new JSONObject(listResult.get(i));
					if (jsonObject.getJSONObject("domain").has("upperDomainList")) {
						upperDomainArrayList = jsonObject.getJSONObject("domain").getJSONObject("upperDomainList")
								.getJSONArray("domain");
					}

				}
			}
			BasicDBObject parentDomainId = new BasicDBObject("domainId", domain.getParentDomainId());
			upperDomainArrayList.put(parentDomainId);

			// upperDomainList

			BasicDBObject domainIds = new BasicDBObject("domain", JSON.parse(upperDomainArrayList.toString()));
			insertJsonData.put("upperDomainList", domainIds);

			DBObject roleListObj = (DBObject) JSON.parse(mapper.writeValueAsString(domain.getRoleList()));
			BasicDBObject insertroleData = new BasicDBObject();
			insertroleData.put("role", roleListObj);
			insertJsonData.put("roleList", insertroleData);

			BasicDBObject insertData = new BasicDBObject();
			insertData.put("domain", insertJsonData);

			Map<String, BasicDBObject> insertDataMap = new LinkedHashMap<>();
			insertDataMap.put(GlobalStrings.COMMON_DOMAIN, insertData);

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
		} catch (MongoException e) {
			logger.error("[create] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[create] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		return id;
	}

	/**
	 * ドメインを更新
	 * @throws SrdmDataNotFoundException
	 */
	@Override
	public void update(EditDomain domain) throws SrdmDataAccessException, SrdmDataNotFoundException {

		/**
		 * 呼出し元で対象ドメインの存在チェックは、実施される。
		 */

		BasicDBObject domainIdObj = new BasicDBObject("domain.domainId", domain.getDomainId());

		BasicDBObject updateFields = new BasicDBObject();
		IDBRequest dbRequest = new DBRequest();

		try {
			JSONArray grantedPer = new JSONArray();
			updateFields.put(GlobalStrings.OPERATOR_SET,
					new BasicDBObject("domain.domainName", domain.getDomainName()));

			Map<String, BasicDBObject> insertDataMap = new LinkedHashMap<>();
			insertDataMap.put(GlobalStrings.COMMON_DOMAIN, updateFields);
			dbRequest.updateDB(domainIdObj, insertDataMap, false, true);
		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[update] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[update] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[update] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}
	}

	/**
	 * ドメインを削除
	 */
	@Override
	public void delete(List<String> domainIdList) throws SrdmDataAccessException {

		IDBRequest dbRequest = new DBRequest();

		for (String domainId : domainIdList) {
			BasicDBObject domainIdObj = new BasicDBObject("domain.domainId", domainId);

			Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
			collectionsMap.put(GlobalStrings.COMMON_DOMAIN, domainIdObj);
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
	 * 同一ドメイン名のチェック
	 * excludeDomainIdに指定したdomainIdを除いてチェックする。
	 * （ドメイン作成／編集時、自身のドメイン名を除くために使用）
	 * true:同一名のドメインが存在／false:同一名のドメインが存在しない
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public boolean isExistDomainName(String domainName, String excludeDomainId) throws SrdmDataAccessException {

		boolean bRet = false;

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbOutputObj = new BasicDBObject();
		dbOutputObj.append("_id", 0);


		// Conditions
		BasicDBObject domainNameObj = new BasicDBObject("domain.domainName", domainName);
		BasicDBObject domainIdObj = new BasicDBObject("domain.domainId", new BasicDBObject(GlobalStrings.OPERATOR_NOT_EQUAL, excludeDomainId ));

		BasicDBList conditionListObj = new BasicDBList();
		conditionListObj.add(domainNameObj);
		conditionListObj.add(domainIdObj);
		BasicDBObject whereConditionObject = new BasicDBObject(GlobalStrings.OPERATOR_AND, conditionListObj);
		try {

			// DBからデータ取得
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.COMMON_DOMAIN, 0, 0, whereConditionObject, dbOutputObj,
					new BasicDBObject());

			if(listResult.size() > 0){
				bRet = true;
			}

		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[isExistDomainName] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[isExistDomainName] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[isExistDomainName] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}
		return bRet;
	}


	public static List<Permission> fetchPermissionListFrom(JsonNode node) throws Exception {
		List<Permission> permissions = new ArrayList<>();
		Iterator<JsonNode> iterator = node.iterator();
		while (iterator.hasNext()) {
			ObjectMapper m = new ObjectMapper();
			permissions.add(m.treeToValue(iterator.next(), Permission.class));
		}
		return permissions;
	}

	public static List<Role> fetchRoleListFrom(JsonNode node) throws Exception {
		List<Role> roles = new ArrayList<>();
		Iterator<JsonNode> iterator = node.iterator();
		while (iterator.hasNext()) {
			ObjectMapper m = new ObjectMapper();
			m.addMixIn(Role.class, GetOnlyPermissionList.class);
			Role role = m.treeToValue(iterator.next(), Role.class);
			if (node.findValue("permissionList").has("permission")) {
				role.setPermissionList(fetchPermissionListFrom(node.findValue("permissionList").findValue("permission")));
			} else {
				role.setPermissionList(fetchPermissionListFrom(node.findValue("permissionList")));
			}

			roles.add(role);
		}
		return roles;
	}


}
