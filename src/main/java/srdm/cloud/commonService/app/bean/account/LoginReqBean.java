package srdm.cloud.commonService.app.bean.account;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class LoginReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 979273218689026303L;

	@NotBlank(message="E0011")
	private String domainId;

	@NotBlank(message="E0011")
	private String accountName;

	@NotBlank(message="E0011")
	private String password;

	private String sessionId;
}
