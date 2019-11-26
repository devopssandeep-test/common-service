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
@XmlRootElement(name="sysMgtLog")
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlType(propOrder={"logId", "kind","timestamp","accountId", "accountName",
		"domainId", "domainName", "accountStatus", "operation","code","itemList"})
public class SystemManagementLog implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -8751927853231945013L;

	private String logId;
	private String kind;
	private long timestamp;
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
