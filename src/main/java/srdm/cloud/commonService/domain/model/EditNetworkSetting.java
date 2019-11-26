package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class EditNetworkSetting implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1755970863532100637L;

	private String ipAddressEnable;
	private String ipAddress;
	private String httpPortEnable;
	private String httpPort;
	private String httpsPortEnable;
	private String httpsPort;
	private String tunnelPortEnable;
	private String tunnelPort;

	private boolean cloudServicePermission;
}
