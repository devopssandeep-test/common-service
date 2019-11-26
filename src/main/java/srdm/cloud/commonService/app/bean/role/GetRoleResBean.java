package srdm.cloud.commonService.app.bean.role;

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
@EqualsAndHashCode(callSuper=false)
public class GetRoleResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 7473552216495274198L;

	@Getter
	@ToString
	private static class Permission extends BaseBean {
		/**
		 *
		 */
		private static final long serialVersionUID = -8968628506979737652L;

		private String permissionName;
		private String attribute;

		protected Permission(String permissionName, String attribute) {
			this.permissionName = permissionName;
			this.attribute = attribute;
		}
	}
	@JsonProperty
	private String domainId;
	@JsonProperty
	private String roleId;
	@JsonProperty
	private String roleName;
	@Getter(AccessLevel.NONE)
	@JsonProperty("isRoleCanEdit")
	private boolean isRoleCanEdit;
	@JsonProperty
	private String description;
	@JsonProperty
	private List<Permission> permissionList = new ArrayList<Permission>();
	@JsonProperty
	private long sessionTimeout;

	public void addPermission(String permissionName, String attribute) {
		permissionList.add(new Permission(permissionName, attribute));
	}
}
