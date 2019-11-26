package srdm.cloud.commonService.util.optimization;

import java.util.HashMap;
import java.util.Map;

import lombok.ToString;

@ToString
public class DBOptResultInfo {
	private long startTimestamp = 0;
	private long endTimestamp = 0;
	private long dbDataSizeBefore;
	private long dbDataSizeAfter;
	private long diskFreeSizeBefore;
	private long diskFreeSizeAfter;
	private String logFolder = "";
	private String dataFolder = "";
	private Map<String, String> errors = new HashMap<String, String>();	// <Key, Value> = <dbName, error> // 1DB・1エラー

	/**
	 * 開始日時(ミリ秒)
	 *
	 * @param timestamp
	 */
	public void setStartTimestamp(final long timestamp) {
		startTimestamp = timestamp;
	}

	/**
	 * 開始日時(ミリ秒)
	 *
	 * @return
	 */
	public long getStartTimestamp() {
		return startTimestamp;
	}

	/**
	 * 終了日時(ミリ秒)
	 *
	 * @param timestamp
	 */
	public void setEndTimestamp(final long timestamp) {
		endTimestamp = timestamp;
	}

	/**
	 * 終了日時(ミリ秒)
	 *
	 * @return
	 */
	public long getEndTimestamp() {
		return endTimestamp;
	}

	/**
	 * 開始時点のDBサイズ(バイト)
	 *
	 * @param size
	 */
	public void setDbDataSizeBefore(long size) {
		dbDataSizeBefore = size;
	}

	/**
	 * 開始時点のDBサイズ(バイト)
	 *
	 * @return
	 */
	public long getDbDataSizeBefore() {
		return dbDataSizeBefore;
	}

	/**
	 * 終了時点のDBサイズ(バイト)
	 *
	 * @param size
	 */
	public void setDbDataSizeAfter(long size) {
		dbDataSizeAfter = size;
	}

	/**
	 * 終了時点のDBサイズ(バイト)
	 *
	 * @return
	 */
	public long getDbDataSizeAfter() {
		return dbDataSizeAfter;
	}

	/**
	 * 開始時点のディスク空き容量(バイト)
	 *
	 * @param size
	 */
	public void setDiskFreeSizeBefore(long size) {
		diskFreeSizeBefore = size;
	}

	/**
	 * 開始時点のディスク空き容量(バイト)
	 *
	 * @return
	 */
	public long getDiskFreeSizeBefore() {
		return diskFreeSizeBefore;
	}

	/**
	 * 終了時点のディスク空き容量(バイト)
	 *
	 * @param size
	 */
	public void setDiskFreeSizeAfter(long size) {
		diskFreeSizeAfter = size;
	}

	/**
	 * 終了時点のディスク空き容量(バイト)
	 *
	 * @return
	 */
	public long getDiskFreeSizeAfter() {
		return diskFreeSizeAfter;
	}

	/**
	 * バックアップファイル(zip)が格納されているフォルダ
	 *
	 * @return
	 */
	public String getZipFolder() {
		return dataFolder;
	}

	/**
	 * データベースのデータフォルダ
	 *
	 * @param dir
	 */
	public void setDataFolder(final String dir) {
		dataFolder = dir;
	}

	/**
	 * データベースのデータフォルダ
	 * @return
	 */
	public String getDataFolder() {
		return dataFolder;
	}

	/**
	 * ログフォルダ
	 *
	 * @param logFolder
	 */
	public void setLogFolder(final String logFolder) {
		this.logFolder = logFolder;
	}

	/**
	 * ログフォルダ
	 *
	 * @return
	 */
	public String getLogFolder() {
		return logFolder;
	}

	/**
	 * 最適化処理中に発生したエラー
	 *
	 * @param name
	 * @param error
	 */
	public void addError(final String name, final String error) {
		errors.put(name, error);
	}

	/**
	 * 最適化処理中に発生したエラー<br/>
	 * <Key, Value> = DB名または発生タイミング、エラー内容
	 *
	 * @return
	 */
	public Map<String, String> getErrors() {
		return errors;
	}

	/**
	 * 最適化処理中に発生したエラーをクリアする<br/>
	 *
	 * @return
	 */
	public void clearErrors() {
		errors.clear();
	}
}

