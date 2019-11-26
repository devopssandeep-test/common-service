package srdm.cloud.commonService.app.bean.domain;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class SetThemeSettingReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -3291009224560977631L;

	@NotBlank(message="E0011")
	@Pattern(regexp="Enterprise|EnterpriseBlue|EnterpriseRed|EnterpriseGreen|Graphite|Tahoe", message="E0014")
	private String theme;
}
