package srdm.cloud.commonService.domain.model;

import java.util.ArrayList;
import java.util.UUID;

import lombok.Getter;
import lombok.ToString;
import srdm.cloud.shared.constants.SrdmSharedConstants;
import srdm.common.constant.SrdmConstants;

/**
 * 仮想ロール
 * （アカウントに紐付くロールが削除された場合に適用するロール）
 *
 */
@Getter
@ToString
public class VirtualRole extends Role {

	/**
	 *
	 */
	private static final long serialVersionUID = 6290731510271704047L;

	public VirtualRole() {
		this.setRoleId(SrdmConstants.ID_PREFIX_ROLE + UUID.randomUUID().toString());	// Dummy Id
		this.setRoleName("Virtual Role");
		this.setDescription("This role is virtual role. Authority, is not.");
		this.setPermissionList(new ArrayList<Permission>());
		this.setSessionTimeout(SrdmSharedConstants.SESSION_TIMEOUT); // temporary timeout value. should be set by caller.
	}
}
