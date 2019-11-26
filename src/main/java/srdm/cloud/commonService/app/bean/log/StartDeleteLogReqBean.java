package srdm.cloud.commonService.app.bean.log;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class StartDeleteLogReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -745013392180267964L;

	@NotNull(message="E0011")
	@Min(value=0, message="E0014")
	@Max(value=12, message="E0014")
	private Long period;
}
