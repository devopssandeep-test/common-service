package srdm.cloud.commonService.app.bean.setting;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetDeleteSystemLogScheduleResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private long period;
	private long startHour;
	private long startMinute;
	private boolean execFlag;
}
