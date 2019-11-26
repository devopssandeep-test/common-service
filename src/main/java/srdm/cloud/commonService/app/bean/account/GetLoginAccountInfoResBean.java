package srdm.cloud.commonService.app.bean.account;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import srdm.common.bean.BaseBean;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper = false)
public class GetLoginAccountInfoResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 3901627574341082131L;

	@Getter
	@ToString
	private static class Permission extends BaseBean {
		/**
		 *
		 */
		private static final long serialVersionUID = 2343944919435469495L;

		private String permissionName;
		private String attribute;

		protected Permission(String permissionName, String attribute) {
			this.permissionName = permissionName;
			this.attribute = attribute;
		}
	}

	private String domainId;
	private String domainName;
	private String accountId;
	private String accountName;
	@Getter(AccessLevel.NONE)
	@JsonProperty("isPermanentAccount")
	private boolean isPermanentAccount;
	private String language;
	private String dateTimeFormat;
	private String timeZoneSpecifingType;
	private String timeZoneId;
	private String homeGroupId;
	private String targetGroupState;
	private List<Permission> permissionList = new ArrayList<Permission>();

	public void addPermission(String permissionName, String attribute) {
		permissionList.add(new Permission(permissionName, attribute));
	}
}
