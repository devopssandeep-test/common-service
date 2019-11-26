package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

/**
 * ドメインリスト用オブジェクト
 *
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="simpleDomain")
@XmlType(propOrder={"domainId", "domainName", "parentDomainId"})
public class SimpleDomain implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 8151391610807596876L;

	private String domainId;
	private String domainName;
	private String parentDomainId;
}
