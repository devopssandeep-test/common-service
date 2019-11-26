package srdm.cloud.commonService.app.bean.role;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class CreateRoleResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 6848653546389667606L;

	private String roleId;
}
