package srdm.cloud.commonService.domain.model.comm.scheduleService;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import srdm.cloud.commonService.domain.model.comm.Common;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetResponse implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -655675506508200716L;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ResultData implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = -6718584160167332765L;
		private Schedule schedule;
	}

	private Common common;
	private List<ResultData> resultData;
}
