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
@XmlRootElement(name="logForView")
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogForView implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -6177728008431083405L;

	private String logId;
	private String kind;
	private long timestamp;
	private String domainId;
	private String domainName;
	private String accountId;
	private String accountName;
	private String operation;
	private String accountStatus;
	private String code;
}
