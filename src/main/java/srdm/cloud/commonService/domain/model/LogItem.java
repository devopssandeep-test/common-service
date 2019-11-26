package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="item")
@XmlType(propOrder={"name", "value"})
public class LogItem implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private String name;
	private String value;

	public LogItem() {
	}

	public LogItem(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public LogItem(String name, long value) {
		this.name = name;
		this.value = Long.toString(value);
	}
}
