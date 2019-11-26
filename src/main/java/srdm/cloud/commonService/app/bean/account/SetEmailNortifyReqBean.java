package srdm.cloud.commonService.app.bean.account;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.cloud.commonService.validation.EmailAddrList;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class SetEmailNortifyReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -5460783641733497194L;

	@NotNull(message="E0011")
	@EmailAddrList
	private String toAddress;

	@NotNull(message="E0011")
	@EmailAddrList
	private String ccAddress;

	@NotNull(message="E0011")
	@EmailAddrList
	private String bccAddress;

	@NotNull(message="E0011")
	@Size(max=1000, message="E0014")
	private String subject;

	@NotNull(message="E0011")
	@Size(max=1000, message="E0014")
	private String body;
}
