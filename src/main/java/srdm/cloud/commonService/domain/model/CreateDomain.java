package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class CreateDomain implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 7475437378945186748L;

	private String domainName;
	private String parentDomainId;
	private String targetGroupId;
}
