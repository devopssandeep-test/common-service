package srdm.cloud.commonService.asynchronous;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import srdm.cloud.commonService.domain.repository.log.OperationLogRepository;
import srdm.cloud.commonService.domain.repository.log.SystemManagementLogRepository;
import srdm.cloud.commonService.domain.repository.session.SrdmSessionRepository;
import srdm.common.exception.SrdmDataAccessException;

/**
 * ログ削除処理（非同期実行）
 * 本クラスのメソッドは、全て別スレッドで実行される。
 *
 */
@Async
@Component
public class AsyncDeleteLogImpl implements AsyncDeleteLog {

	private static final Logger logger = LoggerFactory.getLogger(AsyncDeleteLogImpl.class);

	@Autowired
	OperationLogRepository operationLogRepository;

	@Autowired
	SystemManagementLogRepository systemManagementLogRepository;

	@Autowired
	SrdmSessionRepository srdmSessionRepository;

	/**
	 * 定期ログ削除（操作ログ）
	 * 操作ログ削除のログは、残さないことに決定。
	 * 代わりにシステムログ（commonServiceのログ）に残す。
	 */
	@Override
	public void deleteOperationLog(String domainId, String accountId, long period) {

		// 定期ログ削除開始
		logger.info("== deleteOperationLog:[Start]. domainId[{}], accountId[{}] ==", domainId, accountId);

		logger.info("period(setting):[{}]", Long.toString(period));
		if (period < 1) {
			period = 1; 	// 操作ログは最低1ヶ月分は残す
		}
		logger.info("period(execute period):[{}]", Long.toString(period));
		Calendar FromCal = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		Date date = new Date();
		FromCal.setTime(date);

		// 基点算出（その日の0：00）
		FromCal.set(Calendar.HOUR_OF_DAY, 0);
		FromCal.set(Calendar.MINUTE, 0);
		FromCal.set(Calendar.SECOND, 0);
		FromCal.set(Calendar.MILLISECOND, 0);

		// 削除時間算出（基点-X月）
		FromCal.add(Calendar.MONTH, (int) (period * -1));
		Date FromDate = FromCal.getTime();
		long deleteTime = FromDate.getTime();

		try {
			operationLogRepository.deleteByTimestamp(deleteTime);
			logger.info("== Operation log has been deleted by schedule. ==");
		} catch (SrdmDataAccessException e) {
			logger.error("== Operation log delete faild. ==",e);
		}
		logger.info("== deleteOperationLog:[End]. ==");
	}

	/**
	 * 定期ログ削除（システム管理ログ）
	 * システム管理ログ削除のログは、残さないことに決定。
	 * 代わりにシステムログ（commonServiceのログ）に残す。
	 */
	@Override
	public void deleteSystemLog(String domainId, String accountId, long period) {

		logger.info("== deleteSystemLog:[Start]. domainId[{}], accountId[{}] ==", domainId, accountId);

		logger.info("period(setting):[{}]", Long.toString(period));
		if (period < 1) {
			period = 1; 	// Tomcatログ、システム管理ログは最低1ヶ月分は残す
		}
		logger.info("period(execute period):[{}]", Long.toString(period));

		// Tomcatログ削除
		deleteTomcatLog(period);

		Calendar FromCal = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		Date date = new Date();
		FromCal.setTime(date);

		// 基点算出（その日の0：00）
		FromCal.set(Calendar.HOUR_OF_DAY, 0);
		FromCal.set(Calendar.MINUTE, 0);
		FromCal.set(Calendar.SECOND, 0);
		FromCal.set(Calendar.MILLISECOND, 0);

		// 削除時間算出（基点-X月）
		FromCal.add(Calendar.MONTH, (int) (period * -1));
		Date FromDate = FromCal.getTime();
		long deleteTime = FromDate.getTime();

		try {
			systemManagementLogRepository.deleteByTimestamp(deleteTime);
			logger.info("== System Management log has been deleted by schedule. ==");
		} catch (SrdmDataAccessException e) {
			logger.error("== System Management log delete faild. ==",e);
		}

		logger.info("== deleteSystemLog:[End]. ==");
	}

	/**
	 * TOMCAT_HOME の logsフォルダ内のログファイルを削除する。
	 *
	 * 引数に指定した月数以前のログを削除する。
	 * (「1」なら1月以上前のログを削除)
	 *
	 * ログの出力日はファイル名で判断する。
	 *
	 * ファイル名に日付の無いログは削除しない。
	 *
	 * @param period ログを残す期間(月)
	 *
	 */
	private void deleteTomcatLog( Long period ) {
		logger.info("deleteTomcatLog(" + Long.toString(period) + ")" );

		Calendar FromCal = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		Date date = new Date();
		FromCal.setTime(date);

		// 基点算出（その日の0：00）
		FromCal.set(Calendar.HOUR_OF_DAY, 0);
		FromCal.set(Calendar.MINUTE, 0);
		FromCal.set(Calendar.SECOND, 0);
		FromCal.set(Calendar.MILLISECOND, 0);

		// 削除時間算出（基点-X月）
		FromCal.add(Calendar.MONTH, (int) (period * -1));
		Date FromDate = FromCal.getTime();

		// logファイルの日付と比較用の数値取得
		// periodEndYmd より小さい（過去）のlogファイルを削除する。
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		int periodEndYmd;
		try{
			periodEndYmd = Integer.parseInt(sdf.format(FromDate));
		}catch(NumberFormatException e){
			periodEndYmd = 0;
	   }

		// TOMCAT_HOME の logsフォルダのパス
		String logsPath = System.getProperties().getProperty("catalina.home") + "\\logs";

		//  TOMCAT_HOME の logsフォルダのファイル一覧取得
		File dir = new File(logsPath);
		File[] files = dir.listFiles();
		if(files != null) {
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				// ファイルの場合
				if (files[i].isFile()){

					int logFileYmd = 0;
					boolean logFileDelete = false;

					// ファイル名を「.」で分割し、「-」を取り除いた文字列が数値の場合、
					// かつ、パターン「数字4桁-数字2桁-数字2桁」にマッチする場合、
					// 日付と判断する。
					// 例）localhost_access_log.2015-04-23.txt は日付があるファイル
					String[] fileNameList = file.getName().split("\\.");
					for (String fileName : fileNameList) {
						try{
							logFileYmd = Integer.parseInt(fileName.replaceAll("-", ""));
							Pattern p = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
							Matcher m = p.matcher(fileName);
							if(m.find()){
								logFileDelete = true;
							}
						}catch(NumberFormatException e2){
							continue;
						}
					}

					// ファイル名に日付があり、その日付が引数指定の期間より過去の場合は削除する。
					if(logFileDelete && logFileYmd < periodEndYmd){
						try{
							boolean fileDelete = file.delete();
							if(!fileDelete){
								logger.error("deleteTomcatLog error(" + file.getName() + ")" );
							}
						}catch(SecurityException e){
							logger.error("deleteTomcatLog error(" + file.getName() + ")" );
						}
					}
				}
			}
		} else {
			logger.warn("deleteTomcatLog file not found. path[{}]", logsPath);
		}
	}
}
