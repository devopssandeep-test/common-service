package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.Data;

@Data
@JsonRootName("networkSetting")
@XmlType(propOrder = { "ipAddressEnable", "ipAddress", "httpPortEnable", "httpPort", "httpsPortEnable", "httpsPort",
		"tunnelPortEnable", "tunnelPort" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkSetting implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -5958203055151379102L;

	@JsonProperty
	private String ipAddressEnable;
	
	@JsonProperty
	private String ipAddress;
	
	@JsonProperty
	private String httpPortEnable;
	
	@JsonProperty
	private String httpPort;
	
	@JsonProperty
	private String httpsPortEnable;
	
	@JsonProperty
	private String httpsPort;
	
	@JsonProperty
	private String tunnelPortEnable;
	
	@JsonProperty
	private String tunnelPort;
}
