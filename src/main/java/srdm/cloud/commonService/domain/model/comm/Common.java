package srdm.cloud.commonService.domain.model.comm;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Common implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1600237124662471688L;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ErrorList implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = -1160184507467102401L;

		private String errorCode;
		private String errorField;
		private String errorValue;
		private String errorMessage;
	}

	private ErrorList[] errorList;
}
