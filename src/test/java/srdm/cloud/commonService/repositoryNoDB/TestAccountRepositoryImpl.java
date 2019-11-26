package srdm.cloud.commonService.repositoryNoDB;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import srdm.cloud.commonService.domain.model.Account;
import srdm.cloud.commonService.domain.model.EditAccount;
import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.SimpleAccount;
import srdm.cloud.commonService.domain.repository.account.AccountRepository;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

@Repository
public class TestAccountRepositoryImpl implements AccountRepository {

	//private static final Logger logger = LoggerFactory.getLogger(AccountRepositoryImpl.class);

	/**
	 * アカウント情報の取得（単一）
	 * （accountStatusが"deleted"のものは除く）
	 *
	 * @throws SrdmDataAccessException
	 * @throws SrdmDataNotFoundException
	 */
	@Override
	public Account findOne(String accountId) throws SrdmDataAccessException, SrdmDataNotFoundException {

		Account account = new Account();
		account.setAccountId("A-d30a67bc-317a-4c60-b9b8-82446e839183");
		account.setAccountType("local");
		account.setAccountName("developer");
		account.setPermanentAccount(false);
		account.setPassword("developer77793711");
		account.setDomainId("D-51a29222-8f56-499e-849c-4e66968c316c");
		account.setRoleId("R-bb5bdf35-0d87-44ea-86e3-4e56336ce398");
		account.setTimeZoneSpecifingType("auto");
		account.setLanguage("en");
		account.setDateTimeFormat("MM/dd/yyyy HH:mm:ss");
		account.setHomeGroupId("0");
		account.setAccountStatus("active");
		account.setErrorCount(0);
		account.setLatestErrorTimestamp(0);
		account.setTimeZone("");
		return account;

	}

	/**
	 * domainId, accountNameをキーにアカウント情報取得（単一）
	 *（accountStatusが"deleted"を含まない）
	 * @throws SrdmDataNotFoundException
	 * @throws SrdmDataAccessException
	 */
	@Override
	public Account findOneByName(String domainId, String accountName) throws SrdmDataNotFoundException, SrdmDataAccessException {

		Account account = new Account();
		account.setAccountId("A-d30a67bc-317a-4c60-b9b8-82446e839183");
		account.setAccountType("local");
		account.setAccountName("developer");
		account.setPermanentAccount(false);
		account.setPassword("developer77793711");
		account.setDomainId("D-51a29222-8f56-499e-849c-4e66968c316c");
		account.setRoleId("R-bb5bdf35-0d87-44ea-86e3-4e56336ce398");
		account.setTimeZoneSpecifingType("auto");
		account.setLanguage("en");
		account.setDateTimeFormat("MM/dd/yyyy HH:mm:ss");
		account.setHomeGroupId("100100");
		account.setAccountStatus("active");
		account.setErrorCount(0);
		account.setLatestErrorTimestamp(0);


		return account;
	}

	/**
	 * アカウントの件数を返す
	 * （accountStatusが"deleted"のものは、除く）
	 *
	 * @throws SrdmDataNotFoundException
	 * @throws SrdmDataAccessException
	 */
	@Override
	public String count(GetListReq getListReq) throws SrdmDataNotFoundException, SrdmDataAccessException {

		String count = "3";

		return count;
	}

	/**
	 * アカウントリスト取得
	 * （accountStatusが"deleted"のものは除く)
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public List<SimpleAccount> findAllWithPagable(GetListReq getListReq) throws SrdmDataAccessException {

		List<SimpleAccount> list = new ArrayList<>();
		SimpleAccount account = new SimpleAccount();
		account.setAccountId("A-d30a67bc-317a-4c60-b9b8-82446e839183");
		account.setAccountName("developer");
		account.setAccountStatus("active");
		account.setCanDelete(false);
		account.setRoleName("developerRole");
		list.add(account);
		list.add(account);
		list.add(account);

		return list;
	}

	/**
	 * アカウントの追加
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public String create(Account account) throws SrdmDataAccessException {

		// accountId Set
		String id = "A-d30a67bc-317a-4c60-b9b8-82446e839183";

		return id;
	}

	/**
	 * アカウントの存在チェック
	 * （accountStatusが"deleted"のものは、存在しないアカウントとして扱う）
	 * true:指定アカウントが存在／false:指定アカウントが存在しない。
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public boolean isExist(String accountId) throws SrdmDataAccessException {

		boolean bRet = true;

		return bRet;
	}

	/**
	 * 同一アカウント名のチェック
	 * excludeAccountIdに指定したaccountIdを除いてチェックする。
	 * （アカウント編集時、自身のアカウント名を除くために使用）
	 * （accountStatusが"deleted"のものはチェック対象としない）
	 * true:同一名のアカウントが存在／false:同一名のアカウントが存在しない
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public boolean isExistAccoutName(String domainId, String accountName, String exculdeAccountId) throws SrdmDataAccessException {

		boolean bRet = false;

		return bRet;
	}

	/**
	 * アカウント情報の更新
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public void update(EditAccount account) throws SrdmDataAccessException {

	}

	/**
	 * 認証エラー情報設定
	 * @throws SrdmDataAccessException
	 */
	@Override
	public void setAuthErrorInfo(String accountId) throws SrdmDataAccessException {

	}

	/**
	 * 最新認証エラー日時更新
	 */
	@Override
	public void updateLatestErrorTimestamp(String accountId) throws SrdmDataAccessException {

	}

	/**
	 * 認証エラー情報クリア
	 */
	@Override
	public void clearAuthErrorInfo(List<String> accountIdList) throws SrdmDataAccessException {

	}


	/**
	 * パスワードチェック
	 */
	@Override
	public boolean checkAuth(String accountId, String rawPassword) throws SrdmDataNotFoundException, SrdmDataAccessException {

		boolean bRet = true;

		return bRet;
	}

	/**
	 * アカウント削除（accountStatusを"deleted"に変更）
	 */
	@Override
	public void updateAccountStatusToDeleted(List<String> accountIdList) throws SrdmDataAccessException {

	}

	/**
	 * ドメイン内のアカウントから指定roleIdを削除
	 */
	@Override
	public void clearRoleId(String domainId, List<String> roleIdList) throws SrdmDataAccessException {

	}

	/**
	 * アカウントリスト取得
	 * （accountStatusが"deleted"のものも含め全て）
	 */
	@Override
	public List<Account> findAllByDomainId(String domainId) throws SrdmDataAccessException {

		List<Account> accountList = new ArrayList<>();

		Account account = new Account();
		account.setAccountId("A-d30a67bc-317a-4c60-b9b8-82446e839183");
		account.setAccountType("local");
		account.setAccountName("developer");
		account.setPermanentAccount(false);
		account.setPassword("developer77793711");
		account.setDomainId("D-51a29222-8f56-499e-849c-4e66968c316c");
		account.setRoleId("R-bb5bdf35-0d87-44ea-86e3-4e56336ce398");
		account.setTimeZoneSpecifingType("auto");
		account.setLanguage("en");
		account.setDateTimeFormat("MM/dd/yyyy HH:mm:ss");
		account.setHomeGroupId("100100");
		account.setAccountStatus("active");
		account.setErrorCount(0);
		account.setLatestErrorTimestamp(0);
		accountList.add(account);
		accountList.add(account);
		accountList.add(account);


		return accountList;
	}

	/**
	 * アカウント削除（DBから削除。accountId指定）
	 * （未使用）
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public void delete(List<String> accountIdList) throws SrdmDataAccessException {
		// DBアクセス(スタブ)
	}

	/**
	 * アカウント削除（DBから削除。domainId指定）
	 */
	@Override
	public void deleteByDomainId(List<String> domainIdList) throws SrdmDataAccessException {
		// DBアクセス(スタブ)
	}

	@Override
	public List<Account> findAllByRoleId(String roleId) throws SrdmDataAccessException {

		// privateRoleのパラメータチェック向けのため、リストは空で返す
		List<Account> accountList = new ArrayList<>();

//		Account account = new Account();
//		account.setAccountId("A-d30a67bc-317a-4c60-b9b8-82446e839183");
//		account.setAccountType("local");
//		account.setAccountName("developer");
//		AccountAttribute accountAttribute = new AccountAttribute();
//		accountAttribute.setPermanentAccount("false");
//		account.setAccountAttribute(accountAttribute);
//		account.setPassword("developer77793711");
//		account.setDomainId("D-51a29222-8f56-499e-849c-4e66968c316c");
//		account.setRoleId(roleId);
//		account.setPrivateRole("false");
//		account.setTimeZoneSpecifingType("auto");
//		account.setLanguage("en");
//		account.setDateTimeFormat("MM/dd/yyyy HH:mm:ss");
//		account.setHomeGroupId("100100");
//		account.setAccountStatus("active");
//		account.setErrorCount(0);
//		account.setLatestErrorTimestamp(0);
//		accountList.add(account);
//		accountList.add(account);
//		accountList.add(account);

		return accountList;
	}

	/**
	 * アカウントロック(ユーザー操作)
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public void updateAccountStatusToManualLock(List<String> accountIdList) throws SrdmDataAccessException {
		// DBアクセス(スタブ)
	}

	/**
	 * アカウントロック解除(ユーザ操作によってロックされたアカウントのロック解除)
	 */
	@Override
	public void clearManualLock(List<String> accountIdList) throws SrdmDataAccessException {
		// DBアクセス(スタブ)
	}

}
