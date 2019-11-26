package srdm.cloud.commonService.testNoDB;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import javax.servlet.http.Cookie;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import srdm.cloud.commonService.app.config.AppConfig;
import srdm.cloud.commonService.config.TestNoDBWebMvcConfig;
import srdm.cloud.commonService.constants.TestConstants.TestAccount;
import srdm.cloud.commonService.constants.TestConstants.TestDomain;
import srdm.cloud.commonService.constants.TestConstants.TestHomeGroup;
import srdm.cloud.commonService.constants.TestConstants.TestRole;
import srdm.cloud.commonService.util.AccountManagerForTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextHierarchy({
	@ContextConfiguration(classes = AppConfig.class),
	@ContextConfiguration(classes = TestNoDBWebMvcConfig.class)
})

@WebAppConfiguration
public class AccountTestNoDB {

	@Autowired
	WebApplicationContext context;

	MockMvc mockMvc;

	// Developer
	private String loginDomainId = TestDomain.developerDomainId;
	// developer
	private String accountId = TestAccount.developerAccountId;
	private String targetGroupId = TestHomeGroup.homeGroup100100;
	private String[] permissionNameList = TestRole.developerRolePermissionList;

	private Cookie cookie;

	@Before
	public void setupMockMvc(){
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}


	/***
	 * SRDM Common Service Communication API Specification 3.1.1
	 * アカウントリスト情報取得テスト getAccountListTest
	 * @throws Exception
	 */
	@Test
	public void getAccountListTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		long startIndex = 1;
		long count = 30;
		String domainId = "\"D-4dddb97a-3272-490f-b0fb-8762eaa69d43\"";
		String simpleFilterKey = "\"accountName\"";
		String simpleFilterValue = "\"Test\"";
		String orderByKey = "\"accountName\"";
		String order = "\"descending\"";

		/* テストケース1 accountName:test でフィルタリング	accountName:testUser[ X ] が返ってくる X...数字
		 * 30件取得 */
		System.out.println("* * * * getAccountListTest:Normal Case:1 * * * *");
		mockMvc.perform(post("/account/getAccountList/")
				.cookie(cookie)
				.content("{ \"startIndex\":\""+ startIndex + "\"" +
						  ",\"count\":\""+ count + "\"" +
						  ",\"domainId\":"+ domainId +
						  ",\"simpleFilter\":[{ \"key\":"+ simpleFilterKey +
						  ",\"value\":" + simpleFilterValue + "}]" +
						  ",\"orderBy\":[{ \"key\":" + orderByKey +
						  ",\"order\":" + order + "}]" +
				"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());

		accountManager.logout();
	}

	/***
	 * SRDM Common Service Communication API Specification 3.1.2
	 * アカウント作成テスト createAccountTest
	 * @throws Exception
	 */
	@Test
	public void createAccountTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		String name = "\"testUser9\"";
		String password = "\"TestUser77793711\"";
		String domainId = "\"" + TestDomain.topDomainId + "\"";
		String roleId = "\"" + TestRole.userRoleId + "\"";
		String dateTimeFormat = "\"MM/dd/yyyy HH:mm:ss\"";
		String timeZoneSpecifingType = "\"manual\"";
		String timeZoneId = "\"Etc/GMT+0\"";

		/* テストケース1 */
		// permanentAccount, privateRoleにtrueを指定
		System.out.println("* * * * createAccountTest:Normal Case:1 * * * *");
		mockMvc.perform(post("/account/createAccount/")
			.cookie(cookie)		//Cookieの設定
			.content("{ \"accountName\":"+ name +
					",\"isPermanentAccount\":\"true\"" +
					",\"password\":"+ password +
					",\"roleId\":"+ roleId +
					",\"isPrivateRole\":\"false\"" +
					",\"domainId\":"+ domainId +
					",\"language\":\"en\"" +
					",\"dateTimeFormat\":"+ dateTimeFormat +
					",\"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
					",\"timeZoneId\":"+ timeZoneId +
					",\"homeGroupId\":\"100100\"" +
			"}")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
			.andDo(print())
			.andExpect(jsonPath("$.accountId").exists());

		accountManager.logout();

	}

	/***
	 * SRDM Common Service Communication API Specification 3.1.3
	 * アカウント情報取得テスト getAccountTest
	 * @throws Exception
	 */
	@Test
	public void getAccountTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* テストケース1 */
		System.out.println("* * * * getAccountTest:Normal Case:1 * * * *");
		mockMvc.perform(post("/account/getAccount/")
			.cookie(cookie)		//Cookieの設定
			.content("{ \"accountId\":\"A-4bd42a8f-c390-4986-b142-94f71fb5a822\"}")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(jsonPath("$.accountId").value("A-d30a67bc-317a-4c60-b9b8-82446e839183"))
			.andExpect(jsonPath("$.accountName").value("developer"))
			.andExpect(jsonPath("$.isPermanentAccount").isNotEmpty())
			.andExpect(jsonPath("$.domainId").value("D-51a29222-8f56-499e-849c-4e66968c316c"))
			.andExpect(jsonPath("$.roleId").value("R-bb5bdf35-0d87-44ea-86e3-4e56336ce398"))
			.andExpect(jsonPath("$.isPrivateRole").isNotEmpty())
			.andExpect(jsonPath("$.language").value("en"))
			.andExpect(jsonPath("$.accountStatus").value("active"))
			.andExpect(jsonPath("$.dateTimeFormat").value("MM/dd/yyyy HH:mm:ss"))
			.andExpect(jsonPath("$.timeZoneSpecifingType").value("auto"))
			.andExpect(jsonPath("$.timeZoneId").value(""))
			.andExpect(jsonPath("$.homeGroupId").value("0"))
			.andDo(print());

		accountManager.logout();

	}

	/***
	 * SRDM Common Service Communication API Specification 3.1.4
	 * アカウント情報更新テスト editAccountTest
	 * @throws Exception
	 */
	@Test
	public void editAccountTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		String accountId = "\"A-4bd42a8f-c390-4986-b142-94f71fb5a822\"";
		String accountName = "\"testUser_update\"";
		String changePasswordFlag = "\"true\"";
		String password = "\"Admin77793711\"";
		String roleId = "\"R-c0c9c026-ba24-4698-8ccb-ff42ed8a26f2\"";
		String language = "\"en\"";
		String dateTimeFormat = "\"MM/dd/yyyy HH:mm:ss\"";
		String timeZoneSpecifingType = "\"auto\"";
		String timeZoneId = "\"\"";	//autoの場合は空文字("")
		String loginPassword = "\"Admin000\"";

		/* テストケース1 */
		// permanentAccount, privateRoleにtrueを指定
		// 同一ドメイン（アカウント）での呼出しのためE0024が返却される(true)のチェックは通過
		System.out.println("* * * * editAccountTest:Normal Case:1 * * * *");
		mockMvc.perform(post("/account/editAccount/")
				.cookie(cookie)
				.content("{ \"accountId\":"   + accountId +
						 ", \"accountName\":" + accountName +
						 ", \"permanentAccount\":\"true\"" +
						 ", \"changePasswordFlag\":" + changePasswordFlag +
						 ", \"password\":"+ password +
						 ", \"loginAccountPassword\":"+ loginPassword +
						 ", \"roleId\":" + roleId +
						 ", \"privateRole\":\"true\"" +
						 ", \"language\":" + language +
						 ", \"dateTimeFormat\":"+ dateTimeFormat +
						 ", \"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
						 ", \"timeZoneId\":"+ timeZoneId +
						 ",\"homeGroupId\":\"100100\"" +
						"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0024"));

		/* テストケース2 */
		System.out.println("* * * * editAccountTest:Normal Case:2 * * * *");
		mockMvc.perform(post("/account/editAccount/")
				.cookie(cookie)
				.content("{ \"accountId\":"   + accountId +
						 ", \"accountName\":" + accountName +
						 ", \"permanentAccount\":\"false\"" +
						 ", \"changePasswordFlag\":" + changePasswordFlag +
						 ", \"password\":"+ password +
						 ", \"loginAccountPassword\":"+ loginPassword +
						 ", \"roleId\":" + roleId +
						 ", \"privateRole\":\"false\"" +
						 ", \"language\":" + language +
						 ", \"dateTimeFormat\":"+ dateTimeFormat +
						 ", \"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
						 ", \"timeZoneId\":"+ timeZoneId +
						 ",\"homeGroupId\":\"100100\"" +
						"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());

		accountManager.logout();
	}


	/***
	 * SRDM Common Service Communication API Specification 3.1.5
	 * アカウント削除テスト deleteAccountTest
	 * @throws Exception
	 */
	@Test
	public void deleteAccountTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* テストケース1 testUserAccount1件削除*/
		System.out.println("* * * * deleteAccountTest:Normal Case:1 * * * *");
		mockMvc.perform(post("/account/deleteAccount/")
				.cookie(cookie)
				.content("{\"accountIdList\":[\"A-8cc64ec5-0848-4444-b435-4c77277798b4\"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());



		/* テストケース2 testUserAccount複数削除*/
		System.out.println("* * * * deleteAccountTest:Normal Case:2 * * * *");
		mockMvc.perform(post("/account/deleteAccount/")
				.cookie(cookie)
				.content("{\"accountIdList\":[\"A-8cc64ec5-0848-4444-b435-4c77277798b4\",\"A-95eb9606-c35a-4fc0-9f9e-f1184f24cb8b\"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());

		accountManager.logout();

	}

	/***
	 * SRDM Common Service Communication API Specification 3.1.6
	 * アカウントロック解除テスト accountUnlockTest
	 * パラメータのテストのみなので、ユーザ操作によるアカウントロックの解除のテストは実行しない
	 *
	 * @throws Exception
	 */
	@Test
	public void accountUnlockTest() throws Exception{

		String accountId = "\"A-" + UUID.randomUUID().toString() +"\"";
		String accountId2 = "\"A-"+ UUID.randomUUID().toString() +"\"";

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* テストケース1 Account2件ロック解除*/
		System.out.println("* * * * accountUnlockTest:Normal Case:1 * * * *");
		mockMvc.perform(post("/account/accountUnlock/")
				.cookie(cookie)
				.content("{\"accountIdList\":["+ accountId + "," + accountId2 +"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());

		accountManager.logout();

	}

	/***
	 * SRDM Common Service Communication API Specification 3.1.7
	 * ログインアカウント権限取得テスト getAccountPermissionTest
	 * @throws Exception
	 */
	@Test
	public void getAccountPermission() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* テストケース1 */
		System.out.println("* * * * getAccountPermissionTest:Normal Case:1 * * * *");
		mockMvc.perform(post("/account/getAccountPermission/")
				.cookie(cookie)
				.content("")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());

		accountManager.logout();

	}


	/***
	 * SRDM Common Service Communication API Specification 3.1.8
	 * アカウント情報メール通知テスト setEmailNortifyTest
	 * @throws Exception
	 */
	// テスト時にネットワーク接続設定で例外が発生するためテストは未実施
//	@Test
	public void setEmailNortifyTest() throws Exception{
		String toAddress = "\"testUser2@mail.com\"";
		String ccAddress = "\"testUser2@mail.com\"";
		String bccAddress = "\"\"";
		String subject = "\"MailTitle\"";
		String body = "\"Content string\"";

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* テストケース1 setEmailNortifyTest */
		System.out.println("* * * * setEmailNortifyTestt:Normal Case:1 * * * *");
		mockMvc.perform(post("/account/setEmailNortify/")
				.cookie(cookie)
				.content("{ \"toAddress\":" + toAddress +
						  ",\"ccAddress\": "+ ccAddress +
						  ",\"bccAddress\":" + bccAddress +
						  ",\"subject\":" + subject +
						  ",\"body\":" + body + "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());


		/* テストケース2 アドレス複数指定 setEmailNortifyTest */
		System.out.println("* * * * setEmailNortifyTestt:Normal Case:2 * * * *");

		toAddress = "\"testUser2@mail.com;testUser3@mail.com\"";
		ccAddress = "\"testUser4@mail.com;testUser8@mail.com\"";
		bccAddress = "\"testUser5@mail.com;testUser6@mail.com\"";

		mockMvc.perform(post("/account/setEmailNortify/")
				.cookie(cookie)
				.content("{ \"toAddress\":" + toAddress +
						  ",\"ccAddress\": "+ ccAddress +
						  ",\"bccAddress\":" + bccAddress +
						  ",\"subject\":" + subject +
						  ",\"body\":" + body + "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());


		accountManager.logout();

	}

	/***
	 * SRDM Common Service Communication API Specification 3.1.9
	 * ログインアカウント情報取得テスト getLoginAccountInfoTest
	 * @throws Exception
	 */
	@Test
	public void getLoginAccountInfo() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* テストケース1 */
		System.out.println("* * * * getLoginAccountInfoTest:Normal Case:1 * * * *");
		mockMvc.perform(post("/account/getLoginAccountInfo/")
				.cookie(cookie)
				.content("")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());

		accountManager.logout();

	}

	/***
	 * SRDM Common Service Communication API Specification 3.6.2
	 * ログイン状態取得テスト getLoginStatusTest
	 * @throws Exception
	 */
	@Test
	public void getLoginStatusTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* テストケース1 */
		System.out.println("* * * * getLoginStatusTest:Normal Case:1 * * * *");
		mockMvc.perform(post("/account/getLoginStatus/")
				.cookie(cookie)
				.content("")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.loginFlg").value("true"))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty())
				.andExpect(jsonPath("$.loginFlg").isNotEmpty());

		accountManager.logout();

	}

	/***
	 * SRDM Common Service Communication API Specification 3.6.3
	 * ログアウトテスト getAccountListTest
	 * @throws Exception
	 */
	@Test
	public void logoutTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* テストケース1 */
		System.out.println("* * * * logoutTest:Normal Case:1 * * * *");
		mockMvc.perform(post("/account/logout/")
				.cookie(cookie)
				.content("")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());


	}

	/***
	 * SRDM Common Service Communication API Specification 3.6.4
	 * ログイン認証テスト loginForAgentTest
	 * @throws Exception
	 */
	@Test
	public void loginForAgentTest() throws Exception{

		/* テストケース1 */
		System.out.println("* * * * loginForAgentTest:Normal Case:1 * * * *");
		mockMvc.perform(post("/account/loginForAgent/")
				.content("{ \"domainId\":\"" + loginDomainId + "\"" +
						  ",\"accountName\":\"" + TestAccount.adminAccountName + "\"" +
						  ",\"password\":\"Admin000\"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty())
				.andExpect(jsonPath("$.validateResultFlg").value("true"))
				.andExpect(jsonPath("$.sessionId").isNotEmpty());
	}


	/***
	 * SRDM Common Service Communication API Specification 3.6.5
	 * ログイン認証スケジュールテスト loginForScheduleTest
	 * @throws Exception
	 */
	@Test
	public void loginForSchedule() throws Exception{

		/* テストケース1 */
		System.out.println("* * * * loginForScheduleTest:Normal Case:1 * * * *");
		mockMvc.perform(post("/account/loginForSchedule/")
				.content("{ \"domainId\":\"" + loginDomainId + "\"" +
						  ",\"accountId\":\"" + accountId + "\"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty())
				.andExpect(jsonPath("$.validateResultFlg").value("true"))
				.andExpect(jsonPath("$.sessionId").isNotEmpty());
	}

	/***
	 * SRDM Common Service Communication API Specification 3.6.6
	 * ログアウトスケジュールテスト logoutForScheduleTest
	 * @throws Exception
	 */
	@Test
	public void LogoutForScheduleTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* テストケース1 */
		System.out.println("* * * * logoutForScheduleTest:Normal Case:1 * * * *");
		mockMvc.perform(post("/account/logoutForSchedule/")
				.cookie(cookie)
				.content("")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());

		accountManager.logout();

	}


	/***
	 * SRDM Common Service Communication API Specification 2.5.0
	 * アカウントロックテスト accountLockTest
	 * @throws Exception
	 */
	@Test
	public void accountLockTest() throws Exception{

		String accountId = "\"A-" + UUID.randomUUID().toString() +"\"";
		String accountId2 = "\"A-"+ UUID.randomUUID().toString() +"\"";

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* テストケース1 Account1件ロック*/
		System.out.println("* * * * accountLockTest:Normal Case:1 * * * *");
		mockMvc.perform(post("/account/accountLock/")
				.cookie(cookie)
				.content("{\"accountIdList\":["+ accountId + "]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print());

		/* テストケース1 Account2件ロック*/
		System.out.println("* * * * accountLockTest:Normal Case:2 * * * *");
		mockMvc.perform(post("/account/accountLock/")
				.cookie(cookie)
				.content("{\"accountIdList\":["+ accountId + "," + accountId2 +"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print());

		accountManager.logout();

	}

}