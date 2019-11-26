package srdm.cloud.commonService.app.bean.log;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class MakeDownloadLogFileResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -2815173507597746701L;

	private long requestId;
}
