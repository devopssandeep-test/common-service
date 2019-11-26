package srdm.cloud.commonService.app.bean.role;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.cloud.commonService.validation.DomainId;
import srdm.cloud.commonService.validation.IdList;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class DeleteRoleReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 6797560932973216445L;

	@NotBlank(message="E0011")
	@DomainId
	private String domainId;

	@NotNull(message="E0011")
	@Size(min=1, message="E0014")
	@IdList(field = "roleId")
	private List<String> roleIdList;
}
