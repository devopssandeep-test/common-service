package srdm.cloud.commonService.asynchronous;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.srdm.mongodb.service.IDBRequest;
import com.srdm.mongodb.serviceImpl.DBBackup;
import com.srdm.mongodb.serviceImpl.DBRequest;

import srdm.cloud.commonService.domain.model.LogItem;
import srdm.cloud.commonService.domain.model.SystemManagementLog;
import srdm.cloud.commonService.domain.repository.log.SystemManagementLogRepository;
import srdm.cloud.commonService.util.Config;
import srdm.cloud.commonService.util.DiskUtil;
import srdm.cloud.commonService.util.file.MongoLogCtl;
import srdm.cloud.commonService.util.optimization.DBOptConst;
import srdm.cloud.commonService.util.optimization.DBOptResultInfo;
import srdm.cloud.commonService.util.optimization.FssOptimization;
import srdm.cloud.commonService.util.optimization.MailUtil;
import srdm.cloud.commonService.util.optimization.TcoOptimization;
import srdm.cloud.shared.system.MaintenanceStatus;
import srdm.common.constant.SrdmLogConstants;
import srdm.common.exception.SrdmDataAccessException;

@Component
public class DbOptimization {
	private final Logger logSimple = Logger.getLogger(DBOptConst.LogCategorySimple); // 実行ログ
	private final Logger logDetail = Logger.getLogger(DBOptConst.LogCategoryDetail); // 詳細ログ

	private static final String DatabaseOptimization = "Database Optimization: ";
	private static final String DatabaseOptimizationError = "Database Optimization Error: ";
	private static final String DatabaseOptimizationRecoveryError = "Database Optimization Recovery Error: ";

	/** メール通知 */
	private MailUtil mMailUtil = null;

	/** 結果 */
	private DBOptResultInfo mResultInfo = new DBOptResultInfo();

	/** FSSの情報 */
	FssOptimization fss = new FssOptimization(logSimple, logDetail);

	@Autowired
	Config config;

	@Autowired
	SystemManagementLogRepository systemManagementLogRepository;

	@Autowired
	MongoLogCtl mongoLogCtl;

	/**
	 * DB最適化処理実行
	 */
	@Async
	public void dbOptimize() {
		writeInfoLog("***** START *****");
		long startTimestamp = System.currentTimeMillis();
		mResultInfo.setStartTimestamp(startTimestamp);
		File f = new File(config.getAppSystemErrorFile());

		// 前処理
		if (initialization(f) == false) {
			writeSystemManagementLog(startTimestamp);
			writeErrorLog("***** ABORT *****");
			return;
		}

		// 最適化処理
		optimization();

		// 後処理
		if (finalization(f) == false) {
			writeSystemManagementLog(startTimestamp);
			writeErrorLog("***** ERROR *****");
			return;
		}

		writeSystemManagementLog(startTimestamp);
		writeInfoLog("***** END *****");

	}

	/**
	 * システム管理ログへ登録
	 *
	 * @param startTimestamp
	 */
	private void writeSystemManagementLog(final long startTimestamp) {
		if (hasFatalError() == true) {
			// 復旧失敗＝DBアクセス不可
			return;
		}

		SystemManagementLog sysLog = new SystemManagementLog();
		sysLog.setOperation(SrdmLogConstants.SYSMGT_OPERATION_DB_OPTIMIZE);

		List<LogItem> listLogItem = new ArrayList<LogItem>();
		listLogItem
				.add(new LogItem(SrdmLogConstants.SYSMGT_ITEM_NAME_TIMESTAMP_START, mResultInfo.getStartTimestamp()));
		listLogItem.add(new LogItem(SrdmLogConstants.SYSMGT_ITEM_NAME_TIMESTAMP_END, mResultInfo.getEndTimestamp()));
		listLogItem.add(
				new LogItem(SrdmLogConstants.SYSMGT_ITEM_NAME_DB_SIZE_BEFORE, mResultInfo.getDbDataSizeBefore()));
		listLogItem.add(
				new LogItem(SrdmLogConstants.SYSMGT_ITEM_NAME_DB_SIZE_AFTER, mResultInfo.getDbDataSizeAfter()));
		listLogItem.add(
				new LogItem(SrdmLogConstants.SYSMGT_ITEM_NAME_DISK_SIZE_BEFORE, mResultInfo.getDiskFreeSizeBefore()));
		listLogItem.add(
				new LogItem(SrdmLogConstants.SYSMGT_ITEM_NAME_DISK_SIZE_AFTER, mResultInfo.getDiskFreeSizeAfter()));

		Map<String, String> errors = mResultInfo.getErrors();
		if (errors.isEmpty() == true) {
			sysLog.setKind(SrdmLogConstants.SYSMGT_KIND_INFO);
			sysLog.setCode(DBOptConst.getLogCode(DBOptConst.Success));
			listLogItem.add(
					new LogItem(SrdmLogConstants.SYSMGT_ITEM_NAME_RESULT, DBOptConst.getLogCode(DBOptConst.Success)));
		} else {
			sysLog.setKind(SrdmLogConstants.SYSMGT_KIND_ERROR);
			sysLog.setCode(DBOptConst.getLogCode(DBOptConst.Failure));
			for (Map.Entry<String, String> err : errors.entrySet()) {
				String item = err.getKey(); // エラー発生箇所
				String code = Integer.toString(DBOptConst.getErrorCode(err.getValue())); // エラー内容をコード化
				String value;
				if (DBOptConst.ListOfInternalSections.contains(item) == true) { // DB以外でエラー
					value = code;
				} else { // DBでエラー
					value = code + "/" + item; // 結果は「コード/DB名」
				}
				listLogItem.add(new LogItem(SrdmLogConstants.SYSMGT_ITEM_NAME_RESULT, value));
			}
		}
		sysLog.setItemList(listLogItem);

		try {
			systemManagementLogRepository.add(sysLog);
		} catch (SrdmDataAccessException e) {
			writeSimpleErrorLog("System Management Log Write Error");
			writeDetailErrorLog("System Management Log Write Error: " + e);
		}
	}

	/**
	 * 実行ログと詳細ログに出力する(INFOレベル)
	 *
	 * @param message
	 */
	private void writeInfoLog(Object message) {
		writeDetailInfoLog(message);
		writeSimpleInfoLog(message);
	}

	/**
	 * 前処理
	 *
	 * @return true=成功、false=失敗
	 */
	private boolean initialization(final File fileDiskFullError) {
		try {
			writeDetailInfoLog("+++ Initialization +++");

			if (MaintenanceStatus.isInMaintenance() == true) {
				// 多重起動チェック(APIレベルでガードされているため基本的にはここに入らないはず)
				writeErrorLog("Already in Maintenance Mode.");
				mResultInfo.addError(DBOptConst.INIT, DBOptConst.InitializationError);
				return false;
			}

			writeDetailInfoLog("Reading Mail Configuration...");
			mMailUtil = new MailUtil(logSimple, logDetail);
			if (mMailUtil.setup() == false) {
				writeErrorLog("Mail Setup Failure.");
				mResultInfo.addError(DBOptConst.INIT, DBOptConst.InitializationError);
				return false;
			}

			mResultInfo.setLogFolder(config.getAppLogsDir());

			// FSSのデバイス情報を共有メモリに保持
			writeDetailInfoLog("Loading FSS DeviceInfo...");
			fss.loadDeviceInfo();

			// メンテナンス開始
			writeDetailInfoLog("Into MaintenanceMode.");
			MaintenanceStatus.setStart();
			mResultInfo.setStartTimestamp(System.currentTimeMillis());
			writeToDiskFullErrorFile(fileDiskFullError, DatabaseOptimization + new Date().toString());

			return true;
		} catch (OutOfMemoryError | Exception e) {
			writeSimpleErrorLog("Initialization Error.");
			writeDetailErrorLog("Initialization Error: " + e);
			for (StackTraceElement ste : e.getStackTrace()) {
				writeDetailFatalLog(" " + ste);
			}
			mResultInfo.addError(DBOptConst.INIT, DBOptConst.InitializationError);
			return false;
		}
	}

	/**
	 * 後処理
	 *
	 * @return true=正常終了(またはリカバリ成功)、false=異常終了(リカバリ失敗、DBオープン失敗等)
	 */
	private boolean finalization(final File fileDiskFullError) {
		try {
			writeDetailInfoLog("+++ Finalization +++");

			// DB更新(ステータス変更)
			writeDetailInfoLog("Checking TCO DB...");
			TcoOptimization tco = new TcoOptimization(logSimple, logDetail);
			if (tco.update() == false) {
				for (String dbName : tco.getErrorDBs()) {
					mResultInfo.addError(dbName, DBOptConst.DatabaseFinalizationError);
				}
			}
			// FSSのDB更新(ステータス変更)
			writeDetailInfoLog("Checking FSS DB...");
			if (fss.update() == false) {
				for (String dbName : fss.getErrorDBs()) {
					mResultInfo.addError(dbName, DBOptConst.DatabaseFinalizationError);
				}
			}

			// ログローテーション
			writeDetailInfoLog("logRotate Start ...");
			if (!new DBRequest().logRotate()
				|| !mongoLogCtl.oldLogFileDelete()) {
				mResultInfo.addError(DBOptConst.FIN, DBOptConst.logRotateError);
			}

			// DB最適化終了→メール送信
			mResultInfo.setEndTimestamp(System.currentTimeMillis());
			if (mMailUtil != null) {
				if (mMailUtil.isEnabled() == true) {
					writeDetailInfoLog("Sending Notification Mail...");
					if (mMailUtil.send(mResultInfo) == false) {
						mResultInfo.addError(DBOptConst.MAIL, DBOptConst.MailSendError);
					}
				}
			}

			// メンテナンスエラー(復旧失敗)
			if (hasFatalError() == true) {
				MaintenanceStatus.setFatalError();
				writeToDiskFullErrorFile(fileDiskFullError, DatabaseOptimizationRecoveryError + new Date().toString());
				return false;
			}

			// FSSのデバイス情報(共有メモリ)をクリア(復旧失敗時は残しておく)
			writeDetailInfoLog("Clearing FSS DeviceInfo...");
			fss.clearDeviceInfo();

			// メンテナンスエラー(復旧成功)
			if (hasError() == true) {
				MaintenanceStatus.setError();
				writeToDiskFullErrorFile(fileDiskFullError, DatabaseOptimizationError + new Date().toString());
				return true;
			}

			// メンテナンス終了
			writeToDiskFullErrorFile(fileDiskFullError, null);
			MaintenanceStatus.clear();
			writeDetailInfoLog("Into ServiceMode.");

			return true;
		} catch (OutOfMemoryError | Exception e) {
			writeSimpleErrorLog("Finalization Error.");
			writeDetailErrorLog("Finalization Error: " + e);
			for (StackTraceElement ste : e.getStackTrace()) {
				writeDetailFatalLog(" " + ste);
			}
			mResultInfo.addError(DBOptConst.FIN, DBOptConst.FinalizationError);
			return false;
		}
	}

	/**
	 * 最適化処理
	 */
	private void optimization() {
		try {
			writeDetailInfoLog("+++ Optimization +++");

			// ディスク使用量取得
			final String dbFolder = config.getDatabaseDataDir();
			DBRequest dbReq = new DBRequest();
			mResultInfo.setDataFolder(dbFolder);
			DiskUtil diskUtil = setupFolderInfo(dbFolder);
			mResultInfo.setDiskFreeSizeBefore(diskUtil.getAvailableSize());
			mResultInfo.setDbDataSizeBefore(dbReq.getBackupFileSize().longValue());
			mResultInfo.clearErrors();

			writeInfoLog("DB Data Used Size(Before): " + mResultInfo.getDbDataSizeBefore());
			writeInfoLog("Available Disk Space(Before) : " + mResultInfo.getDiskFreeSizeBefore());

			// ディスク空き容量チェック
			if (!checkDiskSize(new BigDecimal(mResultInfo.getDbDataSizeBefore())
							, new BigDecimal(mResultInfo.getDiskFreeSizeBefore()))) {
				mResultInfo.addError(DBOptConst.OPT, DBOptConst.DiskSpaceError);
				return;
			}

			// 最適化処理
			if (optimizeAllDB()) {
				mResultInfo.setDiskFreeSizeAfter(diskUtil.getAvailableSize());
				mResultInfo.setDbDataSizeAfter(dbReq.getBackupFileSize().longValue());
				writeInfoLog("DB Data Used Size(After) : " + mResultInfo.getDbDataSizeAfter());
				writeInfoLog("Available Disk Space(After) : " + mResultInfo.getDiskFreeSizeAfter());
			} else {
				mResultInfo.addError(DBOptConst.OPT, DBOptConst.OptimizationError);
			}
		} catch (OutOfMemoryError | Exception e) {
			writeSimpleErrorLog("Optimization Error.");
			writeDetailErrorLog("Optimization Error: " + e);
			for (StackTraceElement ste : e.getStackTrace()) {
				writeDetailFatalLog(" " + ste);
			}
			mResultInfo.addError(DBOptConst.OPT, DBOptConst.OptimizationError);
		}
	}

	private DiskUtil setupFolderInfo(final String targetPath) {
		DiskUtil diskUtil = new DiskUtil(targetPath, logDetail);
		return diskUtil;
	}

	/**
	 * ディスク空き容量チェック
	 *
	 * @param backupFileSize バックアップファイルサイズ（バイト）
	 * @param diskFreeSize ディスク空き容量（バイト）
	 * @return true:チェックOK false:チェックNG
	 */
	public boolean checkDiskSize(BigDecimal backupFileSize, BigDecimal diskFreeSize) {
		NumberFormat nfNum = NumberFormat.getNumberInstance();
		writeInfoLog("backupFileSize = " + nfNum.format(backupFileSize)
					+ " diskFreeSize = " + nfNum.format(diskFreeSize));
		if (diskFreeSize.compareTo(backupFileSize) < 0) {
			writeInfoLog("Disk Size Check Error");
			return false;
		}
		writeInfoLog("Disk Size Check OK");
		return true;
	}

	/**
	 * 最適化処理実行<br/>
	 * (全DBを対象に、最適化処理を呼び出す)
	 *
	 * @param dbOptUtil
	 * @return
	 */
	private boolean optimizeAllDB() {
		boolean bResult = true;
		IDBRequest dbRequest = new DBRequest();

		// MongoDBバックアップ取得
		bResult = DBBackup.mongodump();
		if (bResult) {
			// 最適化処理
			try {
				if (dbRequest.initiateDBOptmization()) {
					writeSimpleInfoLog("common service Optimization successed");
				} else {
					// MongoDB リストア
					DBBackup.mongorestore();
					writeSimpleErrorLog("Optimization Failed.");
					bResult = false;
				}
			} catch (Exception e) {
				// MongoDB リストア
				DBBackup.mongorestore();
				writeSimpleErrorLog("Optimization Exception Failed. " + e.getMessage());
				throw e;
			}
		}

		return bResult;
	}

	/**
	 * メンテナンスエラー(復旧成功)あり？<br/>
	 * {@link #hasFatalError()}を先に呼ぶこと。
	 *
	 * @return
	 */
	private boolean hasError() {
		return hasError(null);
	}

	private boolean hasError(final String dbName) {
		return hasError(DBOptConst.ListOfErrors, dbName);
	}

	/**
	 * メンテナンスエラー(復旧失敗)あり？
	 *
	 * @return
	 */
	private boolean hasFatalError() {
		return hasFatalError(null);
	}

	private boolean hasFatalError(final String dbName) {
		return hasError(DBOptConst.ListOfFatalErrors, dbName);
	}

	private boolean hasError(final List<String> listErrorType, final String dbName) {
		Map<String, String> errors = mResultInfo.getErrors();
		if (errors.isEmpty() == true) {
			return false;
		}
		if (dbName == null) { // 全DBについて、指定のエラーが発生しているか
			for (String error : listErrorType) {
				if (errors.containsValue(error) == true) {
					return true;
				}
			}
		} else { // 指定DBは指定のエラーが発生しているか
			final String value = errors.get(dbName);
			if (errors.containsValue(value) == true) {
				return true;
			}
		}
		return false;
	}

	/**
	 * エラーファイルにメッセージを書き込む<br/>
	 * メッセージがnullの場合はファイルを削除する。
	 *
	 * @param f
	 *            ファイル
	 * @param msg
	 *            メッセージ
	 */
	private void writeToDiskFullErrorFile(final File f, final String msg) {
		// ファイル削除
		if (msg == null) {
			if (f.delete() == false) {
				writeDetailErrorLog("File Deletion Error: " + f.getAbsolutePath());
			}
			return;
		}

		// ファイル作成
		try {
			if (f.createNewFile() == false) {
				logDetail.warn("File Already Exists (Overwrite): " + f.getAbsolutePath());
			}
		} catch (IOException e) {
			writeDetailErrorLog("File Creation Error: " + f.getAbsolutePath() + " Exception: " + e);
			return;
		}

		// ファイル書込
		try (FileOutputStream fos = new FileOutputStream(f);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
				BufferedWriter bw = new BufferedWriter(osw)) {
			bw.append(msg);
			bw.newLine();
			bw.flush();
			osw.flush();
			fos.flush();
			bw.close();
			osw.close();
			fos.close();
		} catch (IOException e) {
			writeDetailErrorLog("File Output Error: " + f.getAbsolutePath() + " Exception: " + e);
		}
	}

	/**
	 * 実行ログと詳細ログに出力する(ERRORレベル)
	 *
	 * @param message
	 */
	private void writeErrorLog(Object message) {
		writeDetailErrorLog(message);
		writeSimpleErrorLog(message);
	}

	private void writeSimpleInfoLog(Object message) {
		if (logSimple != null) {
			logSimple.info(message);
		}
	}

	private void writeSimpleErrorLog(Object message) {
		if (logSimple != null) {
			logSimple.error(message);
		}
	}

	private void writeDetailInfoLog(Object message) {
		if (logDetail != null) {
			logDetail.info(message);
		}
	}

	private void writeDetailErrorLog(Object message) {
		if (logDetail != null) {
			logDetail.error(message);
		}
	}

	private void writeDetailFatalLog(Object message) {
		if (logDetail != null) {
			logDetail.fatal(message);
		}
	}

}
