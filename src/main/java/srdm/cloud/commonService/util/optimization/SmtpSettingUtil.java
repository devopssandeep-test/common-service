package srdm.cloud.commonService.util.optimization;


import java.io.StringReader;

import javax.xml.bind.JAXB;

import srdm.cloud.commonService.domain.model.SmtpSetting;

public class SmtpSettingUtil {
	private static final String Header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	/**
	 * iPAUSysSettingsDBのSmtpSettingUtilノード<br/>
	 * (値参照用)
	 *
	 * @param mWorkXmlString
	 * @return SmtpSettingInfoクラスのインスタンス
	 */
	public SmtpSetting getSmtpSettingUtil(final String xml) {
		return JAXB.unmarshal(new StringReader(Header + xml),  SmtpSetting.class);
	}


}
