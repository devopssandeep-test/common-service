package srdm.cloud.commonService.app.bean.setting;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetProtocolSettingResBean extends JsonBaseResBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6803601312611474667L;
	
	private String http;
	private String https;
	private String serverIP;
	private String landingModule;
}
