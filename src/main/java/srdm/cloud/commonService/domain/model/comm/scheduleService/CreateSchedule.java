package srdm.cloud.commonService.domain.model.comm.scheduleService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateSchedule implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 4844426767899797878L;

	private String scheduleName;
	private String groupId;
	private String timeZone;
	private String systemId;
	private String status;
	private boolean noWait;
	private String externalApiPath;
	private String externalApiJsonParam;
	private String runStartDate;

	@ToString
	@Getter
	@Setter
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class RunDate implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = -6218312835188283540L;
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

	@ToString
	@Getter
	@Setter
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class RunTime implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = 6131987687649865269L;
		private String select;
		private Long periodHour;
		private Long periodMinute;

		@ToString
		@Getter
		@Setter
		@JsonInclude(JsonInclude.Include.NON_NULL)
		public static class TimeList implements Serializable {
			/**
			 *
			 */
			private static final long serialVersionUID = 7413084959570537758L;

			@ToString
			@Getter
			@Setter
			@JsonInclude(JsonInclude.Include.NON_NULL)
			public static class Time implements Serializable {
				/**
				 *
				 */
				private static final long serialVersionUID = -6633314336426930663L;
				private Long hour;
				private Long minute;
			}

			private Time time;
			public TimeList(Long hour, Long minute) {
				Time time = new Time();
				time.setHour(hour);
				time.setMinute(minute);
				this.time = time;
			}
		}

		@JsonInclude(JsonInclude.Include.NON_NULL)
		private List<TimeList> timeList = new ArrayList<TimeList>();

		@JsonIgnore
		public void addTime(Long hour, Long minute) {
			TimeList tL = new TimeList(hour, minute);
			timeList.add(tL);
		}
	}
	private RunTime runTime;

	@ToString
	@Getter
	@Setter
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class LoginInfo implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = 1855163103897038910L;
		private String domainId;
		private String accountId;
	}
	private LoginInfo loginInfo;
	private Boolean execInStartingTimeFlag;

	@JsonIgnore
	public CreateSchedule() {
		this.runDate = new RunDate();
		this.runTime = new RunTime();
		this.loginInfo = new LoginInfo();
	}

	public void addRunTimeTimeList(Long hour, Long minute) {
		runTime.addTime(hour, minute);
	}
}
