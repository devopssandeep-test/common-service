package srdm.cloud.commonService.app.bean.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import srdm.common.bean.BaseBean;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetDomainListResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -3772572251643240813L;

	@ToString
	@Getter
	@Setter
	private static class Domain extends BaseBean {
		/**
		 *
		 */
		private static final long serialVersionUID = 67653772722820852L;

		private String domainId;
		private String domainName;
		private String parentDomainId;

		protected Domain(String domainId, String domainName, String parentDomainId) {
			this.domainId = domainId;
			this.domainName = domainName;
			this.parentDomainId = parentDomainId;
		}
	}

	private long startIndex;
	private long count;
	private long resultCount;
	private long totalCount;
	private String domainId;

	List<Domain> domainList = new ArrayList<Domain>();

	public void addDomain(String domainId, String domainName, String parentDomainId) {
		domainList.add(new Domain(domainId, domainName, parentDomainId));
	}
}
