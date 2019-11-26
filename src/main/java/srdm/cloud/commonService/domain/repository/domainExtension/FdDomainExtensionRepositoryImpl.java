package srdm.cloud.commonService.domain.repository.domainExtension;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.MongoQueryException;
import com.srdm.mongodb.constants.GlobalStrings;
import com.srdm.mongodb.service.IDBRequest;
import com.srdm.mongodb.serviceImpl.DBRequest;

import srdm.cloud.commonService.domain.model.FdDomainExtensionInitial;
import srdm.cloud.commonService.util.OxmProcessor;
import srdm.common.constant.SrdmConstants;
import srdm.common.exception.SrdmDataAccessException;

@Repository
public class FdDomainExtensionRepositoryImpl implements FdDomainExtensionRepository {

	private static final Logger logger = LoggerFactory.getLogger(FdDomainExtensionRepositoryImpl.class);

	@Autowired
	OxmProcessor oxmProcessor;

	@Override
	public void add(String domainId) throws SrdmDataAccessException {

		FdDomainExtensionInitial domainExtension = new FdDomainExtensionInitial();
		domainExtension.setDomainId(domainId);

		/**
		 * O/X Mapperを使用し、作成するアカウントXML データを生成 createは、Domain
		 * Objectから生成したXMLを挿入するため、 Injectionは、発生しない。なので、O/X Mapperの結果をそのままQueryに
		 * 指定しても問題なし。
		 */
		// O/X Mapping(Object to xml)

		try {
			IDBRequest dbRequest = new DBRequest();
			BasicDBObject inserDataObj = new BasicDBObject();
			inserDataObj.put("domainId", domainExtension.getDomainId());
			inserDataObj.put("theme", domainExtension.getTheme());

			BasicDBObject inserUnderDomainExtObj = new BasicDBObject("domainExtension", inserDataObj);
			Map<String, BasicDBObject> insertDataMap = new LinkedHashMap<>();
			insertDataMap.put(GlobalStrings.FD_DOMAIN_EXTENSION_COLLECTION, inserUnderDomainExtObj);
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
	public void delete(List<String> domainIdList) throws SrdmDataAccessException {

		for (String domainId : domainIdList) {

			IDBRequest dbRequest = new DBRequest();

			BasicDBObject domainIdObj = new BasicDBObject("domainExtension.domainId", domainId);

			Map<String, BasicDBObject> collectionsMap = new HashMap<String, BasicDBObject>();
			collectionsMap.put(GlobalStrings.FD_DOMAIN_EXTENSION_COLLECTION, domainIdObj);

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
			}  catch (MongoException e) {
				logger.error("[delete] DB access error.", e);
				throw new SrdmDataAccessException("DB access error.", e);
			} catch (Exception e) {
				logger.error("[delete] DB access error.", e);
				throw new SrdmDataAccessException("DB access error.", e);
			}
		}

	}

}
