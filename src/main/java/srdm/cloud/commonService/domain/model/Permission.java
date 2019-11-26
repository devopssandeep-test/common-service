package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
@JsonSerialize
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName("permission")
@JsonPropertyOrder({ "permissionName", "attribute" })
public class Permission implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 844607242870721216L;
	@JsonProperty
	private String permissionName;
	@JsonProperty
	private String attribute;

	@Override
	public String toString() {
		return "Permission [permissionName=" + permissionName + ", attribute=" + attribute + "]";
	}

}
