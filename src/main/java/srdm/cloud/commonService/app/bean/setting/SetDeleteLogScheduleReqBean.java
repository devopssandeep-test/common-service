package srdm.cloud.commonService.app.bean.setting;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class SetDeleteLogScheduleReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 2494588193930632949L;

	@NotNull(message="E0011")
	@Min(value=0, message="E0014")
	@Max(value=12, message="E0014")
	private Long period;

	@NotNull(message="E0011")
	@Min(value=0, message="E0014")
	@Max(value=23, message="E0014")
	private Long startHour;

	@NotNull(message="E0011")
	@Min(value=0, message="E0014")
	@Max(value=59, message="E0014")
	private Long startMinute;

	@NotBlank(message="E0011")
	@Pattern(regexp = "(?i)true|(?i)false", message="E0014")
	private String execFlag;
}
