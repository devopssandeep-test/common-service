package srdm.cloud.commonService.domain.repository.domain;

import java.util.ArrayList;
import java.util.Iterator;
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

import com.fasterxml.jackson.core.JsonProcessingException;
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

import srdm.cloud.commonService.app.bean.OrderBy;
import srdm.cloud.commonService.app.bean.SimpleFilter;
import srdm.cloud.commonService.domain.model.EditRole;
import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.GetOnlyPermissionList;
import srdm.cloud.commonService.domain.model.Permission;
import srdm.cloud.commonService.domain.model.Role;
import srdm.cloud.commonService.domain.model.SimpleRole;
import srdm.cloud.commonService.util.OxmProcessor;
import srdm.cloud.commonService.util.SrdmIdGenerator;
import srdm.common.constant.SrdmConstants;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

@Repository
public class RoleRepositoryImpl implements RoleRepository {

	private static final Logger logger = LoggerFactory.getLogger(RoleRepositoryImpl.class);

	String DB_DOMAIN_NODE = "domain";
	String DB_DOMAIN_ID_NODE = "domain.domainId";
	String DB_ROLE_ID_NODE = "domain.roleList.role.roleId";
	String DB_ACCOUNT_STATUS_PATH = "account.accountStatus";

	@Autowired
	OxmProcessor oxmProcessor;

	@Autowired
	SrdmIdGenerator srdmIdGenerator;

	@Value("${srdm.login.timeout}")
	private long timeout;

	/**
	 * ロール取得（単一）
	 *
	 * @throws SrdmDataNotFoundException
	 * @throws SrdmDataAccessException
	 */
	@Override
	public Role findOne(String domainId, String roleId) throws SrdmDataNotFoundException, SrdmDataAccessException {

		Role role = null;
		try {

			List<String> listResult = DBQuery.fetch("amroleName")
					.where(DBPropertyEntity.withName("cmnDomainId").isEqualTo(domainId)
							.and(DBPropertyEntity.withName("cmnRoleId").isEqualTo(roleId)))
					.skip(0).limit(0).getAsList();

			if (listResult.size() != 0) {
				ObjectMapper mapper = new ObjectMapper();
				for (String str : listResult) {
					JSONObject jsonObj = new JSONObject(str);
					Object domainobj = jsonObj.get("domain");
					JSONObject rollListObj = (JSONObject) ((JSONObject) domainobj).get("roleList");
					JSONArray dbRespArrayList = rollListObj.getJSONArray("role");
					for (int i = 0; i < dbRespArrayList.length(); i++) {
						String roleIdValue = (String) dbRespArrayList.getJSONObject(i).get("roleId");
						if (roleIdValue.equalsIgnoreCase(roleId)) {
							mapper.addMixIn(Role.class, GetOnlyPermissionList.class);
							JsonNode root = mapper.readTree(dbRespArrayList.get(i).toString());
							role = mapper.readValue(dbRespArrayList.get(i).toString(), Role.class);

							if (root.findValue("permissionList").has("permission")) {
								role.setPermissionList(fetchPermissionListFrom(
										root.findValue("permissionList").findValue("permission")));
							} else {
								role.setPermissionList(fetchPermissionListFrom(root.findValue("permissionList")));
							}

							if (root.has("sessionTimeout") == false) {
								role.setSessionTimeout(timeout);
							}
						}

					}

				}

			} else {
				// 指定Role無し
				logger.warn("[findOne] Role Info not found. domainId[{}], roleIdId[{}]", domainId, roleId);
				throw new SrdmDataNotFoundException("Role Not found!!.");
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
		} catch (MongoException | JsonProcessingException e) {
			logger.error("[findOne] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (SrdmDataNotFoundException e) {
			throw new SrdmDataNotFoundException("Role Not found!!.");
		} catch (Exception e) {
			logger.error("[findOne] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		return role;
	}

	/**
	 * ロール編集
	 *
	 * @throws SrdmDataAccessException
	 * @throws SrdmDataNotFoundException
	 */
	@Override
	public void update(EditRole editRole) throws SrdmDataAccessException, SrdmDataNotFoundException {

		// 編集対象のロールの存在チェック
		Role role = findOne(editRole.getDomainId(), editRole.getRoleId());
		logger.info("isRoleCanEdit=" + editRole.isRoleCanEdit());
		BasicDBObject domainIdObj = new BasicDBObject("domain.domainId", editRole.getDomainId());
		try {
			// DB object creation
			IDBRequest dbRequest = new DBRequest();

			// input data
			BasicDBObject insertJsonData = new BasicDBObject();
			insertJsonData.put("roleId", editRole.getRoleId());
			insertJsonData.put("roleName", editRole.getRoleName());
			insertJsonData.put("description", editRole.getDescription());

			BasicDBObject roleAttributeData = new BasicDBObject();
			roleAttributeData.put("isRoleCanEdit", editRole.isRoleCanEdit());
			roleAttributeData.put("isPrivateRole", role.getRoleAttribute().isPrivateRole());

			insertJsonData.put("roleAttribute", roleAttributeData);

			JSONArray grantedPer = new JSONArray();
			// 付与権限Query生成
			if (editRole.getGrantedPermissionList() != null && editRole.getGrantedPermissionList().isEmpty() == false) {
				ObjectMapper mapper = new ObjectMapper();
				List<String> permList = editRole.getGrantedPermissionList();
				for (String perm : permList) {
					Permission permission = new Permission();
					permission.setPermissionName(perm);
					permission.setAttribute(SrdmConstants.PERMISSION_MAP.get(perm));
					BasicDBObject grantedRoleObj = (BasicDBObject) JSON.parse(mapper.writeValueAsString(permission));
					grantedPer.put(grantedRoleObj);
				}
				logger.debug("[update] GrantPermission[{}]", grantedPer.toString());
			}
			BasicDBObject permObj = new BasicDBObject("permission", JSON.parse(grantedPer.toString()));
			insertJsonData.put("permissionList", permObj);

			long sessionTimeout = editRole.getSessionTimeout();
			if (sessionTimeout == -1) { // -1の場合は変更しない(元の値をセットする)
				insertJsonData.put("sessionTimeout", role.getSessionTimeout());
			} else {
				insertJsonData.put("sessionTimeout", sessionTimeout);
			}

			BasicDBObject insertRoleData = new BasicDBObject();
			insertRoleData.put("domain.roleList.role", insertJsonData);

			BasicDBObject setQueryObj = new BasicDBObject();
			setQueryObj.append(GlobalStrings.OPERATOR_PUSH, insertRoleData);

			Map<String, BasicDBObject> insertDataMap = new LinkedHashMap<>();
			insertDataMap.put(GlobalStrings.COMMON_DOMAIN, setQueryObj);
			List<String> roleIdList = new ArrayList<String>();
			roleIdList.add(editRole.getRoleId());

			delete(editRole.getDomainId(), roleIdList);

			dbRequest.updateDB(domainIdObj, insertDataMap, false, false);
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
		} catch (Exception e) {
			logger.error("[update] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}
	}

	/**
	 * ロールリストの件数取得
	 *
	 * @throws SrdmDataAccessException
	 * @throws SrdmDataNotFoundException
	 */
	@Override
	public String count(GetListReq getListReq) throws SrdmDataAccessException, SrdmDataNotFoundException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbSourceObj = new BasicDBObject();
		dbSourceObj.append("domain.roleList", 1).append("_id", 0);

		// Conditions
		BasicDBObject domainIdObj = new BasicDBObject("domain.domainId", getListReq.getKeyMap().get("domainId"));
		String count = null;
		try {
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.COMMON_DOMAIN, 0, 0, domainIdObj, dbSourceObj,
					new BasicDBObject());
			if (listResult.size() != 0) {
				for (String str : listResult) {
					JSONObject jsonObject = new JSONObject(str);
					JSONArray roleJsonArray = jsonObject.getJSONObject("domain").getJSONObject("roleList")
							.getJSONArray("role");
					count = String.valueOf(roleJsonArray.length());
				}
			}

			// DBからデータ取得
			if (count == null || count.isEmpty()) {
				// データが無い場合
				logger.warn("[count] Domain or Role List not found. domainId[{}]",
						getListReq.getKeyMap().get("domainId"));
				count = "0";
			}
		}  catch (MongoQueryException me) {
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
	 * ロールリスト取得
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public List<SimpleRole> findAllWithPagable(GetListReq getListReq) throws SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		List<SimpleRole> list = new ArrayList<SimpleRole>();

		List<BasicDBObject> pipeline = makeRoleListPipelineObject(getListReq);

		try {
			List<BasicDBObject> listResult = dbRequest.readDatawithAggregate(GlobalStrings.COMMON_DOMAIN, pipeline);
			if (listResult.size() > 0) {
				for (BasicDBObject dbRoleInfoObject : listResult) {
					BasicDBList roleListObj = (BasicDBList) dbRoleInfoObject.get("role");
					BasicDBObject[] roleListArrObj = roleListObj.toArray(new BasicDBObject[0]);
					for (BasicDBObject roleInfoObj : roleListArrObj) {
						SimpleRole role = new SimpleRole();
						String strRoleId = roleInfoObj.getString("roleId");
						BasicDBObject roleAttrObj = (BasicDBObject) roleInfoObj.get("roleAttribute");
						boolean boolPrivateRole = roleAttrObj.getBoolean("isPrivateRole");
						if (boolPrivateRole) {
							role.setCanDelete(false);
						} else {
							if (!strRoleId.equalsIgnoreCase(getListReq.getKeyMap().get("loginAccountRoleId"))) {
								role.setCanDelete(true);
							}
						}
						// Conditions
						BasicDBObject domainId = new BasicDBObject("account.domainId",
								getListReq.getKeyMap().get("domainId"));
						BasicDBObject roleIdObj = new BasicDBObject("account.roleId", strRoleId);
						BasicDBObject accountStatusObj = new BasicDBObject(DB_ACCOUNT_STATUS_PATH, new BasicDBObject(
								GlobalStrings.OPERATOR_NOT_EQUAL, SrdmConstants.ACCOUNT_STATUS_DELETED));

						BasicDBList conditionListObj = new BasicDBList();
						conditionListObj.add(domainId);
						conditionListObj.add(accountStatusObj);
						conditionListObj.add(roleIdObj);

						BasicDBObject whereConditionObject = new BasicDBObject(GlobalStrings.OPERATOR_AND,
								conditionListObj);
						long linkedAccountCount = dbRequest.getTotalCount(GlobalStrings.COMMON_ACCOUNT,
								whereConditionObject);
						if (linkedAccountCount > 0) {
							role.setLinkedAccount(true);
						}
						role.setRoleId(strRoleId);
						role.setRoleName(roleInfoObj.getString("roleName"));
						role.setDescription(roleInfoObj.getString("description"));
						role.setRoleCanEdit(roleAttrObj.getBoolean("isRoleCanEdit"));
						role.setPrivateRole(roleAttrObj.getBoolean("isPrivateRole"));
						role.setSessionTimeout(roleInfoObj.getLong("sessionTimeout"));
						list.add(role);

					}
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
	 * ロール作成
	 */
	@Override
	public String create(String domainId, Role role) throws SrdmDataAccessException {

		// roleId set
		String id = srdmIdGenerator.generateRoleId();
		role.setRoleId(id);

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// input data
		BasicDBObject insertJsonData = new BasicDBObject();
		insertJsonData.put("roleId", id);
		insertJsonData.put("roleName", role.getRoleName());
		insertJsonData.put("description", role.getDescription());

		BasicDBObject roleAttributeData = new BasicDBObject();
		roleAttributeData.put("isRoleCanEdit", role.getRoleAttribute().isRoleCanEdit());
		roleAttributeData.put("isPrivateRole", false);
		insertJsonData.put("roleAttribute", roleAttributeData);

		insertJsonData.put("sessionTimeout", role.getSessionTimeout());

		ObjectMapper mapper = new ObjectMapper();
		try {

			BasicDBObject insertData = new BasicDBObject();
			insertData.put("role", insertJsonData);

			// BasicDBObject inserdataObj = (BasicDBObject)
			// JSON.parse(mapper.writeValueAsString(role));
			DBObject permissionListObj = (DBObject) JSON.parse(mapper.writeValueAsString(role.getPermissionList()));
			BasicDBObject permission = new BasicDBObject("permission", permissionListObj);
			insertJsonData.put("permissionList", permission);

			BasicDBObject insertRoleData = new BasicDBObject();
			insertRoleData.put("domain.roleList.role", insertJsonData);

			BasicDBObject setQueryObj = new BasicDBObject();
			setQueryObj.append(GlobalStrings.OPERATOR_PUSH, insertRoleData);

			Map<String, BasicDBObject> insertDataMap = new LinkedHashMap<>();
			insertDataMap.put(GlobalStrings.COMMON_DOMAIN, setQueryObj);

			BasicDBObject domainIdObj = new BasicDBObject("domain.domainId", domainId);

			// DBからデータ取得
			dbRequest.updateDB(domainIdObj, insertDataMap, false, false);

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
	 * ロール削除
	 */
	@Override
	public void delete(String domainId, List<String> roleIdList) throws SrdmDataAccessException {


			IDBRequest dbRequest = new DBRequest();

			BasicDBObject domainIdObj = new BasicDBObject("domain.domainId", domainId);

			BasicDBObject deleteRoleObj = new BasicDBObject(GlobalStrings.OPERATOR_PULL,
					new BasicDBObject("domain.roleList.role", new BasicDBObject("roleId", new BasicDBObject(GlobalStrings.OPERATOR_IN, roleIdList))));

			Map<String, BasicDBObject> insertDataMap = new LinkedHashMap<>();
			insertDataMap.put(GlobalStrings.COMMON_DOMAIN, deleteRoleObj);
			try {
				dbRequest.updateDB(domainIdObj, insertDataMap, true, true);
				// queryの実行

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

	/**
	 * ロールの存在チェック
	 */
	@Override
	public boolean isExist(String domainId, String roleId) throws SrdmDataAccessException {

		boolean bRet = false;

		try {
			List<String> listResult = DBQuery.fetch("amIsDomainExist")
					.where(DBPropertyEntity.withName("cmnDomainId").isEqualTo(domainId)
							.and(DBPropertyEntity.withName("cmnRoleId").isEqualTo(roleId)))
					.skip(0).limit(0).getAsList();

			// bRet = Boolean.parseBoolean(listResult.get(0)) ? false : true;
			if (listResult.size() > 0) {
				bRet = true;
			}
		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[isExist] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[isExist] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[isExist] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}
		return bRet;
	}

	/**
	 * ロールリスト（指定ドメイン配下のものを全て取得）
	 */
	@Override
	public List<Role> findAllByDomainId(String domainId) throws SrdmDataAccessException {
		List<Role> list = new ArrayList<Role>();
		try {

			List<String> listResult = DBQuery.fetch("amroleName")
					.where(DBPropertyEntity.withName("cmnDomainId").isEqualTo(domainId)).skip(0).limit(0).getAsList();

			if (listResult.size() > 0) {
				ObjectMapper mapper = new ObjectMapper();
				for (String str : listResult) {
					JSONObject jsonObj = new JSONObject(str);
					Object domainobj = jsonObj.get("domain");
					JSONObject rollListObj = (JSONObject) ((JSONObject) domainobj).get("roleList");
					JSONArray DBRespArrayList = rollListObj.getJSONArray("role");
					for (int i = 0; i < DBRespArrayList.length(); i++) {
						Role role = new Role();
						mapper.addMixIn(Role.class, GetOnlyPermissionList.class);
						JsonNode root = mapper.readTree(DBRespArrayList.get(i).toString());
						role = mapper.readValue(DBRespArrayList.get(i).toString(), Role.class);

						if (root.findValue("permissionList").has("permission")) {
							role.setPermissionList(
									fetchPermissionListFrom(root.findValue("permissionList").findValue("permission")));
						} else {
							role.setPermissionList(fetchPermissionListFrom(root.findValue("permissionList")));
						}
						list.add(role);
					}

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

		return list;
	}

	public static List<Permission> fetchPermissionListFrom(JsonNode node) throws JsonProcessingException {
		List<Permission> permissions = new ArrayList<>();
		Iterator<JsonNode> iterator = node.iterator();
		while (iterator.hasNext()) {
			ObjectMapper m = new ObjectMapper();
			permissions.add(m.treeToValue(iterator.next(), Permission.class));
		}
		return permissions;
	}

	public static List<Role> fetchRoleListFrom(JsonNode node) throws JsonProcessingException {
		List<Role> roles = new ArrayList<>();
		Iterator<JsonNode> iterator = node.iterator();
		while (iterator.hasNext()) {
			ObjectMapper m = new ObjectMapper();
			m.addMixIn(Role.class, GetOnlyPermissionList.class);
			Role role = m.treeToValue(iterator.next(), Role.class);
			if (node.findValue("permissionList").has("permission")) {
				role.setPermissionList(
						fetchPermissionListFrom(node.findValue("permissionList").findValue("permission")));
			} else {
				role.setPermissionList(fetchPermissionListFrom(node.findValue("permissionList")));
			}
			roles.add(role);
		}
		return roles;
	}

	/**
	 * Role deletion impossible attribute (with tying restriction) updated Not
	 * applicable for role update.
	 */
	@Override
	public void updatePrivateRoleAttribute(String domainId, String roleId, boolean editPrivateRole)
			throws SrdmDataAccessException, SrdmDataNotFoundException {
		try {
			List<String> roleIdList = new ArrayList<String>();
			roleIdList.add(roleId);

			Role role = findOne(domainId, roleId);

			// input data
			BasicDBObject insertJsonRoleData = new BasicDBObject();
			insertJsonRoleData.put("roleId", roleId);
			insertJsonRoleData.put("roleName", role.getRoleName());
			insertJsonRoleData.put("description", role.getDescription());

			Boolean isRoleCanEdit = role.getRoleAttribute().isRoleCanEdit();
			BasicDBObject roleAttributeData = new BasicDBObject();
			roleAttributeData.put("isRoleCanEdit", isRoleCanEdit);
			roleAttributeData.put("isPrivateRole", editPrivateRole);
			insertJsonRoleData.put("roleAttribute", roleAttributeData);

			ObjectMapper mapper = new ObjectMapper();

			BasicDBObject insertData = new BasicDBObject();
			insertData.put("role", insertJsonRoleData);
			DBObject permissionListObj = (DBObject) JSON.parse(mapper.writeValueAsString(role.getPermissionList()));
			BasicDBObject permission = new BasicDBObject("permission", permissionListObj);
			insertJsonRoleData.put("permissionList", permission);

			insertJsonRoleData.put("sessionTimeout", role.getSessionTimeout());

			delete(domainId, roleIdList);

			// DB object creation
			IDBRequest dbRequest = new DBRequest();

			// conditions
			BasicDBObject domainIdObj = new BasicDBObject("domain.domainId", domainId);

			// input data
			BasicDBObject insertRoleData = new BasicDBObject();
			insertRoleData.put("domain.roleList.role", insertJsonRoleData);

			BasicDBObject setQueryObj = new BasicDBObject();
			setQueryObj.append(GlobalStrings.OPERATOR_PUSH, insertRoleData);

			Map<String, BasicDBObject> insertDataMap = new LinkedHashMap<>();
			insertDataMap.put(GlobalStrings.COMMON_DOMAIN, setQueryObj);
			dbRequest.updateDB(domainIdObj, insertDataMap, false, false);

		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[updatePrivateRoleAttribute] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[updatePrivateRoleAttribute] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[updatePrivateRoleAttribute] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}
	}


	/**
	 *
	 * @throws SrdmDataNotFoundException
	 * @throws SrdmDataAccessException
	 */
	@Override
	public Role getRoleDetails(String domainId, String roleId)
			throws SrdmDataNotFoundException, SrdmDataAccessException {

		Role role = null;
		try {

			List<String> listResult = DBQuery.fetch("amroleName")
					.where(DBPropertyEntity.withName("cmnDomainId").isEqualTo(domainId)
							.and(DBPropertyEntity.withName("cmnRoleId").isEqualTo(roleId)))
					.skip(0).limit(0).getAsList();

			if (listResult.size() != 0) {
				ObjectMapper mapper = new ObjectMapper();
				for (String str : listResult) {
					JSONObject jsonObj = new JSONObject(str);
					Object domainobj = jsonObj.get("domain");
					JSONObject rollListObj = (JSONObject) ((JSONObject) domainobj).get("roleList");
					JSONArray dbRespArrayList = rollListObj.getJSONArray("role");
					for (int i = 0; i < dbRespArrayList.length(); i++) {
						String roleIdValue = (String) dbRespArrayList.getJSONObject(i).get("roleId");
						if (roleIdValue.equalsIgnoreCase(roleId)) {
							mapper.addMixIn(Role.class, GetOnlyPermissionList.class);
							JsonNode root = mapper.readTree(dbRespArrayList.get(i).toString());
							role = mapper.readValue(dbRespArrayList.get(i).toString(), Role.class);

							if (root.findValue("permissionList").has("permission")) {
								role.setPermissionList(fetchPermissionListFrom(
										root.findValue("permissionList").findValue("permission")));
							} else {
								role.setPermissionList(fetchPermissionListFrom(root.findValue("permissionList")));
							}

							if (root.has("sessionTimeout") == false) {
								role.setSessionTimeout(timeout);
							}
						}

					}

				}

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
		} catch (MongoException | JsonProcessingException e) {
			logger.error("[findOne] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[findOne] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		return role;
	}

	/**
	 *
	 * @param getListReq
	 * @param isListOpr
	 * @return
	 */
	private List<BasicDBObject> makeRoleListPipelineObject(GetListReq getListReq) {

		List<BasicDBObject> pipeline = new ArrayList<BasicDBObject>();

		// Conditions
		BasicDBObject domainIdObj = new BasicDBObject("domain.domainId", getListReq.getKeyMap().get("domainId"));

		// $match
		BasicDBObject matchObject = new BasicDBObject(GlobalStrings.OPERATOR_MATCH, domainIdObj);
		pipeline.add(matchObject);

		// $unwind
		BasicDBObject unwind = new BasicDBObject(GlobalStrings.OPERATOR_UNWIND, "$domain.roleList.role");
		pipeline.add(unwind);

		// $addFields: for sessionTimeout node that is introduced in SRDM2.8.
		BasicDBList ifNullList = new BasicDBList();
		ifNullList.add("$domain.roleList.role.sessionTimeout");
		ifNullList.add(timeout); // use default timeout value if sessionTimeout node does not exist.
		BasicDBObject addFields = new BasicDBObject(GlobalStrings.OPERATOR_ADDFields,
				new BasicDBObject("domain.roleList.role.sessionTimeout",
						new BasicDBObject(GlobalStrings.OPERATOR_IFNULL, ifNullList)));
		pipeline.add(addFields);

		// Condition: Simple Filter
		BasicDBList conditionListObj = new BasicDBList();
		if (!getListReq.getSimpleFilter().isEmpty()) {
			for (SimpleFilter filter : getListReq.getSimpleFilter()) {
				if (filter != null) {
					String key = filter.getKey();
					String value = filter.getValue();
					boolean bSessionTimeout = false;
					if ("sessionTimeout".equalsIgnoreCase(key) == true) {
						BasicDBObject sessionTimeoutObj = new BasicDBObject();
						Long val = null;
						try {
							val = Long.parseLong(value);
							sessionTimeoutObj.put("domain.roleList.role.sessionTimeout", val);
							conditionListObj.add(sessionTimeoutObj);
							bSessionTimeout = true;
						} catch (NumberFormatException e) {
							bSessionTimeout = false;
						}
					}
					if (bSessionTimeout == false) {
						BasicDBObject filterExpression = new BasicDBObject();
						filterExpression.put(GlobalStrings.OPERATOR_REGEX, ".*" + value + ".*");
						filterExpression.put(GlobalStrings.OPERATOR_OPTIONS, GlobalStrings.SIGN_CASE_INSENSITIVE);
						BasicDBObject valueObj = new BasicDBObject("domain.roleList.role." + key,
								filterExpression);
						conditionListObj.add(valueObj);
					}
				}
			}
		}

		// filterMatch
		if(conditionListObj.size() != 0) {
			BasicDBObject filterMatchObject = new BasicDBObject(GlobalStrings.OPERATOR_MATCH,
					new BasicDBObject(GlobalStrings.OPERATOR_AND, conditionListObj));
			pipeline.add(filterMatchObject);
		}

		// $Sort
		BasicDBObject sortFields = new BasicDBObject();
		if (!getListReq.getOrderBy().isEmpty()) {
			for (OrderBy orderBy : getListReq.getOrderBy()) {
				if (orderBy.getOrder().equalsIgnoreCase("ascending")) {
					sortFields.put("domain.roleList.role." + orderBy.getKey(), 1);
				} else {
					sortFields.put("domain.roleList.role." + orderBy.getKey(), -1);
				}

			}
			BasicDBObject sortObject = new BasicDBObject(GlobalStrings.OPERATOR_SORT, sortFields);
			pipeline.add(sortObject);
		}

		// $group
		BasicDBObject groupFields = new BasicDBObject("_id", null).append("role",
				new BasicDBObject(GlobalStrings.OPERATOR_PUSH, "$" + "domain.roleList.role"));
		BasicDBObject groupObj = new BasicDBObject("$group", groupFields);
		pipeline.add(groupObj);

		return pipeline;
	}

}
