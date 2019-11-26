package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.Data;

/**
 * ロールリスト用オブジェクト
 *
 */
@Data
@JsonRootName("simpleRole")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimpleRole implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3867016918978158240L;
	@JsonProperty
	private String roleId;
	@JsonProperty
	private String roleName;
	@JsonProperty
	private boolean isRoleCanEdit;
	@JsonProperty
	private boolean isPrivateRole;
	@JsonProperty
	private String description;
	@JsonProperty
	private boolean linkedAccount;
	@JsonProperty
	private boolean canDelete;
	@JsonProperty
	private long sessionTimeout;
}
