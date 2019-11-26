package srdm.cloud.commonService.app.bean.log;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.cloud.commonService.validation.DomainId;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class DeleteOperationLogReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 3533108231456728229L;

	@NotBlank(message="E0011")
	@DomainId
	private String domainId;
}
