package srdm.cloud.commonService.domain.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class LoginAccountInfo implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 6538139184796712421L;

	private String domainId;
	private String domainName;
	private String accountId;
	private String accountName;
	private boolean isPermanentAccount;
	private String language;
	private String dateTimeFormat;
	private String timeZoneSpecifingType;
	private String timeZoneId;
	private String homeGroupId;
	private String targetGroupState;
	private List<Permission> permissionList;
}
