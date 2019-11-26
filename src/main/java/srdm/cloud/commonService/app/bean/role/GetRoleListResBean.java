package srdm.cloud.commonService.app.bean.role;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import srdm.common.bean.BaseBean;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetRoleListResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 896541248631230778L;

	@Getter
	@Setter
	@ToString
	private static class Role extends BaseBean {

		/**
		 *
		 */
		private static final long serialVersionUID = 8307345707743522933L;

		private String roleId;
		private String roleName;
		private String description;

		//@Getter(AccessLevel.NONE)
		@JsonProperty("isRoleCanEdit")
		private boolean isRoleCanEdit;

	//	@Getter(AccessLevel.NONE)
		@JsonProperty("isPrivateRole")
		private boolean isPrivateRole;

		@Getter(AccessLevel.NONE)	// @JsonPropertyを付与するとJsonに変換した時にProperty名変換前後の項目が出力されるため、Getterを抑制
		@JsonProperty("isLinkedAccount")
		private boolean isLinkedAccount;
		private boolean canDelete;

		private long sessionTimeout;

		protected Role(String roleId, String roleName, boolean isRoleCanEdit, boolean isPrivateRole, String description, boolean isLinkedAccount, boolean canDelete, long sessionTimeout) {
			this.roleId = roleId;
			this.roleName = roleName;
			this.isRoleCanEdit = isRoleCanEdit;
			this.isPrivateRole = isPrivateRole;
			this.description = description;
			this.isLinkedAccount = isLinkedAccount;
			this.canDelete = canDelete;
			this.sessionTimeout = sessionTimeout;
		}
	}

	private long startIndex;
	private long count;
	private long resultCount;
	private long totalCount;
	private String domainId;
	private List<Role> roleList = new ArrayList<Role>();

	public void addRole(String roleId, String roleName, boolean isRoleCanEdit, boolean isPrivateRole, String description, boolean isLinkedAccount, boolean canDelete, long sessionTimeout) {
		roleList.add(new Role(roleId, roleName, isRoleCanEdit, isPrivateRole, description, isLinkedAccount, canDelete, sessionTimeout));
	}
}
