package srdm.cloud.commonService.app.bean.maintenance;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseResBean;

/**
 * GetMaintenanceStatusResBeanクラス
 *
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class GetMaintenanceStatusResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 8761308887786819771L;

	private String status;
	private long maintenanceCode;
}
