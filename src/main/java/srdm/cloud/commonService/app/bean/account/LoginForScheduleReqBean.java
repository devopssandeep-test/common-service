package srdm.cloud.commonService.app.bean.account;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class LoginForScheduleReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 5208694774243929297L;

	@NotBlank(message="E0011")
	private String domainId;

	@NotBlank(message="E0011")
	private String accountId;
}
