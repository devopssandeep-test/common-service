package srdm.cloud.commonService.domain.repository.domainExtension;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.MongoQueryException;
import com.srdm.mongodb.constants.GlobalStrings;
import com.srdm.mongodb.service.IDBRequest;
import com.srdm.mongodb.serviceImpl.DBRequest;

import srdm.cloud.commonService.domain.model.TcoDomainExtensionInitial;
import srdm.cloud.commonService.util.OxmProcessor;
import srdm.common.constant.SrdmConstants;
import srdm.common.exception.SrdmDataAccessException;

@Repository
public class TcoDomainExtensionRepositoryImpl implements TcoDomainExtensionRepository {

	private static final Logger logger = LoggerFactory.getLogger(TcoDomainExtensionRepositoryImpl.class);

	@Autowired
	OxmProcessor oxmProcessor;

	@Override
	public void add(String domainId) throws SrdmDataAccessException {

		TcoDomainExtensionInitial domainExtension = new TcoDomainExtensionInitial();
		domainExtension.setDomainId(domainId);

		try {
			IDBRequest dbRequest = new DBRequest();
			BasicDBObject inserDataObj = new BasicDBObject();
			inserDataObj.put("domainId", domainExtension.getDomainId());
			inserDataObj.put("theme", domainExtension.getTheme());

			BasicDBObject inserUnderDomainExtObj = new BasicDBObject("domainExtension", inserDataObj);

			Map<String, BasicDBObject> insertDataMap = new LinkedHashMap<>();
			insertDataMap.put(GlobalStrings.TCO_DOMAIN_EXTENSION_COLLECTION, inserUnderDomainExtObj);
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
			collectionsMap.put(GlobalStrings.TCO_DOMAIN_EXTENSION_COLLECTION, domainIdObj);

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
