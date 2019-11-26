package srdm.cloud.commonService.app.bean.setting;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class SetEnableRspReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -3092808544966692764L;

	@NotBlank(message="E0011")
	@Pattern(regexp="disable|enable",message="E0014")
	private String rspEnableStatus;
}
