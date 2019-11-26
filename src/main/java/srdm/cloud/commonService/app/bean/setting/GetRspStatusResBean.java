package srdm.cloud.commonService.app.bean.setting;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetRspStatusResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -2798976431012077171L;

	private String status;
}
