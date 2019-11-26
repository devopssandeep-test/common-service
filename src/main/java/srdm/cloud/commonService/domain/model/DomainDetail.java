package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class DomainDetail implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 3283726288933984221L;

	private String domainId;
	private String domainName;
	private String targetGroupId;
	private String targetGroupName;
	private String httpURL;
	private String httpsURL;
}
