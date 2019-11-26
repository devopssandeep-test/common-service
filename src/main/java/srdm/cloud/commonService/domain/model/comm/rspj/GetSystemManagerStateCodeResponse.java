package srdm.cloud.commonService.domain.model.comm.rspj;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import srdm.cloud.commonService.domain.model.comm.Common;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetSystemManagerStateCodeResponse implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -8260611843209559960L;

	private Common common;
	private String systemManagerStateCode;
}
