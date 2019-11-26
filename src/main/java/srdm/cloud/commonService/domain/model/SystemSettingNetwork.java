package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SystemSettingNetwork implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -2585473538660786252L;

	private boolean enablePublicIp;
	private String publicIp;
	private String privateIp;
	private boolean enablePublicHttpPort;
	private String publicHttpPort;
	private String privateHttpPort;
	private boolean enablePublicHttpsPort;
	private String publicHttpsPort;
	private String privateHttpsPort;
	private boolean enableSsl;
}
