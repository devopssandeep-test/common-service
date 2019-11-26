package srdm.cloud.commonService.app.bean.setting;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetEnableRspResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 1067696801747241042L;

	private String rspEnableStatus;
}
