package srdm.cloud.commonService.app.bean.domain;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.cloud.commonService.validation.DomainId;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetDomainListReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -7032717230597778843L;

	private long startIndex;
	private long count;

	@NotBlank(message="E0011")
	@DomainId
	private String domainId;

	@Pattern(regexp="no|childrenOnly", message="E0014")
	private String expand;
}
