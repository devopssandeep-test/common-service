package srdm.cloud.commonService.domain.model.comm.scheduleService;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SetRequest implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 6000711744088889169L;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	static class F implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = -4147174109333943572L;
		private String variable;
		private String in;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	static class With implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = 8458498304870751753L;
		private Schedule schedule;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	static class R implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = 9039854046817745205L;
		private String replaceNode;
		private With with;
	}

	@JsonProperty("sessionId")
	private String sessionId;

	@JsonProperty("scheduleId")
	private String scheduleId;

	@Setter(AccessLevel.NONE)
	@JsonProperty("F")
	private F fClause;

	@JsonProperty("W")
	private String wCause;

	@Setter(AccessLevel.NONE)
	@JsonProperty("R")
	private R rClause;

	@JsonIgnore
	public void addFcause(String variable, String in) {
		fClause = new F();
		fClause.setVariable(variable);
		fClause.setIn(in);
	}

	@JsonIgnore
	public void addRcause(String replaceNode, Schedule schedule) {
		rClause = new R();
		rClause.setReplaceNode(replaceNode);
		With withClause = new With();
		withClause.setSchedule(schedule);
		rClause.setWith(withClause);
	}
}
