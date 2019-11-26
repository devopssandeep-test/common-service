package srdm.cloud.commonService.domain.model.comm.scheduleService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
public class StartStopRequest implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -5363346519916603842L;

	@Getter
	@Setter
	@ToString
	static class ScheduleId implements Serializable {

		/**
		 *
		 */
		private static final long serialVersionUID = -9220581650737882572L;

		private String scheduleId;
	}

	@JsonProperty("sessionId")
	private String sessionId;

	@JsonProperty("scheduleIds")
	List<ScheduleId> scheduleIds = new ArrayList<ScheduleId>();

	@JsonIgnore
	public void addScheduleId(String id) {
		ScheduleId scheduleId = new ScheduleId();
		scheduleId.setScheduleId(id);
		scheduleIds.add(scheduleId);
	}
}
