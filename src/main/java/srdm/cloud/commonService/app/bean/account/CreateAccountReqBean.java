package srdm.cloud.commonService.app.bean.account;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import srdm.cloud.commonService.validation.DateTimeFormat;
import srdm.cloud.commonService.validation.DomainId;
import srdm.cloud.commonService.validation.Language;
import srdm.cloud.commonService.validation.Password;
import srdm.cloud.commonService.validation.RoleId;
import srdm.cloud.commonService.validation.TimeZoneId;
import srdm.cloud.commonService.validation.TimeZoneIdConfirm;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper = false)
@TimeZoneIdConfirm
public class CreateAccountReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -880798263028924667L;

	@NotBlank(message = "E0011")
	@Size(min = 1, max = 64, message = "E0014")
	@Pattern.List({ @Pattern(regexp = "^[\\p{ASCII}]+$", message = "E0014"),
			@Pattern(regexp = "[^\\\\/:*?\"<>|]*", message = "E0014") })
	private String accountName;

	@NotNull(message = "E0011")
	//@Getter(AccessLevel.NONE)
	@JsonProperty("isPermanentAccount")
	private Boolean isPermanentAccount;

	@NotBlank(message = "E0011")
	@Password
	private String password;

	@NotBlank(message = "E0011")
	@RoleId
	private String roleId;

	//@Getter(AccessLevel.NONE)
	@JsonProperty("isPrivateRole")
	private Boolean isPrivateRole;

	@NotBlank(message = "E0011")
	@DomainId
	private String domainId;

	@NotBlank(message = "E0011")
	@Language
	private String language;

	@NotBlank(message = "E0011")
	@DateTimeFormat
	private String dateTimeFormat;

	@NotBlank(message = "E0011")
	@Pattern(regexp = "auto|manual", message = "E0014")
	private String timeZoneSpecifingType;

	@TimeZoneId
	private String timeZoneId;

	@NotNull(message = "E0011")
	@Pattern(regexp = "^[\\p{ASCII}]+$", message = "E0014")
	private String homeGroupId;

}
