package srdm.cloud.commonService.app.bean.account;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class LoginResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 394114895372694911L;

	private boolean validateResultFlg;
	private String sessionId;

}
