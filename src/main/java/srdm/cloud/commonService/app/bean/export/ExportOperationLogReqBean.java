package srdm.cloud.commonService.app.bean.export;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.cloud.commonService.validation.DomainId;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class ExportOperationLogReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -4465653648198944793L;

	@NotBlank(message="E0011")
	@DomainId
	private String domainId;

	@NotBlank(message="E0011")
	@Pattern(regexp="xml", message="E0014")
	private String format;
}
