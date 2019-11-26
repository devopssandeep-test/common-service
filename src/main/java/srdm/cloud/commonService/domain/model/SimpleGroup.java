package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

/**
 * グループリスト取得用オブジェクト
 *
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="simpleGroup")
@XmlType(propOrder={"groupId", "groupName", "parentGroupId"})
public class SimpleGroup implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 395952593966228317L;

	private String groupId;
	private String groupName;
	private String parentGroupId;
}
