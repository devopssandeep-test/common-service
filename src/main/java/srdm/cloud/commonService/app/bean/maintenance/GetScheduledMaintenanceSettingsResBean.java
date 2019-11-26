package srdm.cloud.commonService.app.bean.maintenance;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseResBean;

/**
 * GetScheduledMaintenanceSettingsResBeanクラス
 *
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class GetScheduledMaintenanceSettingsResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -347446108769234033L;

	private Boolean execFlag;
	private Boolean sendFlag;
	private String execType;
	private List<String> weekDay;
	private List<Long> monthDate;
	private Long execDateTimestamp;
	private Long execTimeHour;
	private Long execTimeMinute;
	private String timeZoneId;
	private String dateTimeFormat;
	private String language;
	private String toAddress;
	private String ccAddress;
	private String bccAddress;
}
