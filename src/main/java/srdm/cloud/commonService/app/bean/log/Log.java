package srdm.cloud.commonService.app.bean.log;

import java.io.Serializable;

import lombok.Data;

@Data
public class Log implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 539530131778326836L;

	private String logId;
	private String kind;
	private long timestamp;
	private String domainId;
	private String domainName;
	private String accountId;
	private String accountName;
	private String accountStatus;
	private String operation;
	private String code;
}
