package srdm.cloud.commonService.app.bean.export;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class ExportOperationLogResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 674653221482635521L;

	private long requestId;
}
