package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.Data;

@Data
@JsonRootName("account")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Account implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -4665256873155897360L;
	


	@JsonProperty
	private String accountId;
	
	@JsonProperty
	private String accountType;
	
	@JsonProperty
	private String accountName;
	
	@JsonProperty
	private String password;
	
	@JsonProperty
	private String domainId;
	
	@JsonProperty
	private String roleId;
	
	@JsonProperty
	private String timeZoneSpecifingType;
	
	@JsonProperty	
	private String timeZone;
	
	@JsonProperty
	private String language;
	
	@JsonProperty
	private String dateTimeFormat;
	
	@JsonProperty
	private String homeGroupId;
	
	@JsonProperty
	private String homeGroupName;
	
	@JsonProperty
	private String accountStatus;
	
	@JsonProperty
	private int    errorCount;
	
	@JsonProperty
	private long   latestErrorTimestamp;
	
	@JsonProperty
	private boolean isPermanentAccount;

}
