package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement(name="rspSettings")
public class RspSetting implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -8357494581469613340L;

	private String enableRsp;
}
