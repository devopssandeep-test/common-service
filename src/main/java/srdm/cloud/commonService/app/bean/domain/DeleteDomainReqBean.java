package srdm.cloud.commonService.app.bean.domain;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.cloud.commonService.validation.IdList;
import srdm.common.bean.JsonBaseReqBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class DeleteDomainReqBean extends JsonBaseReqBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -785642273358094659L;

	@NotNull(message="E0011")
	@Size(min=1, message="E0014")
	@IdList(field = "domainId")
	private List<String> domainIdList;
}
