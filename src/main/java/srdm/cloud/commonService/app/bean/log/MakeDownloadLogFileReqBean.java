package srdm.cloud.commonService.app.bean.log;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class MakeDownloadLogFileReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -5581345419914311518L;

	@NotBlank(message="E0011")
	@Pattern(regexp = "tomcat|mib|tco|fss|fd|EmailAlert|common", message="E0014")
	private String logType;
}
