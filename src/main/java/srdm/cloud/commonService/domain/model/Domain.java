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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "domain")
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlType(propOrder = { "domainId", "domainName", "parentDomainId", "theme", "roleList" })
public class Domain implements Serializable {

	public String getDomainId() {
		return domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getParentDomainId() {
		return parentDomainId;
	}

	public void setParentDomainId(String parentDomainId) {
		this.parentDomainId = parentDomainId;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}	

	public List<Role> getRoleList() {
		return roleList;
	}

	public void setRoleList(List<Role> roleList) {
		this.roleList = roleList;
	}

	/**
	 *
	 */
	private static final long serialVersionUID = -2844860864036035824L;

	private String domainId;
	private String domainName;
	private String parentDomainId;
	private String theme;
	

	@XmlElementWrapper(name = "roleList")
	@XmlElement(name = "role")
	private List<Role> roleList;

	@Override
	public String toString() {
		return "Domain [domainId=" + domainId + ", domainName=" + domainName + ", parentDomainId=" + parentDomainId
				+ ", theme=" + theme + ", roleList=" + roleList + "]";
	}

}
