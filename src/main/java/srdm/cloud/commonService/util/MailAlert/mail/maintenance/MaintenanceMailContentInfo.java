package srdm.cloud.commonService.util.MailAlert.mail.maintenance;

import java.util.Map;

import lombok.ToString;

/**
 * 件名および本文
 * (ローカライズ対象のデータ)
 */
@ToString
public class MaintenanceMailContentInfo {
	/*
	 * 本文ローカライズ情報
	 */
	private String timeZoneId = null;
	private String dateTimeFormat = null;
	private String language = null;
	public void setTimeZoneId(final String timeZoneId) {
		this.timeZoneId = timeZoneId;
	}
	public String getTimeZoneId() {
		return this.timeZoneId;
	}
	public void setDateTimeFormat(final String dateTimeFormat) {
		this.dateTimeFormat = dateTimeFormat;
	}
	public String getDateTimeFormat() {
		return this.dateTimeFormat;
	}
	public void setLanguage(final String language) {
		this.language = language;
	}
	public String getLanguage() {
		return this.language;
	}

	/*
	 * 本文情報
	 */
	private long startTimestamp = 0;
	private long endTimestamp = 0;
	private long dataFolderSizeBefore = 0;
	private long dataFolderSizeAfter = 0;
	private long diskFreeSizeBefore = 0;
	private long diskFreeSizeAfter = 0;
	private Map<String, String> errors = null;	// <Key, Value> = <dbName, error> // 1DB・1エラー

	public void setStartTimestamp(final long startTimestamp) {
		this.startTimestamp = startTimestamp;
	}
	protected long getStartTimestamp() {
		return this.startTimestamp;
	}
	public void setEndTimestamp(final long endTimestamp) {
		this.endTimestamp = endTimestamp;
	}
	protected long getEndTimestamp() {
		return this.endTimestamp;
	}
	public void setDataFolderSizeBefore(final long dataFolderSizeBefore) {
		this.dataFolderSizeBefore = dataFolderSizeBefore;
	}
	protected long getDataFolderSizeBefore() {
		return this.dataFolderSizeBefore;
	}
	public void setDataFolderSizeAfter(final long dataFolderSizeAfter) {
		this.dataFolderSizeAfter = dataFolderSizeAfter;
	}
	protected long getDataFolderSizeAfter() {
		return this.dataFolderSizeAfter;
	}
	public void setDiskFreeSizeBefore(final long diskFreeSizeBefore) {
		this.diskFreeSizeBefore = diskFreeSizeBefore;
	}
	protected long getDiskFreeSizeBefore() {
		return this.diskFreeSizeBefore;
	}
	public void setDiskFreeSizeAfter(final long diskFreeSizeAfter) {
		this.diskFreeSizeAfter = diskFreeSizeAfter;
	}
	protected long getDiskFreeSizeAfter() {
		return this.diskFreeSizeAfter;
	}
	public void setErrors(final Map<String, String> errors) {
		this.errors = errors;
	}
	protected Map<String, String> getErrors() {
		return this.errors;
	}
}
