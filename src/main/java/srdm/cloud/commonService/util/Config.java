package srdm.cloud.commonService.util;

import org.springframework.stereotype.Component;

@Component
public class Config {
	private static final String baseDir = System.getProperties().getProperty("catalina.home");
	private static final String dataDir = baseDir + "/../DBService/data";
	private static final String mongoLogDir = baseDir + "/../DBService/data/log";

	private static final String tomcatLogsDir = baseDir + "/logs";
	private static final String appLogsDir = baseDir + "/var/logs";
	private static final String exportFileDataDir = baseDir + "/webapps/commonService/WEB-INF/export/";
	private static final String errorFile = baseDir + "/srdmConf/DiskFullError";

	/**
	 * App Log Dir (末尾に"/"なし)
	 * @return
	 */
	public String getAppLogsDir() {
		return appLogsDir;
	}

	/**
	 * Tomcat Logs Dir (末尾に"/"なし)
	 * @return
	 */
	public String getTomcatLogsDir() {
		return tomcatLogsDir;
	}

	/**
	 * BaseX Data Dir (末尾に"/"なし)
	 * @return
	 */
	public String getDatabaseDataDir() {
		return dataDir;
	}

	/**
	 * MongoDB Log フォルダ
	 * @return
	 */
	public String getMongoLog() {
		return mongoLogDir;
	}


	/**
	 * Export File work Dir (末尾に"/"あり)
	 * @return
	 */
	public String getExportFileDataDir(){
		return exportFileDataDir;
	}

	/**
	 * DiskFullErrorファイル
	 * @return
	 */
	public String getAppSystemErrorFile() {
		return errorFile;
	}
}
