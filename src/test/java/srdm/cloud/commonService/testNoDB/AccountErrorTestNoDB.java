package srdm.cloud.commonService.testNoDB;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
public class AccountErrorTestNoDB {

	@Autowired
	WebApplicationContext context;

	MockMvc mockMvc;

	// Developer
	private String loginDomainId = TestDomain.developerDomainId;
	// developer
	private String accountId = TestAccount.developerAccountId;
	private String targetGroupId = TestHomeGroup.homeGroup100100;
	private String[] permissionNameList = TestRole.developerRolePermissionList;

	private String loginPassword = "\"Admin000\"";

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


		long startIndex = 1;
		long count = 10;

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* domainIdをリクエストボディにセットしない。 E0011(必須パラメータチェックエラー)が返される */
		System.out.println("* * * * getAccountListTest:E0011 Case:1 * * * *");
		mockMvc.perform(post("/account/getAccountList/")
					.cookie(cookie)
					.content("{\"startIndex\":\""+ startIndex +
							"\",\"count\":\""+ count + "\"" +
							// "\",\"domainId\":"+ loginDomainId +
							"}")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainId"));

		/* domainIdの D- を除いてみる E0014(パラメータが有効値でない)が返される */
		System.out.println("* * * * getAccountListTest:E0014 Case:1 * * * *");
		mockMvc.perform(post("/account/getAccountList/")
				.cookie(cookie)
				.content("{\"startIndex\":\""+ startIndex +
						"\",\"count\":\""+ count +
						"\",\"domainId\":\""+ loginDomainId.replaceAll("D-", "") + "\"" +
				"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainId"));


		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * exportOperationLogTest: E0021 Case:1 * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");
		errorCookie.setMaxAge(60 * 30);
		errorCookie.setPath("/");
		errorCookie.setSecure(false);

		mockMvc.perform(post("/account/getAccountList/")
				.cookie(errorCookie)
				.content("{\"startIndex\":\""+ startIndex +
						"\",\"count\":\""+ count +
						"\",\"domainId\":"+ loginDomainId +
				"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

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

		String name = "\"testUser2\"";
		String password = "\"TestUser77793711\"";
		String domainId = "\"D-4dddb97a-3272-490f-b0fb-8762eaa69d43\"";
		String roleId = "\"R-100c4c12-8f88-4fc5-9d7e-b2335e770c8a\"";
		String dateTimeFormat = "\"MM/dd/yyyy HH:mm:ss\"";
		String timeZoneSpecifingType = "\"manual\"";
		String timeZoneId = "\"Etc/GMT+0\"";

		/* domainIdをリクエストボディにセットしない。 E0011(必須パラメータチェックエラー)が返される */
		System.out.println("* * * * createAccountTest:E0011 Case:1 * * * *");
				mockMvc.perform(post("/account/createAccount/")
					.cookie(cookie)		//Cookieの設定
					.content("{ \"accountName\":"+ name +
							",\"isPermanentAccount\":\"false\"" +
							",\"password\":"+ password +
							//",\"domainId\":"+ domainId +
							",\"roleId\":"+ roleId +
							",\"isPrivateRole\":\"false\"" +
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
					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));

		/* isPermanentAccountをリクエストボディにセットしない。 E0011(必須パラメータチェックエラー)が返される */
		System.out.println("* * * * createAccountTest:E0011 Case:2 * * * *");
				mockMvc.perform(post("/account/createAccount/")
					.cookie(cookie)		//Cookieの設定
					.content("{ \"accountName\":"+ name +
							//",\"isPermanentAccount\":\"false\"" +
							",\"password\":"+ password +
							",\"domainId\":"+ domainId +
							",\"roleId\":"+ roleId +
							",\"isPrivateRole\":\"false\"" +
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
					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));

		/* isPermanentAccountの値をnullにセット。 E0011(必須パラメータチェックエラー)が返される */
		System.out.println("* * * * createAccountTest:E0011 Case:3 * * * *");
				mockMvc.perform(post("/account/createAccount/")
					.cookie(cookie)		//Cookieの設定
					.content("{ \"accountName\":"+ name +
							",\"isPermanentAccount\":null" +
							",\"password\":"+ password +
							",\"domainId\":"+ domainId +
							",\"roleId\":"+ roleId +
							",\"isPrivateRole\":\"false\"" +
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
					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));

		/* isPermanentAccountに空の値("")にセット。 E0011(必須パラメータチェックエラー)が返される */
		System.out.println("* * * * createAccountTest:E0011 Case:4 * * * *");
				mockMvc.perform(post("/account/createAccount/")
					.cookie(cookie)		//Cookieの設定
					.content("{ \"accountName\":"+ name +
							",\"isPermanentAccount\":\"\"" +
							",\"password\":"+ password +
							",\"domainId\":"+ domainId +
							",\"roleId\":"+ roleId +
							",\"isPrivateRole\":\"false\"" +
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
					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));

		/* isPermanentAccountに不正な値をセット。 E0004(JSONパースエラー)が返される */
		/**
		 * 不正な値("dummy")を指定している為、E0014が正しいと思われるが、
		 * Pattern指定では、ダブルクォートで括った場合等エラーになってしまう。
		 * そのため、現状の実装では、Patterrn指定を行っておらず、JSON⇒Objectへのマッピング時に
		 * Exceptionを検知している為。
		 */
		System.out.println("* * * * createAccountTest:E0004 Case:1 * * * *");
				mockMvc.perform(post("/account/createAccount/")
					.cookie(cookie)		//Cookieの設定
					.content("{ \"accountName\":"+ name +
							",\"isPermanentAccount\":\"dummy\"" +
							",\"password\":"+ password +
							",\"domainId\":"+ domainId +
							",\"roleId\":"+ roleId +
							",\"isPrivateRole\":\"false\"" +
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
					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0004"));

		/* permanentAccountにJSONフォーマット不正の値をセット。 E0004(JSONパースエラー)が返される */
		System.out.println("* * * * createAccountTest:E0004 Case:2 * * * *");
				mockMvc.perform(post("/account/createAccount/")
					.cookie(cookie)		//Cookieの設定
					.content("{ \"accountName\":"+ name +
							",\"isPermanentAccount\":\"false" +
							",\"password\":"+ password +
							",\"domainId\":"+ domainId +
							",\"roleId\":"+ roleId +
							",\"isPrivateRole\":\"false\"" +
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
					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0004"));

		/**
		 * isPrivateRoleに対する必須チェックを行っていない為、
		 * isPrivateRoleに対するテストはコメントアウト
		 */
//		/* isPrivateRoleをリクエストボディにセットしない。 E0011(必須パラメータチェックエラー)が返される */
//		System.out.println("* * * * createAccountTest:E0011 Case:3 * * * *");
//				mockMvc.perform(post("/account/createAccount/")
//					.cookie(cookie)		//Cookieの設定
//					.content("{ \"accountName\":"+ name +
//							",\"isPermanentAccount\":\"false\"" +
//							",\"password\":"+ password +
//							",\"domainId\":"+ domainId +
//							",\"roleId\":"+ roleId +
////							",\"isPrivateRole\":null" +
//							",\"language\":\"en\"" +
//							",\"dateTimeFormat\":"+ dateTimeFormat +
//							",\"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
//							",\"timeZoneId\":"+ timeZoneId +
//							",\"homeGroupId\":\"100100\"" +
//					"}")
//					.contentType(MediaType.APPLICATION_JSON)
//					.accept(MediaType.APPLICATION_JSON))
//					.andExpect(status().isOk())
//					.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//					.andDo(print())
//					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));
//
//		/* isPrivateRoleの値をnullにセット。 E0011(必須パラメータチェックエラー)が返される */
//		System.out.println("* * * * createAccountTest:E0011 Case:4 * * * *");
//				mockMvc.perform(post("/account/createAccount/")
//					.cookie(cookie)		//Cookieの設定
//					.content("{ \"accountName\":"+ name +
//							",\"isPermanentAccount\":\"false\"" +
//							",\"password\":"+ password +
//							",\"domainId\":"+ domainId +
//							",\"roleId\":"+ roleId +
//							",\"isPrivateRole\":null" +
//							",\"language\":\"en\"" +
//							",\"dateTimeFormat\":"+ dateTimeFormat +
//							",\"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
//							",\"timeZoneId\":"+ timeZoneId +
//							",\"homeGroupId\":\"100100\"" +
//					"}")
//					.contentType(MediaType.APPLICATION_JSON)
//					.accept(MediaType.APPLICATION_JSON))
//					.andExpect(status().isOk())
//					.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//					.andDo(print())
//					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));
//
//		/* isPrivateRoleの空の値("")をセット。 E0014(パラメータが有効値でない)が返される */
//		System.out.println("* * * * createAccountTest:E0014 Case:1 * * * *");
//				mockMvc.perform(post("/account/createAccount/")
//					.cookie(cookie)		//Cookieの設定
//					.content("{ \"accountName\":"+ name +
//							",\"isPermanentAccount\":\"false\"" +
//							",\"password\":"+ password +
//							",\"domainId\":"+ domainId +
//							",\"roleId\":"+ roleId +
//							",\"isPrivateRole\":\"\"" +
//							",\"language\":\"en\"" +
//							",\"dateTimeFormat\":"+ dateTimeFormat +
//							",\"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
//							",\"timeZoneId\":"+ timeZoneId +
//							",\"homeGroupId\":\"100100\"" +
//					"}")
//					.contentType(MediaType.APPLICATION_JSON)
//					.accept(MediaType.APPLICATION_JSON))
//					.andExpect(status().isOk())
//					.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//					.andDo(print())
//					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"));
//
//		/* privateAccountの不正な値をセット。 E0014(パラメータが有効値でない)が返される */
//		System.out.println("* * * * createAccountTest:E0014 Case:2 * * * *");
//				mockMvc.perform(post("/account/createAccount/")
//					.cookie(cookie)		//Cookieの設定
//					.content("{ \"accountName\":"+ name +
//							",\"isPermanentAccount\":\"false\"" +
//							",\"password\":"+ password +
//							",\"domainId\":"+ domainId +
//							",\"roleId\":"+ roleId +
//							",\"isPrivateRole\":\"dummy\"" +
//							",\"language\":\"en\"" +
//							",\"dateTimeFormat\":"+ dateTimeFormat +
//							",\"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
//							",\"timeZoneId\":"+ timeZoneId +
//							",\"homeGroupId\":\"100100\"" +
//					"}")
//					.contentType(MediaType.APPLICATION_JSON)
//					.accept(MediaType.APPLICATION_JSON))
//					.andExpect(status().isOk())
//					.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//					.andDo(print())
//					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"));
//
//		/* privateAccountにJSONフォーマット不正の値をセット。 E0004(JSONパースエラー)が返される */
//		System.out.println("* * * * createAccountTest:E0014 Case:3 * * * *");
//				mockMvc.perform(post("/account/createAccount/")
//					.cookie(cookie)		//Cookieの設定
//					.content("{ \"accountName\":"+ name +
//							",\"isPermanentAccount\":\"false\"" +
//							",\"password\":"+ password +
//							",\"domainId\":"+ domainId +
//							",\"roleId\":"+ roleId +
//							",\"isPrivateRole\":\"false" +
//							",\"language\":\"en\"" +
//							",\"dateTimeFormat\":"+ dateTimeFormat +
//							",\"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
//							",\"timeZoneId\":"+ timeZoneId +
//							",\"homeGroupId\":\"100100\"" +
//					"}")
//					.contentType(MediaType.APPLICATION_JSON)
//					.accept(MediaType.APPLICATION_JSON))
//					.andExpect(status().isOk())
//					.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//					.andDo(print())
//					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0004"));

		/* homeGroupIdをリクエストボディにセットしない。 E0011(必須パラメータチェックエラー)が返される */
		System.out.println("* * * * createAccountTest:E0011 Case * * * *");
				mockMvc.perform(post("/account/createAccount/")
					.cookie(cookie)		//Cookieの設定
					.content("{ \"accountName\":"+ name +
							",\"isPermanentAccount\":\"false\"" +
							",\"password\":"+ password +
							",\"domainId\":"+ domainId +
							",\"roleId\":"+ roleId +
							",\"isPrivateRole\":\"false\"" +
							",\"language\":\"en\"" +
							",\"dateTimeFormat\":"+ dateTimeFormat +
							",\"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
							",\"timeZoneId\":"+ timeZoneId +
							//",\"homeGroupId\":\"100100\"" +
					"}")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
					.andDo(print())
					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));

		/* homeGroupIdの値をnullにセット。 E0011(必須パラメータチェックエラー)が返される */
		System.out.println("* * * * createAccountTest:E0011 Case:5 * * * *");
				mockMvc.perform(post("/account/createAccount/")
					.cookie(cookie)		//Cookieの設定
					.content("{ \"accountName\":"+ name +
							",\"isPermanentAccount\":\"false\"" +
							",\"password\":"+ password +
							",\"domainId\":"+ domainId +
							",\"roleId\":"+ roleId +
							",\"isPrivateRole\":\"false\"" +
							",\"language\":\"en\"" +
							",\"dateTimeFormat\":"+ dateTimeFormat +
							",\"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
							",\"timeZoneId\":"+ timeZoneId +
							",\"homeGroupId\":null" +
					"}")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
					.andDo(print())
					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));

		/* homeGroupIdの値をnullにセット。 E0014(パラメータが有効値でない)が返される */
		System.out.println("* * * * createAccountTest:E0014 Case * * * *");
				mockMvc.perform(post("/account/createAccount/")
					.cookie(cookie)		//Cookieの設定
					.content("{ \"accountName\":"+ name +
							",\"isPermanentAccount\":\"false\"" +
							",\"password\":"+ password +
							",\"domainId\":"+ domainId +
							",\"roleId\":"+ roleId +
							",\"isPrivateRole\":\"false\"" +
							",\"language\":\"en\"" +
							",\"dateTimeFormat\":"+ dateTimeFormat +
							",\"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
							",\"timeZoneId\":"+ timeZoneId +
							",\"homeGroupId\":\"\"" +
					"}")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
					.andDo(print())
					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"));

		/* homeGroupIdにJSONフォーマット不正の値をセット。 E0004(JSONパースエラー)が返される */
		System.out.println("* * * * createAccountTest:E0014 Case * * * *");
				mockMvc.perform(post("/account/createAccount/")
					.cookie(cookie)		//Cookieの設定
					.content("{ \"accountName\":"+ name +
							",\"isPermanentAccount\":\"false\"" +
							",\"password\":"+ password +
							",\"domainId\":"+ domainId +
							",\"roleId\":"+ roleId +
							",\"isPrivateRole\":\"false" +
							",\"language\":\"en\"" +
							",\"dateTimeFormat\":"+ dateTimeFormat +
							",\"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
							",\"timeZoneId\":"+ timeZoneId +
							",\"homeGroupId\":\"100100" +
					"}")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
					.andDo(print())
					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0004"));

		/* languageを "en-us" にする E0014(パラメータが有効値でない)が返される */
		System.out.println("* * * * createAccountTest:E0014 Case * * * *");
			mockMvc.perform(post("/account/createAccount/")
				.cookie(cookie)		//Cookieの設定
				.content("{ \"accountName\":"+ name +
						",\"isPermanentAccount\":\"false\"" +
						",\"password\":"+ password +
						",\"domainId\":"+ domainId +
						",\"roleId\":"+ roleId +
						",\"isPrivateRole\":\"false\"" +
						",\"language\":\"en-us\"" +
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
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("language"));

		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * createAccountTest: E0021 Case * * * *");
			Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");
			errorCookie.setMaxAge(60 * 30);
			errorCookie.setPath("/");
			errorCookie.setSecure(false);

			mockMvc.perform(post("/account/createAccount/")
					.cookie(errorCookie)
					.content("{ \"accountName\":"+ name +
							",\"isPermanentAccount\":\"false\"" +
							",\"password\":"+ password +
							",\"domainId\":"+ domainId +
							",\"roleId\":"+ roleId +
							",\"isPrivateRole\":\"false\"" +
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
					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));
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

		/* accountIdリクエストボディにセットしない E0011(必須パラメータチェックエラー) */
		System.out.println("* * * * getAccountTest:E0011 Case * * * *");
		mockMvc.perform(post("/account/getAccount/")
			.cookie(cookie)		//Cookieの設定
			.content("{}")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
			.andDo(print())
			.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));


		/* accountIdの"A-"を除く E0014(パラメータが有効値でない) */
		System.out.println("* * * * getAccountTest:E0014 Case * * * *");
		mockMvc.perform(post("/account/getAccount/")
			.cookie(cookie)		//Cookieの設定
			.content("{ \"accountId\":\"a0e4d82e-2fbd-4ce7-b162-b208c0cb39ba\"}")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
			.andDo(print())
			.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"));


		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * getAccountTest: E0021 Case * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/account/getAccount/")
				.cookie(errorCookie)
				.content("{ \"accountId\":\"A-a0e4d82e-2fbd-4ce7-b162-b208c0cb39be\"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

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

		String accountId = "\"A-10b2c666-e5e8-4b13-9f18-8b2f9a0a107f\"";		//Id設定
		String accountName = "\"testUser3\"";
		String changePasswordFlag = "\"true\"";
		String password = "\"TestUser377793711\"";
		String roleId = "\"R-100c4c12-8f88-4fc5-9d7e-b2335e770c8a\"";
		String language = "\"ja\"";
		String dateTimeFormat = "\"MM/dd/yyyy HH:mm:ss\"";
		String timeZoneSpecifingType = "\"auto\"";
		String timeZoneId = "\"\"";	//autoの場合は空文字("")


		/* accountIdをコンテントボディにセットしない E0011(必須パラメータチェックエラー) */
		System.out.println("* * * * editAccountTest:E0011 Case:1 * * * *");
		mockMvc.perform(post("/account/editAccount/")
				.cookie(cookie)
				.content("{ \"accountName\":" + accountName +
						", \"isPermanentAccount\":\"false\"" +
						", \"changePasswordFlag\":" + changePasswordFlag +
						", \"password\":"+ password +
						", \"loginAccountPassword\":"+ loginPassword +
						", \"roleId\":" + roleId +
						", \"isPrivateRole\":\"false\"" +
						", \"language\":" + language +
						", \"dateTimeFormat\":"+ dateTimeFormat +
						", \"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
						", \"timeZoneId\":"+ timeZoneId +
						", \"homeGroupId\":\"100100\"" +
				"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));

		/* accountIdの"A-"を除く E0014(パラメータが有効値でない) */
		System.out.println("* * * * editAccountTest:E0014 Case:1 * * * *");
		mockMvc.perform(post("/account/editAccount/")
				.cookie(cookie)
				.content("{ \"accountId\":" + accountId.replaceAll("A-", "") +
						 ", \"accountName\":" + accountName +
						 ", \"isPermanentAccount\":\"false\"" +
						 ", \"changePasswordFlag\":" + changePasswordFlag +
						 ", \"password\":"+ password +
						 ", \"loginAccountPassword\":"+ loginPassword +
						 ", \"roleId\":" + roleId +
						 ", \"isPrivateRole\":\"false\"" +
						 ", \"language\":" + language +
						 ", \"dateTimeFormat\":"+ dateTimeFormat +
						 ", \"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
						 ", \"timeZoneId\":"+ timeZoneId +
						 ", \"homeGroupId\":\"100100\"" +
				"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"));


		/**
		 * isPermanentAccountのパラメータ定義がプリミティブのbooleanである為、
		 * 指定無ない場合、falseの扱いになり、パラメータが無いことを検知できない。
		 * そのため、テストは、一旦コメントアウトとする。
		 */
//		/* isPermanentAccountをコンテントボディにセットしない E0011(必須パラメータチェックエラー) */
//		System.out.println("* * * * editAccountTest:E0011 Case:2 * * * *");
//		accountId = "\"A-10b2c666-e5e8-4b13-9f18-8b2f9a0a107f\"";
//		mockMvc.perform(post("/account/editAccount/")
//				.cookie(cookie)
//				.content("{ \"accountId\":" + accountId +
//						", \"accountName\":" + accountName +
//						//", \"isPermanentAccount\":\"false\"" +
//						", \"changePasswordFlag\":" + changePasswordFlag +
//						", \"password\":"+ password +
//						", \"loginAccountPassword\":"+ loginPassword +
//						", \"roleId\":" + roleId +
//						", \"isPrivateRole\":\"false\"" +
//						", \"language\":" + language +
//						", \"dateTimeFormat\":"+ dateTimeFormat +
//						", \"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
//						", \"timeZoneId\":"+ timeZoneId +
//						", \"homeGroupId\":\"100100\"" +
//				"}")
//				.contentType(MediaType.APPLICATION_JSON)
//				.accept(MediaType.APPLICATION_JSON))
//				.andExpect(status().isOk())
//				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//				.andDo(print())
//				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));
//
//		/* isPermanentAccountに空の値("")をセットする E0014(パラメータが有効値でない) */
//		System.out.println("* * * * editAccountTest:E0014 Case:2 * * * *");
//		mockMvc.perform(post("/account/editAccount/")
//				.cookie(cookie)
//				.content("{ \"accountId\":" + accountId +
//						", \"accountName\":" + accountName +
//						", \"isPermanentAccount\":null" +
//						", \"changePasswordFlag\":" + changePasswordFlag +
//						", \"password\":"+ password +
//						", \"loginAccountPassword\":"+ loginPassword +
//						", \"roleId\":" + roleId +
//						", \"isPrivateRole\":\"false\"" +
//						", \"language\":" + language +
//						", \"dateTimeFormat\":"+ dateTimeFormat +
//						", \"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
//						", \"timeZoneId\":"+ timeZoneId +
//						", \"homeGroupId\":\"100100\"" +
//				"}")
//				.contentType(MediaType.APPLICATION_JSON)
//				.accept(MediaType.APPLICATION_JSON))
//				.andExpect(status().isOk())
//				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//				.andDo(print())
//				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"));
//
//		/* isPermanentAccountに空の値("")をセットする E0014(パラメータが有効値でない) */
//		System.out.println("* * * * editAccountTest:E0014 Case:3 * * * *");
//		mockMvc.perform(post("/account/editAccount/")
//				.cookie(cookie)
//				.content("{ \"accountId\":" + accountId +
//						", \"accountName\":" + accountName +
//						", \"isPermanentAccount\":\"\"" +
//						", \"changePasswordFlag\":" + changePasswordFlag +
//						", \"password\":"+ password +
//						", \"loginAccountPassword\":"+ loginPassword +
//						", \"roleId\":" + roleId +
//						", \"isPrivateRole\":\"false\"" +
//						", \"language\":" + language +
//						", \"dateTimeFormat\":"+ dateTimeFormat +
//						", \"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
//						", \"timeZoneId\":"+ timeZoneId +
//						", \"homeGroupId\":\"100100\"" +
//				"}")
//				.contentType(MediaType.APPLICATION_JSON)
//				.accept(MediaType.APPLICATION_JSON))
//				.andExpect(status().isOk())
//				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//				.andDo(print())
//				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"));

		/**
		 * isPermanentAccountがプリミティブ型の為、不正な値は、JSONフォーマットエラーになる。
		 * 実装に合わせる。
		 */
		/* isPermanentAccountに不正な値をセットする E0004(JSONパースエラー) */
		System.out.println("* * * * editAccountTest:E0004 Case:1 * * * *");
		mockMvc.perform(post("/account/editAccount/")
				.cookie(cookie)
				.content("{ \"accountId\":" + accountId +
						", \"accountName\":" + accountName +
						", \"isPermanentAccount\":\"dummy\"" +
						", \"changePasswordFlag\":" + changePasswordFlag +
						", \"password\":"+ password +
						", \"loginAccountPassword\":"+ loginPassword +
						", \"roleId\":" + roleId +
						", \"isPrivateRole\":\"false\"" +
						", \"language\":" + language +
						", \"dateTimeFormat\":"+ dateTimeFormat +
						", \"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
						", \"timeZoneId\":"+ timeZoneId +
						", \"homeGroupId\":\"100100\"" +
				"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0004"));

		/* isPermanentAccountにJSONフォーマット不正の値をセットする E0004(JSONパースエラー) */
		System.out.println("* * * * editAccountTest:E0004 Case:2 * * * *");
		mockMvc.perform(post("/account/editAccount/")
				.cookie(cookie)
				.content("{ \"accountId\":" + accountId +
						", \"accountName\":" + accountName +
						", \"isPermanentAccount\":\"false" +
						", \"changePasswordFlag\":" + changePasswordFlag +
						", \"password\":"+ password +
						", \"loginAccountPassword\":"+ loginPassword +
						", \"roleId\":" + roleId +
						", \"isPrivateRole\":\"false\"" +
						", \"language\":" + language +
						", \"dateTimeFormat\":"+ dateTimeFormat +
						", \"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
						", \"timeZoneId\":"+ timeZoneId +
						", \"homeGroupId\":\"100100\"" +
				"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0004"));

		/**
		 * isPrivateRoleのパラメータ定義がプリミティブのbooleanである為、
		 * 指定無ない場合、falseの扱いになり、パラメータが無いことを検知できない。
		 * そのため、テストは、一旦コメントアウトとする。
		 */
//		/* isPrivateRoleをコンテントボディにセットしない E0011(必須パラメータチェックエラー) */
//		System.out.println("* * * * editAccountTest:E0011 Case:3 * * * *");
//		mockMvc.perform(post("/account/editAccount/")
//				.cookie(cookie)
//				.content("{ \"accountId\":" + accountId +
//						", \"accountName\":" + accountName +
//						", \"isPermanentAccount\":\"false\"" +
//						", \"changePasswordFlag\":" + changePasswordFlag +
//						", \"password\":"+ password +
//						", \"loginAccountPassword\":"+ loginPassword +
//						", \"roleId\":" + roleId +
//						//", \"isPrivateRole\":\"false\"" +
//						", \"language\":" + language +
//						", \"dateTimeFormat\":"+ dateTimeFormat +
//						", \"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
//						", \"timeZoneId\":"+ timeZoneId +
//						", \"homeGroupId\":\"100100\"" +
//				"}")
//				.contentType(MediaType.APPLICATION_JSON)
//				.accept(MediaType.APPLICATION_JSON))
//				.andExpect(status().isOk())
//				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//				.andDo(print())
//				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));
//
//		/* isPrivateRoleの値にnullをセットする E0011(必須パラメータチェックエラー) */
//		System.out.println("* * * * editAccountTest:E0011 Case:4 * * * *");
//		mockMvc.perform(post("/account/editAccount/")
//				.cookie(cookie)
//				.content("{ \"accountId\":" + accountId +
//						", \"accountName\":" + accountName +
//						", \"isPermanentAccount\":\"false\"" +
//						", \"changePasswordFlag\":" + changePasswordFlag +
//						", \"password\":"+ password +
//						", \"loginAccountPassword\":"+ loginPassword +
//						", \"roleId\":" + roleId +
//						", \"isPrivateRole\":null" +
//						", \"language\":" + language +
//						", \"dateTimeFormat\":"+ dateTimeFormat +
//						", \"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
//						", \"timeZoneId\":"+ timeZoneId +
//						", \"homeGroupId\":\"100100\"" +
//				"}")
//				.contentType(MediaType.APPLICATION_JSON)
//				.accept(MediaType.APPLICATION_JSON))
//				.andExpect(status().isOk())
//				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//				.andDo(print())
//				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));
//
//		/* isPrivateRoleに空の値("")をセットする E0014(パラメータが有効値でない) */
//		System.out.println("* * * * editAccountTest:E0014 Case:4 * * * *");
//		mockMvc.perform(post("/account/editAccount/")
//				.cookie(cookie)
//				.content("{ \"accountId\":" + accountId +
//						", \"accountName\":" + accountName +
//						", \"isPermanentAccount\":\"false\"" +
//						", \"changePasswordFlag\":" + changePasswordFlag +
//						", \"password\":"+ password +
//						", \"loginAccountPassword\":"+ loginPassword +
//						", \"roleId\":" + roleId +
//						", \"isPrivateRole\":\"\"" +
//						", \"language\":" + language +
//						", \"dateTimeFormat\":"+ dateTimeFormat +
//						", \"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
//						", \"timeZoneId\":"+ timeZoneId +
//						", \"homeGroupId\":\"100100\"" +
//				"}")
//				.contentType(MediaType.APPLICATION_JSON)
//				.accept(MediaType.APPLICATION_JSON))
//				.andExpect(status().isOk())
//				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//				.andDo(print())
//				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"));
//
//		/* isPrivateRoleに不正な値をセットする E0014(パラメータが有効値でない) */
//		System.out.println("* * * * editAccountTest:E0014 Case:5 * * * *");
//		mockMvc.perform(post("/account/editAccount/")
//				.cookie(cookie)
//				.content("{ \"accountId\":" + accountId +
//						", \"accountName\":" + accountName +
//						", \"isPermanentAccount\":\"false\"" +
//						", \"changePasswordFlag\":" + changePasswordFlag +
//						", \"password\":"+ password +
//						", \"loginAccountPassword\":"+ loginPassword +
//						", \"roleId\":" + roleId +
//						", \"isPrivateRole\":\"dummy\"" +
//						", \"language\":" + language +
//						", \"dateTimeFormat\":"+ dateTimeFormat +
//						", \"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
//						", \"timeZoneId\":"+ timeZoneId +
//						", \"homeGroupId\":\"100100\"" +
//				"}")
//				.contentType(MediaType.APPLICATION_JSON)
//				.accept(MediaType.APPLICATION_JSON))
//				.andExpect(status().isOk())
//				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//				.andDo(print())
//				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"));

		/**
		 * isPrivateRoleがプリミティブ型の為、不正な値は、JSONフォーマットエラーになる。
		 * 実装に合わせる。
		 */
		/* isPrivateRoleにJSONフォーマット不正の値をセットする E0004(JSONパースエラー) */
		System.out.println("* * * * editAccountTest:E0004 Case:3 * * * *");
		mockMvc.perform(post("/account/editAccount/")
				.cookie(cookie)
				.content("{ \"accountId\":" + accountId +
						", \"accountName\":" + accountName +
						", \"isPermanentAccount\":\"false\"" +
						", \"changePasswordFlag\":" + changePasswordFlag +
						", \"password\":"+ password +
						", \"loginAccountPassword\":"+ loginPassword +
						", \"roleId\":" + roleId +
						", \"isPrivateRole\":\"false" +
						", \"language\":" + language +
						", \"dateTimeFormat\":"+ dateTimeFormat +
						", \"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
						", \"timeZoneId\":"+ timeZoneId +
						", \"homeGroupId\":\"100100\"" +
				"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0004"));

		/* homeGroupIdをリクエストボディにセットしない。 E0011(必須パラメータチェックエラー)が返される */
		System.out.println("* * * * editAccountTest:E0011 Case:5 * * * *");
				mockMvc.perform(post("/account/editAccount/")
					.cookie(cookie)		//Cookieの設定
					.content("{ \"accountId\":" + accountId +
							 ", \"accountName\":" + accountName +
							 ", \"isPermanentAccount\":\"false\"" +
							 ", \"changePasswordFlag\":" + changePasswordFlag +
							 ", \"password\":"+ password +
							 ", \"loginAccountPassword\":"+ loginPassword +
							 ", \"roleId\":" + roleId +
							 ", \"isPrivateRole\":\"false\"" +
							 ", \"language\":" + language +
							 ", \"dateTimeFormat\":"+ dateTimeFormat +
							 ", \"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
							 ", \"timeZoneId\":"+ timeZoneId +
							 //", \"homeGroupId\":\"100100\"" +
					"}")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
					.andDo(print())
					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));

		/* homeGroupIdの値をnullにセット。 E0011(必須パラメータチェックエラー)が返される */
		System.out.println("* * * * editAccountTest:E0011 Case:6 * * * *");
				mockMvc.perform(post("/account/editAccount/")
					.cookie(cookie)		//Cookieの設定
					.content("{ \"accountId\":" + accountId +
							 ", \"accountName\":" + accountName +
							 ", \"isPermanentAccount\":\"false\"" +
							 ", \"changePasswordFlag\":" + changePasswordFlag +
							 ", \"password\":"+ password +
							 ", \"loginAccountPassword\":"+ loginPassword +
							 ", \"roleId\":" + roleId +
							 ", \"isPrivateRole\":\"false\"" +
							 ", \"language\":" + language +
							 ", \"dateTimeFormat\":"+ dateTimeFormat +
							 ", \"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
							 ", \"timeZoneId\":"+ timeZoneId +
							 ", \"homeGroupId\":null" +
					"}")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
					.andDo(print())
					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));

		/* homeGroupIdに空の値("")をセット。 E0014(パラメータが有効値でない)が返される */
		System.out.println("* * * * editAccountTest:E0014 Case:6 * * * *");
				mockMvc.perform(post("/account/editAccount/")
					.cookie(cookie)		//Cookieの設定
					.content("{ \"accountId\":" + accountId +
							 ", \"accountName\":" + accountName +
							 ", \"isPermanentAccount\":\"false\"" +
							 ", \"changePasswordFlag\":" + changePasswordFlag +
							 ", \"password\":"+ password +
							 ", \"loginAccountPassword\":"+ loginPassword +
							 ", \"roleId\":" + roleId +
							 ", \"isPrivateRole\":\"false\"" +
							 ", \"language\":" + language +
							 ", \"dateTimeFormat\":"+ dateTimeFormat +
							 ", \"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
							 ", \"timeZoneId\":"+ timeZoneId +
							 ", \"homeGroupId\":\"\"" +
					"}")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
					.andDo(print())
					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"));

		/* homeGroupIdにJSONフォーマット不正の値をセット。 E0004(JSONパースエラー)が返される */
		System.out.println("* * * * editAccountTest:E0004 Case:4 * * * *");
				mockMvc.perform(post("/account/editAccount/")
					.cookie(cookie)		//Cookieの設定
					.content("{ \"accountId\":" + accountId +
							 ", \"accountName\":" + accountName +
							 ", \"isPermanentAccount\":\"false\"" +
							 ", \"changePasswordFlag\":" + changePasswordFlag +
							 ", \"password\":"+ password +
							 ", \"loginAccountPassword\":"+ loginPassword +
							 ", \"roleId\":" + roleId +
							 ", \"isPrivateRole\":\"false\"" +
							 ", \"language\":" + language +
							 ", \"dateTimeFormat\":"+ dateTimeFormat +
							 ", \"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
							 ", \"timeZoneId\":"+ timeZoneId +
							 ", \"homeGroupId\":\"100100" +
					"}")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
					.andDo(print())
					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0004"));

		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * editAccountTest: E0021 Case:1 * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(
				post("/account/editAccount/")
					.cookie(errorCookie)
					.content("{ \"accountId\":" + accountId +
						 ", \"accountName\":" + accountName +
						 ", \"isPermanentAccount\":\"false\"" +
						 ", \"changePasswordFlag\":" + changePasswordFlag +
						 ", \"password\":"+ password +
						 ", \"loginAccountPassword\":"+ loginPassword +
						 ", \"roleId\":" + roleId +
						 ", \"isPrivateRole\":\"false\"" +
						 ", \"language\":" + language +
						 ", \"dateTimeFormat\":"+ dateTimeFormat +
						 ", \"timeZoneSpecifingType\":"+ timeZoneSpecifingType +
						 ", \"timeZoneId\":"+ timeZoneId +
						 ", \"homeGroupId\":\"100100\"" + "}")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

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

		String deleteAccountList[] = {"\"A-a0e4d82e-2fbd-4ce7-b162-b208c0cb39ba\"",};		//削除済み

		/* accountIdをコンテントボディにセットしない E0011(必須パラメータチェックエラー) */
		System.out.println("* * * * deleteAccountTest:E0011 Case * * * *");
		mockMvc.perform(post("/account/deleteAccount/")
				.cookie(cookie)
				.content("{}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));


		/* deleteAccountList[0]の "A-"を除く E0014(必須パラメータが有効値でない) */
		System.out.println("* * * * deleteAccountTest:E0014 Case * * * *");
		mockMvc.perform(post("/account/deleteAccount/")
				.cookie(cookie)
				.content("{\"accountIdList\":["+ deleteAccountList[0].replaceAll("A-", "") +"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"));

		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * deleteAccountTest: E0021 Case * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/account/deleteAccount/")
				.cookie(errorCookie)
				.content("{\"accountIdList\":["+ deleteAccountList[0] +"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		accountManager.logout();

	}

	/***
	 * SRDM Common Service Communication API Specification 3.1.6
	 * アカウントロック解除テスト accountUnlockTest
	 * @throws Exception
	 */
	@Test
	public void accountUnlockTest() throws Exception{

		String accountId = "\"A-287befaa-6aa5-45af-87fa-b9e5baf334a1\"";

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* accountIdをコンテントボディにセットしない E0011(必須パラメータチェックエラー) */
		System.out.println("* * * * accountUnlockTest:E0011 Case * * * *");
		mockMvc.perform(post("/account/accountUnlock/")
				.cookie(cookie)
				.content("{}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));


		/* accountIdの"A-"を除く E0014(パラメータが有効値でない) */
		System.out.println("* * * * accountUnlockTest:E0014 Case * * * *");
		accountId = "\"287befaa-6aa5-45af-87fa-b9e5baf334a1\"";
		mockMvc.perform(post("/account/accountUnlock/")
				.cookie(cookie)
				.content("{\"accountIdList\":["+ accountId +"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"));

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

		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * getAccountPermissionTest: E0021 Case * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/account/getAccountPermission/")
				.cookie(errorCookie)
				.content("")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		accountManager.logout();


	}

	/***
	 * SRDM Common Service Communication API Specification 3.1.8
	 * アカウント情報メール通知テスト setEmailNortifyTest
	 * @throws Exception
	 */
	//@Test
	public void setEmailNortifyTest() throws Exception{
		String toAddress = "\"testUser2@mail.com\"";
		String ccAddress = "\"testUser2@mail.com\"";
		String bccAddress = "\"\"";
		String subject = "\"MailTitle\"";
		String body = "\"Content string\"";

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* toAddressをコンテントボディにセットしない E0011(必須パラメータチェックエラー) */
		System.out.println("* * * * setEmailNortifyTest:E0011 Case * * * *");
		mockMvc.perform(post("/account/setEmailNortify/")
				.cookie(cookie)
				.content("{ \"ccAddress\": "+ ccAddress +
						  ",\"bccAddress\":" + bccAddress +
						  ",\"subject\":" + subject +
						  ",\"body\":" + body + "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));


		/* toAddressの "@"を除く E0014(パラメータが有効値ない) */
		System.out.println("* * * * setEmailNortifyTest:E0014 Case * * * *");
		toAddress = "\"testUser2mail.com\"";
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
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"));
		toAddress = "\"testUser2@mail.com\"";	//元に戻す


		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * setEmailNortifyTest: E0021 Case * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/account/setEmailNortify/")
				.cookie(errorCookie)
				.content("")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

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

		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * getLoginAccountInfoTest: E0021 Case * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/account/getLoginAccountInfo/")
				.cookie(errorCookie)
				.content("")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		accountManager.logout();

	}

	/***
	 * SRDM Common Service Communication API Specification 3.6.2
	 * ログイン状態取得テスト getLoginStatusTest
	 * @throws Exception
	 */
	//@Test
	public void getLoginStatusTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* cookieをセットしない loginFlg:false が返ってくる */
		System.out.println("* * * * getLoginStatusTest: loginFlg=false Case * * * *");
		mockMvc.perform(post("/account/getLoginStatus/")
				//.cookie(cookie)
				.content("")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.loginFlg").value("false"));

		accountManager.logout();

	}

	/***
	 * SRDM Common Service Communication API Specification 3.6.3
	 * ログアウトテスト getAccountListTest
	 * @throws Exception
	 */
	@Test
	public void LogoutTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		//ひな型
		mockMvc.perform(post("/account/logout/")
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
	 * SRDM Common Service Communication API Specification 3.6.4
	 * ログイン認証テスト loginForAgentTest
	 * @throws Exception
	 */
	@Test
	public void loginForAgentTest() throws Exception{

		/* domainIdをコンテントボディにセットしない E0011(必須パラメータチェックエラー) が返ってくる */
		System.out.println("* * * * loginForAgentTest: E0011 Case * * * *");
		mockMvc.perform(post("/account/loginForAgent/")
				.content("{ \"accountName\":\"" + TestAccount.adminAccountName + "\"" +
						  ",\"password\":" + loginPassword + "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainId"));

	}

	/***
	 * SRDM Common Service Communication API Specification 3.6.5
	 * ログイン認証スケジュールテスト loginForScheduleTest
	 * @throws Exception
	 */
	@Test
	public void loginForSchedule() throws Exception{

		/* domainIdをコンテントボディにセットしない。E0011(必須パラメータチェックエラー) が返ってくる */
		System.out.println("* * * * loginForScheduleTest: E0011 Case * * * *");
		mockMvc.perform(post("/account/loginForSchedule/")
				.content("{ \"accountId\":\"" + accountId + "\"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainId"));


		/* domainIdの "D-"を除く。validateResultFlg:false が返ってくる */
		System.out.println("* * * * loginForScheduleTest: validateResultFlg:false Case * * * *");
		mockMvc.perform(post("/account/loginForSchedule/")
				.content("{ \"domainId\":\"" + loginDomainId.replaceAll("D-", "") + "\"" +
						  ",\"accountId\":\"" + accountId + "\"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty())
				.andExpect(jsonPath("$.validateResultFlg").value("false"))
				.andExpect(jsonPath("$.sessionId").isEmpty());

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

		//ひな型
		//エラーがディスク容量不足エラーぐらいしかない
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
	 * ユーザー操作によるアカウントロックテスト accountLockTest
	 * @throws Exception
	 */
	@Test
	public void accountLockTest() throws Exception{

		String accountId = "\"A-287befaa-6aa5-45af-87fa-b9e5baf334a1\"";

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* accountIdをコンテントボディにセットしない E0011(必須パラメータチェックエラー) */
		System.out.println("* * * * accountLockTest:E0011 Case1 * * * *");
		mockMvc.perform(post("/account/accountLock/")
				.cookie(cookie)
				.content("{}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));

		/* accountIdの値をセットしない E0011 */
		System.out.println("* * * * accountLockTest:E0011 Case2 * * * *");
		mockMvc.perform(post("/account/accountLock/")
				.cookie(cookie)
				.content("{\"accountIdList\":null}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));

		/* accountIdに空("")をセット E0014 */
		System.out.println("* * * * accountLockTest:E0014 Case1 * * * *");
		mockMvc.perform(post("/account/accountLock/")
				.cookie(cookie)
				.content("{\"accountIdList\":[\"\"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"));

		/* accountIdの"A-"を除く E0014(パラメータが有効値でない) */
		System.out.println("* * * * accountLockTest:E0014 Case2 * * * *");
		accountId = "\"287befaa-6aa5-45af-87fa-b9e5baf334a1\"";
		mockMvc.perform(post("/account/accountLock/")
				.cookie(cookie)
				.content("{\"accountIdList\":["+ accountId +"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"));

		/* accountIdの"A-"を除く E0014(有効値でない値と混在) */
		System.out.println("* * * * accountLockTest:E0014 Case3 * * * *");
		accountId  = "\"287befaa-6aa5-45af-87fa-b9e5baf334a1\"";
		String accountId2 = "\"A-287befaa-6aa5-45af-87fa-b9e5baf334a1\"";
		mockMvc.perform(post("/account/accountLock/")
				.cookie(cookie)
				.content("{\"accountIdList\":["+ accountId + "," + accountId2 +"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"));

		/* accountIdの"\""を除く E0004(JSONパースエラー) */
		System.out.println("* * * * accountLockTest:E0004 Case * * * *");
		accountId = "\"A-287befaa-6aa5-45af-87fa-b9e5baf334a1";
		mockMvc.perform(post("/account/accountLock/")
				.cookie(cookie)
				.content("{\"accountIdList\":["+ accountId +"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0004"));

		/* SessionIdチェック
		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		accountId = "\"A-287befaa-6aa5-45af-87fa-b9e5baf334a1\"";

		System.out.println("* * * * accountLockTest: E0021 Case1 * * * *");
		mockMvc.perform(post("/account/accountLock/")
					.content("{\"accountIdList\":["+ accountId +"]}")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
					.andDo(print())
					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		System.out.println("* * * * accountLockTest: E0021 Case2 * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","");
		errorCookie.setMaxAge(60 * 30);
		errorCookie.setPath("/");
		errorCookie.setSecure(false);
		mockMvc.perform(post("/account/accountLock/")
					.cookie(errorCookie)
					.content("{\"accountIdList\":["+ accountId +"]}")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
					.andDo(print())
					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		System.out.println("* * * * accountLockTest: E0021 Case3 * * * *");
		errorCookie = new Cookie("dsessionId_8085","dummy-sessionId");
		errorCookie.setMaxAge(60 * 30);
		errorCookie.setPath("/");
		errorCookie.setSecure(false);
		mockMvc.perform(post("/account/accountLock/")
					.cookie(errorCookie)
					.content("{\"accountIdList\":["+ accountId +"]}")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
					.andDo(print())
					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		System.out.println("* * * * accountLockTest: E0021 Case4 * * * *");
		errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");
		errorCookie.setMaxAge(60 * 30);
		errorCookie.setPath("/");
		errorCookie.setSecure(false);
		mockMvc.perform(post("/account/accountLock/")
					.cookie(errorCookie)
					.content("{\"accountIdList\":["+ accountId +"]}")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
					.andDo(print())
					.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		accountManager.logout();

	}

}
