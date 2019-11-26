package srdm.cloud.commonService.app.bean.maintenance;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseResBean;

/**
 * GetMaintenanceInfoResBeanクラス
 *
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class GetMaintenanceInfoResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -7721675680185069607L;

	private String message;

}
