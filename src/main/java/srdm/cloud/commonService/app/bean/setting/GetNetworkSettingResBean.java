package srdm.cloud.commonService.app.bean.setting;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetNetworkSettingResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -3816827653190055471L;

	private String ipAddressEnable;
	private String ipAddress;
	private String httpPortEnable;
	private String httpPort;
	private String httpsPortEnable;
	private String httpsPort;
	private String tunnelPortEnable;
	private String tunnelPort;
}
