package srdm.cloud.commonService.app.bean.setting;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetSmtpSettingResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 1934818992581726075L;

	private String smtpHost;
	private String smtpPort;
	private String useAuth;
	private String userName;
	private String password;
	private String fromAddress;
	private String useSSL;
}
