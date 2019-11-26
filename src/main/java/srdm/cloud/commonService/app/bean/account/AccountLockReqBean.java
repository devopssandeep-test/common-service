package srdm.cloud.commonService.app.bean.account;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.cloud.commonService.validation.IdList;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class AccountLockReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 3854872208333775873L;

	@NotNull(message="E0011")
	@Size(min=1, message="E0014")
	@IdList(field = "accountId")
	private List<String> accountIdList;
}
