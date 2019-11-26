package srdm.cloud.commonService.app.bean.log;

import java.util.List;

import javax.validation.Valid;
import javax.validation.groups.Default;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.cloud.commonService.app.bean.OrderBy;
import srdm.cloud.commonService.app.bean.SimpleFilter;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetSystemLogReqBean extends JsonBaseReqBean {
	public static interface GetSystemLog extends Default {};

	/**
	 *
	 */
	private static final long serialVersionUID = 7989645121687019338L;

	private long startIndex;

	private long count;

	@Valid
	private List<SimpleFilter> simpleFilter;

	@Valid
	private List<OrderBy> orderBy;
}
