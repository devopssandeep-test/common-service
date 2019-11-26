package srdm.cloud.commonService.app.bean.log;

import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class DownLoadLogFileReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -3286454867774931938L;

	@NotBlank(message="E0011")
	@Min(0)
	private long requestId;
}
