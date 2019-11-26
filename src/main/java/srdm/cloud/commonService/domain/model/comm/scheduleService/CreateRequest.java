package srdm.cloud.commonService.domain.model.comm.scheduleService;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
public class CreateRequest implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -6453736714729697851L;

	@Getter
	@Setter
	@ToString
	static class F implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = 4585649582569352595L;
		private String variable;
		private String in;
	}

	@Getter
	@Setter
	@ToString
	static class InsertNode implements Serializable {/**
		 *
		 */
		private static final long serialVersionUID = 1738456003394164464L;
		private CreateSchedule schedule;
	}

	@Getter
	@Setter
	@ToString
	static class R implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = 4449392656058349301L;
		private InsertNode insertNode;
		private String asLastInto;
	}

	@JsonProperty("sessionId")
	private String sessionId;

	@Setter(AccessLevel.NONE)
	@JsonProperty("F")
	private F fClause;

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
	public void addRcause(CreateSchedule schedule, String asLastInto) {
		rClause = new R();
		rClause.setAsLastInto(asLastInto);
		InsertNode insertNode = new InsertNode();
		insertNode.setSchedule(schedule);
		rClause.setInsertNode(insertNode);
	}
}
