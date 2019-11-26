package srdm.cloud.commonService.domain.model.comm;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonResponse implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -5757712524608588773L;

	private Common common;
}
