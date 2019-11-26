package srdm.cloud.commonService.app.bean.account;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.cloud.commonService.validation.AccountId;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetAccountReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 4327956449566260138L;

	@NotBlank(message="E0011")
	@AccountId
	private String accountId;
}
