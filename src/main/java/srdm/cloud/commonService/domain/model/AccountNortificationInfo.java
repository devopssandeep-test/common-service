package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class AccountNortificationInfo implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5481175807376975705L;

	private String toAddress;

	private String ccAddress;

	private String bccAddress;

	private String subject;

	private String body;
}
