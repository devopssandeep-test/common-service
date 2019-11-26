package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * アカウントリスト取得用アカウントオブジェクト
 *
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="simpleAccount")
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlType(propOrder={"accountId", "accountName","roleName","accountStatus","canDelete"})
public class SimpleAccount implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 2010694259358324488L;

	private String accountId;
	private String accountName;
	private String roleName;
	private String accountStatus;
	private boolean canDelete;
}
