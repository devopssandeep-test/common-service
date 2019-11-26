package srdm.cloud.commonService.util.optimization;

import com.mongodb.BasicDBObject;
import com.srdm.mongodb.constants.GlobalStrings;
import com.srdm.mongodb.serviceImpl.DBRequest;

public class DBCheckUtil {

	/**
	 * DBに書き込めるか？(オープンできるか？)
	 *
	 * @param dbName データベース名
	 * @return true=可、false=不可
	 */
	public boolean canWrite(final String dbName, final String dbNode ) {
		return canWrite(dbName, dbNode, false);
	}

	/**
	 * DBに書き込めるか？(オープンできるか？)
	 *
	 * @param dbName データベース名
	 * @param bAdmin 管理者権限
	 * @return
	 */
	public boolean canWrite(final String dbName, final String dbNode ,final boolean bAdmin) {
		boolean connectionExist = false;
		DBRequest dbRequest = new DBRequest();
		 BasicDBObject serachFields=new BasicDBObject("dbNode",new BasicDBObject(GlobalStrings.OPERATOR_EXISTS,true));
		 BasicDBObject 	projectionFields = new BasicDBObject();
		 BasicDBObject 	sortFields = new BasicDBObject();
		dbRequest.readFromDB(dbName, 0, 0, serachFields, projectionFields, sortFields);
		if (null != dbRequest) {
			connectionExist = true;
		}
		return connectionExist;
	}
}

