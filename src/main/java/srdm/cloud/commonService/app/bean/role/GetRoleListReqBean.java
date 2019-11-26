package srdm.cloud.commonService.app.bean.role;

import java.util.List;

import javax.validation.Valid;
import javax.validation.groups.Default;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.cloud.commonService.app.bean.OrderBy;
import srdm.cloud.commonService.app.bean.SimpleFilter;
import srdm.cloud.commonService.validation.DomainId;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetRoleListReqBean extends JsonBaseReqBean {
	public static interface GetRoleList extends Default {};			// Validationのグループ名

	/**
	 *
	 */
	private static final long serialVersionUID = 263971499652338673L;

	private long startIndex;

	private long count;

	@NotBlank(message="E0011")
	@DomainId
	private String domainId;

	@Valid
	private List<SimpleFilter> simpleFilter;

	@Valid
	private List<OrderBy> orderBy;
}
