package srdm.cloud.commonService.util.MailAlert.mail.accountInfo;

import java.io.Serializable;

import lombok.Data;

@Data
public class AccountInfoEmailContent implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -4282488364674249732L;

	private String subject;
	private String body;
}
