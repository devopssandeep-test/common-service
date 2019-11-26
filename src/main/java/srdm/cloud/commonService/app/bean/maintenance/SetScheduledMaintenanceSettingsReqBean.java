package srdm.cloud.commonService.app.bean.maintenance;

import java.util.List;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.cloud.commonService.validation.DateTimeFormat;
import srdm.cloud.commonService.validation.EmailAddrList;
import srdm.cloud.commonService.validation.Language;
import srdm.cloud.commonService.validation.TimeZoneId;
import srdm.common.bean.JsonBaseReqBean;

/**
 * SetScheduledMaintenanceSettingsReqBeanクラス
 *
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class SetScheduledMaintenanceSettingsReqBean extends JsonBaseReqBean {/**
	 *
	 */
	private static final long serialVersionUID = -5036815829801016224L;

	@NotBlank(message="E0011")
	@Pattern(regexp="true|false|TRUE|FALSE", message="E0011")
	private String execFlag; // Boolean

	@NotBlank(message="E0011")
	@Pattern(regexp="true|false|TRUE|FALSE", message="E0011")
	private String sendFlag; // Boolean

	private String execType;
	private List<String> weekDay;
	private List<Long> monthDate;
	private Long execDateTimestamp;
	private Long execTimeHour;
	private Long execTimeMinute;

	@TimeZoneId
	private String timeZoneId;

	@DateTimeFormat
	private String dateTimeFormat;

	@Language
	private String language;

	@EmailAddrList
	private String toAddress;

	@EmailAddrList
	private String ccAddress;

	@EmailAddrList
	private String bccAddress;
}
