package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.Data;

@Data
@JsonRootName("roleAttribute")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleAttribute implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1178050455264521334L;

	@JsonProperty
	private boolean isPrivateRole;
	@JsonProperty
	private boolean isRoleCanEdit;

	@Override
	public String toString() {
		return "RoleAttribute [isPrivateRole=" + isPrivateRole + ", isRoleCanEdit=" + isRoleCanEdit + "]";
	}

}
