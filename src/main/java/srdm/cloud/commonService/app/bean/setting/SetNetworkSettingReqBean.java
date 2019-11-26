package srdm.cloud.commonService.app.bean.setting;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.cloud.commonService.validation.EnableRequired;
import srdm.cloud.commonService.validation.IpAddress;
import srdm.cloud.commonService.validation.Port;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
@EnableRequired(field="ipAddress")
@EnableRequired(field="httpPort")
@EnableRequired(field="httpsPort")
@EnableRequired(field="tunnelPort")
public class SetNetworkSettingReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 4087262748619900811L;

	@NotBlank(message="E0011")
	@Pattern(regexp="(?i)true|(?i)false", message="E0014")
	private String ipAddressEnable;

	@NotNull(message="E0011")
	@IpAddress
	private String ipAddress;

	@NotBlank(message="E0011")
	@Pattern(regexp="(?i)true|(?i)false", message="E0014")
	private String httpPortEnable;

	@NotNull(message="E0011")
	@Port
	private String httpPort;

	@NotBlank(message="E0011")
	@Pattern(regexp="(?i)true|(?i)false", message="E0014")
	private String httpsPortEnable;

	@NotNull(message="E0011")
	@Port
	private String httpsPort;

	@Pattern(regexp="(?i)true|(?i)false", message="E0014")
	private String tunnelPortEnable;

	@Port
	private String tunnelPort;
}
