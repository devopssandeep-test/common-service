package srdm.cloud.commonService.app.bean.domain;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.cloud.commonService.validation.DomainId;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class CreateDomainReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 6042908985660610803L;

	@NotBlank(message="E0011")
	@Size(min=1, max=64, message="E0014")
	@Pattern(regexp="[^\\\\/:*?\"<>|]*", message="E0014")
	private String domainName;

	@NotBlank(message="E0011")
	@DomainId
	private String parentDomainId;

	@NotBlank(message="E0011")
	@Pattern(regexp="^[\\p{ASCII}]+$", message="E0014")
	private String targetGroupId;
}
