package srdm.cloud.commonService.app.bean;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.cloud.commonService.app.bean.account.GetAccountListReqBean.GetAccountList;
import srdm.cloud.commonService.app.bean.log.GetOperationLogReqBean.GetOperationLog;
import srdm.cloud.commonService.app.bean.log.GetSystemLogReqBean.GetSystemLog;
import srdm.cloud.commonService.app.bean.role.GetRoleListReqBean.GetRoleList;
import srdm.common.bean.BaseBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class OrderBy extends BaseBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@NotBlank(message="E0011")
	@Pattern.List({
		@Pattern(regexp="accountId|accountName|roleName|accountStatus", message="E0014", groups=GetAccountList.class),
		@Pattern(regexp="roleId|roleName|description|sessionTimeout", message="E0014", groups=GetRoleList.class),
		@Pattern(regexp="kind|timestamp|domainId|domainName|accountId|accountName|operation|code",
			message="E0014",
			groups={GetSystemLog.class, GetOperationLog.class})
	})
	private String key;

	@NotBlank(message="E0011")
	@Pattern(regexp="ascending|descending", message="E0014")
	private String order;
}
