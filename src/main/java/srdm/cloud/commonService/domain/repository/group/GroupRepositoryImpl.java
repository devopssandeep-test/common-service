package srdm.cloud.commonService.domain.repository.group;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.MongoQueryException;
import com.srdm.mongodb.constants.GlobalStrings;
import com.srdm.mongodb.service.IDBRequest;
import com.srdm.mongodb.serviceImpl.DBRequest;

import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.SimpleGroup;
import srdm.cloud.commonService.util.OxmProcessor;
import srdm.common.constant.SrdmConstants;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

@Repository
public class GroupRepositoryImpl implements GroupRepository {

	String DB_GROUP_ID_PATH = "group.groupId";
	String DB_PARENT_GROUP_ID_PATH = "group.parentGroupId";
	public static final String Master_Device_Group_ID = "1";
	private static final Logger logger = LoggerFactory.getLogger(GroupRepositoryImpl.class);

	@Autowired
	OxmProcessor oxmProcessor;

	@Override
	public List<SimpleGroup> findAllByParentGroupIdWithPagable(GetListReq getListReq) throws SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbSourceObj = new BasicDBObject();
		dbSourceObj.append("_id", 0).append("group", 1).append("group.groupId", 1).append("group.groupName", 1)
				.append("group.parentGroupId", 1);

		// Conditions
		BasicDBObject parentGroupIdObj = new BasicDBObject("group.parentGroupId",
				getListReq.getKeyMap().get("groupId"));
		BasicDBObject groupTypeObj = new BasicDBObject("group.groupType", "normal");
		BasicDBObject groupAttributeObj = new BasicDBObject("group.attribute", "managed");
		BasicDBObject childgroupcondition = new BasicDBObject("group.groupId",
				new BasicDBObject(GlobalStrings.OPERATOR_NOT_EQUAL, Master_Device_Group_ID));


		BasicDBList conditionListObj = new BasicDBList();
		conditionListObj.add(parentGroupIdObj);
		conditionListObj.add(groupTypeObj);
		conditionListObj.add(groupAttributeObj);
		conditionListObj.add(childgroupcondition);
		BasicDBObject whereConditionObject = new BasicDBObject(GlobalStrings.OPERATOR_AND, conditionListObj);

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

		List<SimpleGroup> list = null;
		try {

			// DBからデータ取得
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.IPAU_GROUP_COLLECTION, (int) startIndex,
					(int) endIndex, whereConditionObject, dbSourceObj, new BasicDBObject());
			list = new ArrayList<SimpleGroup>();
			if (listResult.size() != 0) {
				ObjectMapper mapper = new ObjectMapper();
				JSONArray arr = new JSONArray(listResult.toString());
				for (int i = 0; i < arr.length(); i++) {
				Object arrStr = arr.get(i);
				JSONObject jsonObj = new JSONObject(arrStr.toString());
				String groupStr = jsonObj.get("group").toString();
				SimpleGroup group = mapper.readValue(groupStr, SimpleGroup.class);
				list.add(group);
				}
			} else {
				return list;
			}

		} catch (MongoQueryException me) {
			int errNum = me.getErrorCode();
			String errorMsg = me.getMessage();
			logger.error("[findAllByParentGroupIdWithPagable] DB access error.", errorMsg);
			if (errNum == 96) {
				throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
			} else {
				throw new SrdmDataAccessException(errorMsg.toString(), me);
			}
		} catch (MongoException e) {
			logger.error("[findAllByParentGroupIdWithPagable] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("[findAllByParentGroupIdWithPagable] DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		return list;
	}

	@Override
	public long count(String groupId) throws SrdmDataNotFoundException, SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		// Conditions
		BasicDBObject parentGroupIdObj = new BasicDBObject("group.parentGroupId", groupId);
		BasicDBObject groupTypeObj = new BasicDBObject("group.groupType", "normal");
		BasicDBObject groupAttributeObj = new BasicDBObject("group.attribute", "managed");

		BasicDBList conditionListObj = new BasicDBList();
		conditionListObj.add(parentGroupIdObj);
		conditionListObj.add(groupTypeObj);
		conditionListObj.add(groupAttributeObj);
		BasicDBObject whereConditionObject = new BasicDBObject(GlobalStrings.OPERATOR_AND, conditionListObj);

		long count = 0;
		try {

			// DBからデータ取得
			count = dbRequest.getTotalCount(GlobalStrings.IPAU_GROUP_COLLECTION, whereConditionObject);
			if (count <= 0) {
				// データが無い場合
				logger.warn("group List not found. parentGroupId[{}]", groupId);
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
			logger.error("DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		} catch (Exception e) {
			logger.error("DB access error.", e);
			throw new SrdmDataAccessException("DB access error.", e);
		}

		return count;
	}

	@Override
	public SimpleGroup findOne(String groupId) throws SrdmDataNotFoundException, SrdmDataAccessException {

		// DB object creation
		IDBRequest dbRequest = new DBRequest();

		// Form DB request query
		BasicDBObject dbSourceObj = new BasicDBObject();
		dbSourceObj.append("_id", 0).append("group", 1).append("group.groupId", 1).append("group.groupName", 1)
				.append("group.parentGroupId", 1);

		// Conditions
		BasicDBObject groupIdObj = new BasicDBObject("group.groupId", groupId);
		BasicDBObject groupTypeObj = new BasicDBObject("group.groupType", "normal");
		BasicDBObject groupAttributeObj = new BasicDBObject("group.attribute", "managed");

		BasicDBList conditionListObj = new BasicDBList();
		conditionListObj.add(groupIdObj);
		conditionListObj.add(groupTypeObj);
		conditionListObj.add(groupAttributeObj);
		BasicDBObject whereConditionObject = new BasicDBObject(GlobalStrings.OPERATOR_AND, conditionListObj);

		SimpleGroup group = new SimpleGroup();
		try {

			// DBからデータ取得
			List<String> listResult = dbRequest.readFromDB(GlobalStrings.IPAU_GROUP_COLLECTION, 0, 0,
					whereConditionObject, dbSourceObj, new BasicDBObject());

			if (listResult.size() != 0) {
				ObjectMapper mapper = new ObjectMapper();
				JSONArray arr = new JSONArray(listResult.toString());
				Object arrStr = arr.get(0);
				JSONObject jsonObj = new JSONObject(arrStr.toString());
				String groupStr = jsonObj.get("group").toString();
				group = mapper.readValue(groupStr, SimpleGroup.class);
			} else{
				group.setGroupId("");
				group.setParentGroupId("");
				group.setGroupName("");
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

		return group;
	}

	@Override
	public boolean isUnderGroup(String srcGroupId, String targetGroupId) throws SrdmDataAccessException {

		boolean bRet = false;

		if (srcGroupId.equals(targetGroupId)) {
			bRet = true;
		} else {
			// DB object creation
			IDBRequest dbRequest = new DBRequest();

			// Form DB request query
			BasicDBObject dbOutputObj = new BasicDBObject();
			dbOutputObj.append("_id", 0).append(DB_GROUP_ID_PATH, 1);

			// Conditions
			BasicDBObject srcGroupIdObj = new BasicDBObject("group.upperGroupList.e",
					new BasicDBObject(GlobalStrings.OPERATOR_ELEMMATCH, new BasicDBObject("groupId", srcGroupId)));
			BasicDBObject groupId = new BasicDBObject(DB_GROUP_ID_PATH, targetGroupId);

			BasicDBList conditionListObj = new BasicDBList();
			conditionListObj.add(groupId);
			conditionListObj.add(srcGroupIdObj);

			BasicDBObject whereCondition = new BasicDBObject(GlobalStrings.OPERATOR_AND, conditionListObj);

			try {

				// DBからデータ取得
				List<String> listResult = dbRequest.readFromDB(GlobalStrings.IPAU_GROUP_COLLECTION, 0, 0,
						whereCondition, dbOutputObj, new BasicDBObject());

				if (listResult.size() != 0) {

					 bRet = true;
				}
			} catch (MongoQueryException me) {
				int errNum = me.getErrorCode();
				String errorMsg = me.getMessage();
				logger.error("[isUnderGroup] DB access error.", errorMsg);
				if (errNum == 96) {
					throw new SrdmDataAccessException(SrdmConstants.ERROR_E0096, SrdmConstants.ERROR_MESSAGE_E0096);
				} else {
					throw new SrdmDataAccessException(errorMsg.toString(), me);
				}
			} catch (MongoException e) {
				logger.error("[isUnderGroup] DB access error.", e);
				throw new SrdmDataAccessException("DB access error.", e);
			} catch (Exception e) {
				logger.error("[isUnderGroup] DB access error.", e);
				throw new SrdmDataAccessException("DB access error.", e);
			}
		}

		return bRet;
	}


}
