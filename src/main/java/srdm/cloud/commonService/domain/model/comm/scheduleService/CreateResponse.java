package srdm.cloud.commonService.domain.model.comm.scheduleService;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import srdm.cloud.commonService.domain.model.comm.Common;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateResponse implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 4342884543958230173L;

	private Common common;
	private String scheduleId;
}
