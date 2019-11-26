package srdm.cloud.commonService.app.bean.log;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetSystemLogDetailResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 4812921752519282860L;

	private long resultCount;
	private String logId;

	private List<Item> itemList;
}
