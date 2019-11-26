package srdm.cloud.commonService.app.bean.account;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class LoginForOnPremisesAgentReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 4348645881731953672L;

	@NotBlank(message="E0011")
	private String loginId;
}
