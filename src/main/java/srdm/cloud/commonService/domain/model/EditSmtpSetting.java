package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class EditSmtpSetting implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3876498435763489587L;

	private String smtpHost;
	private String smtpPort;
	private boolean useAuth;
	private String userName;
	private boolean pwdChgFlag;
	private String password;
	private String fromAddress;
	private boolean useSSL;
}
