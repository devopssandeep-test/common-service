package srdm.cloud.commonService.app.bean.log;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetOperationLogDetailResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -2092191123622019338L;

	private long resultCount;
	private String domainId;
	private String logId;

	private List<Item> itemList;
}
