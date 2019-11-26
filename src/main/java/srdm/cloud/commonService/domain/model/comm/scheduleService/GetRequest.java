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
public class GetRequest implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -8540741118681526681L;

	@Getter
	@Setter
	@ToString
	static class F implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = -462603383872403472L;
		private String variable;
		private String in;
	}

	@JsonProperty("sessionId")
	private String sessionId;

	@Setter(AccessLevel.NONE)
	@JsonProperty("F")
	private F fClause;

	@JsonProperty("R")
	private String rClause;

	@JsonIgnore
	public void addFcause(String variable, String in) {
		fClause = new F();
		fClause.setVariable(variable);
		fClause.setIn(in);
	}
}
