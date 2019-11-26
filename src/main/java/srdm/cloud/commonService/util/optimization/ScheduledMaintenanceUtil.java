package srdm.cloud.commonService.util.optimization;


import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.Data;
@Data
@JsonRootName("scheduledMaintenanceInfo")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduledMaintenanceUtil implements Serializable{
	/**
	 *
	 */
	private static final long serialVersionUID = 6977143943025951626L;

//	@JsonProperty
//	public String scheduleId;
	@JsonProperty
	public Boolean execFlag;
	@JsonProperty
	public boolean sendFlag;
	@JsonProperty
	public String execType;
	@JsonProperty
	public String[] weekDay;
	@JsonProperty
	public String monthDate;
	@JsonProperty
	public Long execDateTimestamp;
	@JsonProperty
	public Long execTimeHour;
	@JsonProperty
	public Long execTimeMinute;
	@JsonProperty
	public String timeZone;
	@JsonProperty
	public String dateTimeFormat;
	@JsonProperty
	public String language;
	@JsonProperty
	public String toAddress;
	@JsonProperty
	public String ccAddress;
	@JsonProperty
	public String bccAddress;
}
