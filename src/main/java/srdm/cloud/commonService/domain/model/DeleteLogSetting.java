package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeleteLogSetting implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 6054272057068546445L;

	private long period;
	private long startHour;
	private long startMinute;
	private boolean execFlag;

	//
	private String domainId;
	private String accountId;
}
