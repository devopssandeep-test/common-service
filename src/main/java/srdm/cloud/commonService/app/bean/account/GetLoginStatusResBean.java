package srdm.cloud.commonService.app.bean.account;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetLoginStatusResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -4841649146369823656L;

	private boolean loginFlg;

}
