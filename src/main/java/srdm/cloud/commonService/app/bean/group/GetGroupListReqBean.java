package srdm.cloud.commonService.app.bean.group;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetGroupListReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 2367661921883693274L;


	private long startIndex;
	private long count;

	@NotBlank(message="E0011")
	@Pattern(regexp="^[\\p{ASCII}]+$", message="E0014")
	private String groupId;

	@Pattern(regexp="no|childrenOnly", message="E0014")
	private String expand;
}
