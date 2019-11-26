package srdm.cloud.commonService.domain.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import srdm.cloud.commonService.domain.model.Permission;
import srdm.cloud.commonService.domain.model.Role;

public abstract class GetOnlyPermissionList {
	@JsonIgnore
	abstract List<Permission> getPermissionList();

	@JsonIgnore
	abstract List<Role> getRoleList();
}
