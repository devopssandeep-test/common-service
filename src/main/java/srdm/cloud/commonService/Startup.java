package srdm.cloud.commonService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mongodb.MongoException;

import srdm.cloud.commonService.domain.model.LogItem;
import srdm.cloud.commonService.domain.model.SystemManagementLog;
import srdm.cloud.commonService.domain.repository.log.SystemManagementLogRepository;
import srdm.cloud.commonService.util.Config;
import srdm.cloud.commonService.util.DiskUtil;
import srdm.cloud.commonService.util.optimization.DBOptConst;
import srdm.cloud.shared.system.MaintenanceStatus;
import srdm.common.constant.SrdmLogConstants;
import srdm.common.exception.SrdmDataAccessException;

@Component
public class Startup implements InitializingBean, DisposableBean {
	private static Logger log = Logger.getLogger(Startup.class);

	private static final String LogErrFilePrefix = "[SystemErrorFile] ";
	private static final String SrdmConfErrorFile = System.getProperties().getProperty("catalina.home") + "/srdmConf/DiskFullError";
	private static final String DatabaseOptimization = "Database Optimization: ";
	private static final String DatabaseOptimizationError = "Database Optimization Error: ";
	private static final String DatabaseOptimizationRecoveryError = "Database Optimization Recovery Error: ";
	private static final String CHARSET = "UTF-8";

	@Autowired
	SystemManagementLogRepository systemManagementLogRepository;

	/**
	 * 初期処理
	 *
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("*** SRDM Service: START ***");
		writeSystemManagementLog(SrdmLogConstants.SYSMGT_OPERATION_SERVICE_START);
		checkErrorFile();
	}

	/**
	 * 終了処理
	 *
	 */
	@Override
	public void destroy() {
		log.info("*** SRDM Service: STOP ***");
		writeSystemManagementLog(SrdmLogConstants.SYSMGT_OPERATION_SERVICE_STOP);
	}

	/**
	 * システム管理ログへ登録
	 *
	 */
	private void writeSystemManagementLog(final String operation) {
		writeSystemManagementLog(operation, null);
	}
	private void writeSystemManagementLog(final String operation, final String detail) {
		try {
			SystemManagementLog sysLog = new SystemManagementLog();
			List<LogItem> listLogItem = new ArrayList<LogItem>();

			sysLog.setOperation(operation);
			if (SrdmLogConstants.SYSMGT_OPERATION_SERVICE_START.equals(operation) == true
			 || SrdmLogConstants.SYSMGT_OPERATION_SERVICE_STOP.equals(operation) == true) {
				sysLog.setKind(SrdmLogConstants.SYSMGT_KIND_INFO);
				sysLog.setCode("0000"); // 正常
				DiskUtil du = new DiskUtil(new Config().getDatabaseDataDir(), log);
				long size = du.getAvailableSize();
				listLogItem.add(new LogItem(SrdmLogConstants.SYSMGT_ITEM_NAME_DISK_SIZE, size));
			} else if (SrdmLogConstants.SYSMGT_OPERATION_DB_OPTIMIZE.equals(operation) == true) {
				if (DBOptConst.RecoveryError.equals(detail) == true) {
					sysLog.setKind(SrdmLogConstants.SYSMGT_KIND_ERROR);
					sysLog.setCode(DBOptConst.getLogCode(DBOptConst.Fatal)); // 異常
					listLogItem.add(new LogItem(SrdmLogConstants.SYSMGT_ITEM_NAME_RESULT, Integer.toString(DBOptConst.getErrorCode(detail))));
				} else {
					sysLog.setKind(SrdmLogConstants.SYSMGT_KIND_ERROR);
					sysLog.setCode(DBOptConst.getLogCode(DBOptConst.Failure)); // 失敗
					listLogItem.add(new LogItem(SrdmLogConstants.SYSMGT_ITEM_NAME_RESULT, Integer.toString(DBOptConst.getErrorCode(detail))));
				}
			} else {
				sysLog.setCode("9999"); // unsupported
				log.fatal("writeSystemManagementLog: unsupported operation=" + operation);
			}
			sysLog.setItemList(listLogItem);

			try {
				systemManagementLogRepository.add(sysLog);
			} catch (SrdmDataAccessException e) {
				log.fatal("writeSystemManagementLog: " + operation + ": " + e);
				
			}
		} catch (MongoException e) {
			log.info("writeSystemManagementLog: " + operation + ": " + e);
//			for (StackTraceElement ste: e.getStackTrace()) {
//				log.fatal(ste);
//			}
		}
	}

	/**
	 * エラーファイルのチェック
	 */
	private void checkErrorFile() {
		boolean bRunning = false;
		boolean bRecovered = false;
		boolean bRecoveryError = false;

		// ファイルチェック
		File f = new File(SrdmConfErrorFile);
		final boolean bFileExists = f.exists();
		if (bFileExists == true) {
			if (f.length() > 0) {
				// ディスクフル以外のエラーは内容を確認する
				try (
					FileInputStream fis = new FileInputStream(f);
					InputStreamReader isr = new InputStreamReader(fis, CHARSET);
					BufferedReader br = new BufferedReader(isr)
				) {
					String buf;
					while ((buf = br.readLine()) != null) { // 最初の有効な行を採用
						if (buf.contains(DatabaseOptimization) == true) {
							// DB最適化実行中
							log.error(LogErrFilePrefix + "RUNING: " + buf);
							bRunning = true;
							break;
						} else if (buf.contains(DatabaseOptimizationError) == true) {
							// DB最適化失敗(復旧成功)
							log.error(LogErrFilePrefix + "ERROR (RECOVERED): " + buf);
							bRecovered = true;
							break;
						} else if (buf.contains(DatabaseOptimizationRecoveryError) == true) {
							// DB最適化失敗(復旧失敗)
							log.fatal(LogErrFilePrefix + "FATAL (RECOVERY FAILED): " + buf);
							bRecoveryError = true;
							break;
						} else {
							log.fatal(LogErrFilePrefix + "UNKNOWN: " + buf);
						}
					}
					br.close();
					isr.close();
					fis.close();
				} catch (IOException e) {
					log.error(LogErrFilePrefix + "File Read Error: " + f.getName() + " Exception: " + e);
				}
			} else {
				// 0バイトの場合はディスクフルエラー
				log.error(LogErrFilePrefix + "DiskFull Detected.");
			}
		}

		// DBチェック
		
		final boolean bDatabase = true;//checkDatabase();
		if (bDatabase == false) {
			log.fatal(LogErrFilePrefix + "FATAL: Invalid Database Detected.");
			writeSystemManagementLog(SrdmLogConstants.SYSMGT_OPERATION_DB_OPTIMIZE, DBOptConst.RecoveryError);
			MaintenanceStatus.setFatalError(); // APIアクセス拒否
		} else if (bRecoveryError == true) {
			// 終了時点では復旧失敗→起動時には復旧済み
			writeSystemManagementLog(SrdmLogConstants.SYSMGT_OPERATION_DB_OPTIMIZE, DBOptConst.Recovered);
		} else if (bRecovered == true) {
			// 終了時点で復旧成功＝システム管理ログに登録済みのはず→ここでは登録しない
		} else if (bRunning == true) {
			// プログラムが中断したがDBは正常
			writeSystemManagementLog(SrdmLogConstants.SYSMGT_OPERATION_DB_OPTIMIZE, DBOptConst.ExecutionError);
		}

		// ファイル削除
		if (bFileExists == true) { // ファイルが存在していて
			if (bDatabase == true || (bDatabase == false && bRecoveryError == false)) { // 全DBがOK、または、NGのDBがあり復旧失敗ではない場合
				if (f.delete() == true) {
					log.info(LogErrFilePrefix + "File Deleteted: " + f.getName());
				} else {
					log.error(LogErrFilePrefix + "File Deletion Error: " + f.getName());
				}
			}
		}

		// ファイル作成
		if (bDatabase == false && bRecoveryError == false) { // NGのDBがある場合はファイル作成。ただし、元々復旧失敗の場合は除く。
			writeToDiskFullErrorFile(f, DatabaseOptimizationRecoveryError + new Date().toString() + " [Startup]");
		}

	}

	/**
	 * 全てのDBについて、オープンできるかチェックする。<br/>
	 * オープンが失敗した場合は、DBが破損している可能性がある。
	 *
	 * @return true=全てオープン成功、false=オープンに失敗したDBがある
	 */
/*	private boolean checkDatabase() {

		boolean connectionExist = false;
		DBRequest dbRequest = new DBRequest();
		if (null != dbRequest) {
			connectionExist = true;
		}
		return connectionExist;

	}*/

	/**
	 * エラーファイルにメッセージを書き込む
	 *
	 * @param f ファイル
	 * @param msg メッセージ
	 */
	private void writeToDiskFullErrorFile(final File f, final String msg) {
		// ファイル作成
		try {
			if (f.createNewFile() == false) {
				log.error(LogErrFilePrefix + "File Already Exists (Overwrite): " + f.getName());
			}
		} catch (IOException e) {
			log.error(LogErrFilePrefix + "File Creation Error: " + f.getName() + " Exception: " + e);
			return;
		}

		// ファイル書込
		try (
			FileOutputStream fos = new FileOutputStream(f);
			OutputStreamWriter osw = new OutputStreamWriter(fos, CHARSET);
			BufferedWriter bw = new BufferedWriter(osw)
		) {
			bw.append(msg);
			bw.newLine();
			bw.flush();
			bw.close();
			osw.flush();
			osw.close();
			fos.flush();
			fos.close();
		} catch (IOException e) {
			log.error(LogErrFilePrefix + "File Output Error: " + f.getName() + " Exception: " + e);
		}
	}

}
