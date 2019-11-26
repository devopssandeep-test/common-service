package srdm.cloud.commonService.domain.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="opeLog")
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlType(propOrder={"logId", "kind", "timestamp", "targetDomainId", "accountId",
		"accountName", "domainId", "accountStatus", "domainName", "operation","code","itemList"})
public class OperationLog implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -8204712954552062324L;

	private String logId;
	private String kind;
	private long timestamp;
	private String targetDomainId;
	private String accountId;
	private String accountName;
	private String domainId;
	private String domainName;
	private String operation;
	private String code;
	private String accountStatus;

	@XmlElementWrapper(name="itemList")
	@XmlElement(name="item")
	private List<LogItem> itemList;
}
