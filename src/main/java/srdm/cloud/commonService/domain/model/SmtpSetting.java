package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.Data;

@Data
@JsonRootName("smtpSetting")
@XmlType(propOrder = { "host", "port", "useAuth", "userName", "password", "fromAddress", "useSsl" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmtpSetting implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 8714590746646469998L;
	@JsonProperty
	private String host;
	@JsonProperty
	private String port;
	@JsonProperty
	private String useAuth;
	@JsonProperty
	private String userName;
	@JsonProperty
	private String password;
	@JsonProperty
	private String fromAddress;
	@JsonProperty
	private String useSsl;
}
