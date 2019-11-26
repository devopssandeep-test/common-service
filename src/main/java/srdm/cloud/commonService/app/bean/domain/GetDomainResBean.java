package srdm.cloud.commonService.app.bean.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import srdm.common.bean.BaseBean;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper = false)
public class GetDomainResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -8634538414591662795L;

	@Getter
	@ToString
	private static class Permission extends BaseBean {
		/**
		 *
		 */
		private static final long serialVersionUID = 1079020004998331677L;

		private String permissionName;
		private String attribute;

	}

	private String domainId;
	private String domainName;
	private String targetGroupId;
	private String targetGroupName;
	
	private String httpURL;
	private String httpsURL;

}
