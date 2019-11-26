package srdm.cloud.commonService.app.bean.log;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetSystemLogResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 387040100065115114L;

	private long startIndex;
	private long count;
	private long resultCount;
	private long totalCount;
	private List<Log> logList = new ArrayList<Log>();
}
