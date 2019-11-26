package srdm.cloud.commonService.app.bean.domain;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.cloud.commonService.validation.DomainId;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetDomainReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 3369170814283593948L;

	@NotBlank(message="E0011")
	@DomainId
	private String domainId;
}
