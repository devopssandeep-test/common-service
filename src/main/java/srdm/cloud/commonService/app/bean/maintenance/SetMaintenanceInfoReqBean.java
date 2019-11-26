package srdm.cloud.commonService.app.bean.maintenance;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseReqBean;

/**
 * GetMaintenanceInfoReqBeanクラス
 *
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class SetMaintenanceInfoReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 6126849433212842697L;

	@NotNull(message="E0011")
	private String message;

}
