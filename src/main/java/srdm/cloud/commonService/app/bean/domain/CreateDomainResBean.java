package srdm.cloud.commonService.app.bean.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class CreateDomainResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 8185179693829405841L;

	private String domainId;
}
