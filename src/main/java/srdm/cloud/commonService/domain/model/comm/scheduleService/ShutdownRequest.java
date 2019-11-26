package srdm.cloud.commonService.domain.model.comm.scheduleService;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
public class ShutdownRequest implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -4002153343510288136L;

	@JsonProperty("sessionId")
	private String sessionId;

}
