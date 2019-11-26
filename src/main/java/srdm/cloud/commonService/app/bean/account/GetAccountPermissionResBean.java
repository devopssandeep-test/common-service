package srdm.cloud.commonService.app.bean.account;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import srdm.common.bean.BaseBean;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetAccountPermissionResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 1976899168434525392L;


	@Getter
	@ToString
	private static class Permission extends BaseBean {
		/**
		 *
		 */
		private static final long serialVersionUID = -6520536371227556375L;

		private String permissionName;
		private String attribute;

		protected Permission(String permissionName, String attribute) {
			this.permissionName = permissionName;
			this.attribute = attribute;
		}
	}
	private List<Permission> permissionList = new ArrayList<Permission>();

	public void addPermission(String permissionName, String attribute) {
		permissionList.add(new Permission(permissionName, attribute));
	}
}
