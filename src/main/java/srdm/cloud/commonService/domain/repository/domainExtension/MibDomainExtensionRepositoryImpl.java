package srdm.cloud.commonService.domain.repository.domainExtension;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.MongoQueryException;
import com.mongodb.util.JSON;
import com.srdm.mongodb.constants.GlobalStrings;
import com.srdm.mongodb.service.IDBRequest;
import com.srdm.mongodb.serviceImpl.DBPropertyEntity;
import com.srdm.mongodb.serviceImpl.DBQuery;
import com.srdm.mongodb.serviceImpl.DBRequest;

import srdm.cloud.commonService.domain.model.MibDomainExtension;
import srdm.cloud.commonService.domain.model.MibDomainExtensionInitial;
import srdm.cloud.commonService.util.OxmProcessor;
import srdm.common.constant.SrdmConstants;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

@Repository
public class MibDomainExtensionRepositoryImpl implements MibDomainExtensionRepository {

	private static final Logger logger = LoggerFactory.getLogger(MibDomainExtensionRepositoryImpl.class);

	@Autowired
	OxmProcessor oxmProcessor;

	@Override
	public MibDomainExtension findOne(String domainId) throws SrdmDataNotFoundException, SrdmDataAccessException {
		MibDomainExtension domainExtension = new MibDomainExtension();
		try {
			List<String> domainIdList = DBQuery.fetch("getGroupId")
					.where(DBPropertyEntity.withName("domainId").isEqualTo(domainId)).skip(0).limit(0).getAsList();

			if (domainIdList.size() != 0) {
				for (String str : domainIdList) {
					JSONObject jsonObject = new JSONObject(str);
					String groupId = jsonObject.getJSONObject("domainExtension").getString("groupId");
					String managed = "managed";
					List<String> groupNameList = DBQuery.fetch("groupName")
							.where(DBPropertyEntity.withName("g_groupId").isEqualTo(groupId)
									.and(DBPropertyEntity.withName("g_groupType").isEqualTo("normal"))
									.and(DBPropertyEntity.withName("g_groupAttribute").isEqualTo(managed)))
							.skip(0).limit(0).getAsList();
					if (groupNameList.size() != 0) {
						for (String groupName : groupNameList) {
							JSONObject jsonObj = new JSONObject(groupName);
							domainExtension.setDomainId(domainId);
							domainExtension.setGroupId(groupId);
							domainExtension.setGroupName(jsonObj.getJSONObject("group").getString("groupName"));
							return domainExtension;
						}
					}
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
		} catch (MongoException e) {
			logger.error("[findOne] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (SrdmDataNotFoundException e) {
			throw new SrdmDataNotFoundException("Domain Not found!!.");
		} catch (Exception e) {
			logger.error("[findOne] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		return domainExtension;
	}

	@Override
	public void add(String domainId, String groupId) throws SrdmDataAccessException {

		MibDomainExtensionInitial domainExtension = new MibDomainExtensionInitial();
		domainExtension.setDomainId(domainId);
		domainExtension.setGroupId(groupId);
		try {
			IDBRequest dbRequest = new DBRequest();
			ObjectMapper mapper = new ObjectMapper();
			BasicDBObject inserDataObj = (BasicDBObject) JSON.parse(mapper.writeValueAsString(domainExtension));
			BasicDBObject insertDomainInfo = new BasicDBObject("domainExtension", inserDataObj);
			Map<String, BasicDBObject> insertDataMap = new LinkedHashMap<>();
			insertDataMap.put(GlobalStrings.IPAU_DOMAIN_EXTENSION_COLLECTION, insertDomainInfo);
			dbRequest.insertIntoDB(insertDataMap);

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

	@Override
	public void update(String domainId, String targetGroupId) throws SrdmDataAccessException {

		// Query生成

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Conditions
		BasicDBObject domainIdObj = new BasicDBObject("domainExtension.domainId", domainId);

		// update value
		BasicDBObject updateFields = new BasicDBObject("domainExtension.groupId", targetGroupId);

		BasicDBObject setQueryObj = new BasicDBObject();
		setQueryObj.append(GlobalStrings.OPERATOR_SET, updateFields);

		Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
		collectionsMap.put(GlobalStrings.IPAU_DOMAIN_EXTENSION_COLLECTION, setQueryObj);

		try {

			// DBからデータ取得
			dbRequest.updateDB(domainIdObj, collectionsMap, false, false);

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

	@Override
	public void delete(List<String> domainIdList) throws SrdmDataAccessException {

		for (String domainId : domainIdList) {

			IDBRequest dbRequest = new DBRequest();

			BasicDBObject domainIdObj = new BasicDBObject("domainExtension.domainId", domainId);

			Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
			collectionsMap.put(GlobalStrings.IPAU_DOMAIN_EXTENSION_COLLECTION, domainIdObj);

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

}
