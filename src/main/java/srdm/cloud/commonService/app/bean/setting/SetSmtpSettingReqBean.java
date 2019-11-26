package srdm.cloud.commonService.app.bean.setting;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.cloud.commonService.validation.EmailAddress;
import srdm.cloud.commonService.validation.MailPassword;
import srdm.cloud.commonService.validation.MailServer;
import srdm.cloud.commonService.validation.MailUserName;
import srdm.cloud.commonService.validation.Port;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
@MailPassword(field="password")
public class SetSmtpSettingReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -3292285924283001929L;

	@NotNull(message="E0011")
	@MailServer
	private String smtpHost;

	@NotNull(message="E0011")
	@Port
	private String smtpPort;

	@NotBlank(message="E0011")
	@Pattern(regexp="(?i)true|(?i)false", message="E0014")
	private String useAuth;

	@NotNull(message="E0011")
	@MailUserName
	private String userName;

	@NotBlank(message="E0011")
	@Pattern(regexp="(?i)true|(?i)false", message="E0014")
	private String pwdChgFlag;

	// バリデーションは、@MailPasswordで実施
	@NotNull(message="E0011")
	private String password;

	@NotNull(message="E0011")
	@EmailAddress
	private String fromAddress;

	@NotBlank(message="E0011")
	@Pattern(regexp="(?i)true|(?i)false", message="E0014")
	private String useSSL;
}
