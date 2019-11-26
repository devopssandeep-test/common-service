package srdm.cloud.commonService.app.bean.export;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class ExportSystemLogResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 7519591935371838809L;

	private long requestId;
}
