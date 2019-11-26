package srdm.cloud.commonService.app.bean.account;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper = false)
public class GetAccountResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 7487533158643435306L;

	private String accountId;
	private String accountName;
	
	@Getter(AccessLevel.NONE)
	@JsonProperty("isPermanentAccount")
	private boolean isPermanentAccount;
	
	private String domainId;
	private String roleId;
	
	@Getter(AccessLevel.NONE)
	@JsonProperty("isPrivateRole")
	private boolean isPrivateRole;
	
	private String language;
	private String dateTimeFormat;
	private String timeZoneSpecifingType;
	private String timeZoneId;
	private String homeGroupId;
	private String homeGroupName;
	private String accountStatus;

}
