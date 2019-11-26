package srdm.cloud.commonService.app.bean.account;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class CreateAccountResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 8685541037575955924L;

	private String accountId;
}
