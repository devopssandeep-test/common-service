package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AccountInfo implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -2660968597846834835L;

	private String accountId;
	private String accountType;
	private String accountName;
	@JsonProperty()
	private boolean isPermanentAccount;
	private String password;
	private String domainId;
	private String roleId;
	@JsonProperty()
	private boolean isPrivateRole;
	private String timeZoneSpecifingType;
	private String timeZone;
	private String language;
	private String dateTimeFormat;
	private String homeGroupId;
	private String homeGroupName;
	private String accountStatus;
	private int errorCount;
	private long latestErrorTimestamp;
}
