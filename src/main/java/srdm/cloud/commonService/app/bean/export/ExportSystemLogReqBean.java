package srdm.cloud.commonService.app.bean.export;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class ExportSystemLogReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 8766964098145125316L;

	@NotBlank(message="E0011")
	@Pattern(regexp="xml", message="E0014")
	private String format;
}
