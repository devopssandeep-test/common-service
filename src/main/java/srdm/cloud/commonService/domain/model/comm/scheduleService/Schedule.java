package srdm.cloud.commonService.domain.model.comm.scheduleService;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import srdm.cloud.commonService.domain.model.comm.Common;
import srdm.cloud.commonService.domain.model.comm.scheduleService.GetResponse.ResultData;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Schedule implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1908563458658824634L;

	private String scheduleId;
	private String scheduleName;
	private Long groupId;
	private String timeZone;
	private String systemId;
	private String status;
	private boolean noWait;
	private String externalApiPath;
	private String externalApiJsonParam;
	private String runStartDate;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class RunDate implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = -4746664444279128747L;

		private String select;
		private Long period;
		private boolean sunday;
		private boolean monday;
		private boolean tuesday;
		private boolean wednesday;
		private boolean thursday;
		private boolean friday;
		private boolean saturday;

		private Long[] day;
	}

	private RunDate runDate;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class RunTime implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = 4324517017969526605L;

		private String select;
		private Long periodHour;
		private Long periodMinute;

		@Data
		@JsonIgnoreProperties(ignoreUnknown = true)
		@JsonInclude(JsonInclude.Include.NON_NULL)
		public static class TimeList implements Serializable {
			/**
			 *
			 */
			private static final long serialVersionUID = 5412211945623544314L;
			
			@Data
			@JsonIgnoreProperties(ignoreUnknown = true)
			@JsonInclude(JsonInclude.Include.NON_NULL)
			public static class Time implements Serializable {
				/**
				 *
				 */
				private static final long serialVersionUID = -7429945229644564249L;

				private Long hour;
				private Long minute;
			}
			private Time time;
		}
		private TimeList[] timeList;
	}
	private RunTime runTime;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class LoginInfo implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = -8413890521586624346L;

		private String domainId;
		private String accountId;
	}
	private LoginInfo loginInfo;
	private Boolean execInStartingTimeFlag;
}
