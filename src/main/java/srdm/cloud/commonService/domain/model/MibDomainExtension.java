package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="mibDomainExtension")
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlType(propOrder={"domainId", "groupId", "groupName"})
public class MibDomainExtension implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1654121772592589969L;

	private String domainId;
	private String groupId;
	private String groupName;
}
