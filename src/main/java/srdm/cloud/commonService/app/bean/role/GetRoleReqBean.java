package srdm.cloud.commonService.app.bean.role;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.cloud.commonService.validation.DomainId;
import srdm.cloud.commonService.validation.RoleId;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetRoleReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -4663582334665827834L;

	@NotBlank(message="E0011")
	@DomainId
	private String domainId;

	@NotBlank(message="E0011")
	@RoleId
	private String roleId;
}
