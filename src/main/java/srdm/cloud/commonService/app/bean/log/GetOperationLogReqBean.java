package srdm.cloud.commonService.app.bean.log;

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
public class GetOperationLogReqBean extends JsonBaseReqBean {
	public static interface GetOperationLog extends Default {};

	/**
	 *
	 */
	private static final long serialVersionUID = -3490651529754982812L;

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
