package srdm.cloud.commonService.app.bean.account;

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
public class GetAccountListResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = 404980384489706107L;

	@ToString
	@Getter
	@Setter
	private static class Account extends BaseBean {

		/**
		 *
		 */
		private static final long serialVersionUID = -924386336623865687L;

		private String accountId;
		private String accountName;
		private String roleName;
		private String accountStatus;
		private boolean canDelete;

		protected Account(String accountId, String accountName, String roleName,
				String accountStatus, boolean canDelete) {
			this.accountId = accountId;
			this.accountName = accountName;
			this.roleName = roleName;
			this.accountStatus = accountStatus;
			this.canDelete = canDelete;
		}
	}

	private long startIndex;
	private long count;
	private long resultCount;
	private long totalCount;
	private String domainId;
	private List<Account> accountList = new ArrayList<Account>();

	public void addAccount(String accountId, String accountName, String roleName, String accountStatus, boolean canDelete) {
		accountList.add(new Account(accountId, accountName, roleName, accountStatus, canDelete));
	}
}
