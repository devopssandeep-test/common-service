package srdm.cloud.commonService.app.bean.log;

import java.io.Serializable;

import lombok.Data;

@Data
public class Item implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1059477774009581763L;

	private String name;
	private String value;
}
