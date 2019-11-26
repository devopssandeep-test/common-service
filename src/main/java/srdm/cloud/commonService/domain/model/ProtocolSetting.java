package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProtocolSetting implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 912132815497982489L;
	/**
	 *
	 */

	@JsonProperty
	private String http;
	@JsonProperty
	private String https;
	@JsonProperty
	private String serverIP;
	@JsonProperty
	private String landingModule;
}
