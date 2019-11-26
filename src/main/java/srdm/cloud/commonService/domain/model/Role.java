package srdm.cloud.commonService.domain.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Role implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -7338485312346373179L;

	private String roleId;
	private String roleName;
	private String description;
	private RoleAttribute roleAttribute;
	private List<Permission> permissionList;
	private long sessionTimeout;

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Permission> getPermissionList() {
		return permissionList;
	}

	public void setPermissionList(List<Permission> permissionList) {
		this.permissionList = permissionList;
	}

	public RoleAttribute getRoleAttribute() {
		return roleAttribute;
	}

	public void setRoleAttribute(RoleAttribute roleAttribute) {
		this.roleAttribute = roleAttribute;
	}

	public long getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(long sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	@Override
	public String toString() {
		return "Role [roleId=" + roleId + ", roleName=" + roleName + ", description=" + description + ", roleAttribute="
				+ roleAttribute + ", permissionList=" + permissionList + "], sessionTimeout=" + sessionTimeout;
	}


}
