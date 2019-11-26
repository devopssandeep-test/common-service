package srdm.cloud.commonService.util.optimization;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBOptConst {
	public static final String LogCategorySimple = "CommonService.DBOptimization";       // Log4Jカテゴリ名(実行ログ用)
	public static final String LogCategoryDetail = "CommonService.DBOptimizationDetail"; // Log4Jカテゴリ名(詳細ログ用)

	// エラー発生箇所(下記 or BXQueryDB.*)
	public static final String INIT = "init"; // 初期化処理のDBに関係のない部分
	public static final String OPT = "opt"; // 最適化処理のDBに関係のない部分
	public static final String MAIL = "mailAlert"; // メール送信処理
	public static final String FIN = "fin"; // 終了処理のDBに関係のない部分
	public static final List<String> ListOfInternalSections = Arrays.asList(
			INIT, OPT, MAIL, FIN
	);

	// エラー内容
	public static final String Success = "success";
	public static final String Failure = "failure"; // システム管理ログ用
	public static final String Fatal = "fatal"; // システム管理ログ用(復旧失敗)
	public static final String InitializationError = "initializationError"; // システム管理ログ用(初期化失敗: プログラムエラー、設定エラー等)
	public static final String ExecutionError = "executionError"; // システム管理ログ用(実行中にプロセス中断・DBは正常)
	public static final String Recovered = "recovered"; // システム管理ログ用(復旧成功)
	public static final String RecoveryError = "recoveryError"; // システム管理ログ用
	public static final String DiskSpaceError = "diskSpaceError";
	public static final String BackupError = "backupError";
	public static final String DatabaseOpenError = "databaseOpenError";
	public static final String DatabaseLockError = "databaseLockError";
	public static final String OptimizationError = "optimizationError";
	public static final String OptimizationRecoveryError = "optimizationRecoveryError";
	public static final String DatabaseFinalizationError = "databaseFinalizationError";
	public static final String FinalizationError = "finalizationError"; // システム管理ログ用(終了処理失敗: プログラムエラー等)
	public static final String MailSendError = "mailSendError";
	public static final String logRotateError = "logRotateError";

	public static final List<String> ListOfErrors = Arrays.asList(
		DiskSpaceError,
		BackupError,
		DatabaseLockError,
		OptimizationError
	);
	public static final List<String> ListOfFatalErrors = Arrays.asList(
		DatabaseOpenError,
		OptimizationRecoveryError,
		DatabaseFinalizationError
	);

	private static final Map<String, Integer> MapErrorCode = new HashMap<String, Integer>() {
		private static final long serialVersionUID = -5776832332506182350L;
		// エラーコード(SRDM内でユニーク: "01xx"はDB最適化関連のエラー)
		{	put(Success,					  0);	}
		{	put(Failure,					101);	} // SysMgtLog
		{	put(Fatal,						102);	} // SysMgtLog
		// 詳細コード(最適化処理内でユニーク)
		{	put(ExecutionError,				101);	} // SysMgtLog
		{	put(Recovered,					102);	} // SysMgtLog
		{	put(InitializationError,		103);	} // SysMgtLog
		{	put(DiskSpaceError,				104);	}
		{	put(BackupError,				105);	}
		{	put(DatabaseLockError,			106);	}
		{	put(OptimizationError,			107);	}
		{	put(MailSendError,				108);	}
		{	put(logRotateError,				109);	}
		{	put(RecoveryError,				201);	} // SysMgtLog
		{	put(DatabaseOpenError,			202);	}
		{	put(OptimizationRecoveryError,	203);	}
		{	put(DatabaseFinalizationError,	204);	}
	};
	/**
	 * エラー内容からエラーコードを返す<br/>
	 * 存在しないエラー内容の場合は999を返す
	 *
	 * @param key エラー内容
	 * @return エラーコード
	 */
	public static int getErrorCode(final String key) {
		return MapErrorCode.getOrDefault(key, 999);
	}

	/**
	 * エラー内容からエラーコードを返す<br/>
	 * システム管理ログ用(4桁)
	 *
	 * @param key
	 * @return
	 */
	public static String getLogCode(final String key) {
		return String.format("%04d", getErrorCode(key));
	}
}
