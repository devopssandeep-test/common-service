package srdm.cloud.commonService.app.bean.role;

import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.cloud.commonService.validation.DomainId;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper = false)
public class CreateRoleReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -1270627455686625607L;

	@NotBlank(message = "E0011")
	@DomainId
	private String domainId;

	@NotBlank(message = "E0011")
	@Size(min = 1, max = 64, message = "E0014")
	@Pattern(regexp = "[^\\\\/:*?\"<>|]*", message = "E0014")
	private String roleName;

	@NotNull(message = "E0011")
	//@Getter(AccessLevel.NONE)
	@JsonProperty("isRoleCanEdit")
	private Boolean isRoleCanEdit;

	@NotNull(message = "E0011")
	@Size(max = 100, message = "E0014")
	@Pattern(regexp = "[^\\\\/:*?\"<>|]*", message = "E0014")
	private String description;

	@NotNull(message = "E0011")
	private List<String> permissionList;

	@NotNull(message = "E0011")
	@Min(value = 0L, message = "E0014") // 無制限
	@Max(value = 9223372036854775807L, message = "E0014") // Long.MAX_VALUE
	private Long sessionTimeout;

	public boolean isRoleCanEdit() {
		return (isRoleCanEdit != null) ? isRoleCanEdit : false;
	}
}
