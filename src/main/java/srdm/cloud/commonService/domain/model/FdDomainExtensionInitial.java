package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.Data;
import srdm.common.constant.SrdmConstants;


@Data
@JsonRootName("domainExtension")
@JsonIgnoreProperties(ignoreUnknown = true)
public class FdDomainExtensionInitial implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1654121772592589969L;

	
	private String domainId;

	private String theme = SrdmConstants.DEFAULT_DOMAIN_FD_THEME;
}
