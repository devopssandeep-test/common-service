package srdm.cloud.commonService.domain.service.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import srdm.cloud.commonService.domain.repository.session.SrdmSessionRepository;
import srdm.cloud.commonService.util.AppLogDownloadInfo;
import srdm.common.constant.SrdmConstants;
import srdm.common.exception.SrdmDataNotFoundException;
import srdm.common.exception.SrdmGeneralException;

@Service
public class AppLogServiceImpl implements AppLogService {

	private static final Logger logger = LoggerFactory.getLogger(AppLogServiceImpl.class);

	@Autowired
	AppLogDownloadInfo appLogDownloadInfo;

	@Autowired
	ResourcePatternResolver resourcePatternResolver;

	@Autowired
	SrdmSessionRepository srdmSessionRepository;

	/**
	 * システムログ（AppLog）ダウンロードリクエスト登録
	 */
	@Override
	public long makeDownloadLogFile(String sessionId, String logType)
			throws SrdmDataNotFoundException, SrdmGeneralException {

		// "logTypeごとの権限のチェック"
		if(SrdmConstants.APPLOG_TYPE.containsKey(logType) == false) {
			logger.error("LogType not found. logType:[{}]", logType);
			throw new SrdmDataNotFoundException("LogType not found.");
		}

		List<String> permList = new ArrayList<String>();
		if(SrdmConstants.MIB_LOG.equals(logType)) {
			permList.add(SrdmConstants.PERM_NAME_GROUP);
		} else if(SrdmConstants.TCO_LOG.equals(logType)) {
			permList.add(SrdmConstants.PERM_NAME_GROUP);
		} else if(SrdmConstants.FSS_LOG.equals(logType)) {
			permList.add(SrdmConstants.PERM_NAME_FIELDSUPPORTSYSTEM);
			permList.add(SrdmConstants.PERM_NAME_FIRMWAREUPDATE);
		} else if(SrdmConstants.FD_LOG.equals(logType)) {
			permList.add(SrdmConstants.PERM_NAME_FILEDISTRIBUTION);
		}

		boolean hasPerm = false;
		if(permList.isEmpty() == false) {
			for(String perm : permList) {
				hasPerm = srdmSessionRepository.hasPermission(sessionId, perm);
				if(hasPerm == true) {
					break;
				}
			}
			if(hasPerm == false) {
				logger.warn("You do not have sufficient privileges.permission[{}]", permList);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0024, SrdmConstants.ERROR_MESSAGE_E0024);
			}
		}

		// 対象のリソースを抽出
		long requestId = appLogDownloadInfo.add(logType, getResourceList(logType));
		logger.info("requestId:[{}]",requestId);

		return requestId;
	}

	/**
	 * ファイル取得処理
	 *
	 * @param logType ログタイプ
	 * @return リソースリスト
	 * @throws SrdmGeneralException
	 * @throws SrdmDataNotFoundException
	 */
	protected List<Resource> getResourceList(String logType) throws SrdmDataNotFoundException, SrdmGeneralException {
		logger.info("logType:" + logType);
		List<Resource> resourceList = new ArrayList<Resource>();

		// targetDirを設定
		String targetDir;
		String catalinaHome = System.getProperties().getProperty("catalina.home");

		if(SrdmConstants.TOMCAT_LOG.equals(logType)) {
			targetDir = catalinaHome + "/logs/";
		} else {
			targetDir = catalinaHome + "/var/logs/";
		}

		getResourceGroup(SrdmConstants.APPLOG_TYPE.get(logType), targetDir, resourceList);

		if (SrdmConstants.COMMON_LOG.equals(logType)) {
			// コントロールパネルログ
			getResourceGroup(SrdmConstants.APPLOG_TYPE.get(SrdmConstants.COMMON_CONTROLPANEL_LOG)
					, catalinaHome + "/../ControlPanel/log/", resourceList);

			// バージョンアップツール
			getResourceGroup(SrdmConstants.APPLOG_TYPE.get(SrdmConstants.COMMON_VERTION_UP_LOG)
					, catalinaHome + "/../DBUtility/Versionup/log/", resourceList);

			// バックアップツール
			getResourceGroup(SrdmConstants.APPLOG_TYPE.get(SrdmConstants.COMMON_BACKUP_LOG_LOG)
					, catalinaHome + "/../DBUtility/DBBackup/log/", resourceList);

			// MongoDB
			getResourceGroup(SrdmConstants.APPLOG_TYPE.get(SrdmConstants.COMMON_MONGODB_LOG_LOG)
					, catalinaHome + "/../DBService/data/log/", resourceList);
		}

		if(resourceList.isEmpty() == true) {
			logger.warn("log file not found. logType[{}]", logType);
			throw new SrdmDataNotFoundException("log file not found.");
		}

		return resourceList;
	}

	/**
	 * ファイル取得処理（グループ単位）
	 *
	 * @param fileList ファイルリスト
	 * @param targetDir ファイルディレクトリ
	 * @param resourceList 格納リソースリスト
	 * @throws SrdmGeneralException
	 */
	private void getResourceGroup(List<String> fileList, String targetDir, List<Resource> resourceList)
			throws SrdmGeneralException {
		logger.debug("getResourceGroup start");
		for (String file : fileList) {
			Resource resource[];
			try {
				logger.debug("file://" + targetDir + file);
				resource = resourcePatternResolver.getResources("file://" + targetDir + file);
			} catch (IOException e) {
				logger.error("Log file download error.", e);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0032, SrdmConstants.ERROR_MESSAGE_E0032, e);
			}
			for (int i = 0; i < resource.length; i++) {
				resourceList.add(resource[i]);
			}
		}
		logger.debug("getResourceGroup end");
	}

	/**
	 * ダウンロードファイル名取得
	 */
	@Override
	public String getDownloadFileName(String sessionId, Long requestId) throws SrdmDataNotFoundException {

		String logType = appLogDownloadInfo.getLogType(requestId);
		if(logType.isEmpty() == true) {
			logger.error("requestId not found. requestId:[{}]", requestId);
			throw new SrdmDataNotFoundException("requestId not found.");
		}

		String fileName = logType + "-"
				+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis())) + ".zip";
		return fileName;
	}

	/**
	 * システムログ（AppLog）ダウンロード
	 */
	@Override
	public void downloadAppLog(String sessionId, Long requestId, OutputStream os)
			throws SrdmGeneralException, SrdmDataNotFoundException {

		List<Resource> resourceList = appLogDownloadInfo.getResourceList(requestId);
		appLogDownloadInfo.remove(requestId);
		if(resourceList.isEmpty() == true) {
			logger.error("requestId not found. requestId:[{}]", requestId);
			throw new SrdmDataNotFoundException("requestId not found.");
		}

		try (ZipOutputStream zipOutputStream = new ZipOutputStream(os)) {

			for(Resource res : resourceList) {
				File file = res.getFile();
				Path filePath = Paths.get(file.toURI()).getFileName();
				if( filePath != null) {
					String fileName = filePath.toString();
					logger.info("log filename[{}]", fileName);
					try (InputStream input = new FileInputStream(file)) {
						zipOutputStream.putNextEntry(new ZipEntry(fileName));
						StreamUtils.copy(input, zipOutputStream);
					}
				} else {
					logger.warn("file not include. URI[{}]",file.toURI().toString());
				}
			}
		} catch (IOException e) {
			logger.error("Log file download error.", e);
			throw new SrdmGeneralException(SrdmConstants.ERROR_E0032, SrdmConstants.ERROR_MESSAGE_E0032, e);
		}
	}

}
