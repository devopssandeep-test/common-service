package srdm.cloud.commonService.app.bean.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetThemeSettingResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -8458147649343172966L;

	private String theme;
}
