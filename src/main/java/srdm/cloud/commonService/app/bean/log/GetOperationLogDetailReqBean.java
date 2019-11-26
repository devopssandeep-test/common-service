package srdm.cloud.commonService.app.bean.log;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.cloud.commonService.validation.DomainId;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetOperationLogDetailReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 813030710713721801L;

	@NotBlank(message="E0011")
	@DomainId
	private String domainId;

	@NotBlank(message="E0011")
	@Pattern(regexp="^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$",message="E0014")
	private String logId;
}
