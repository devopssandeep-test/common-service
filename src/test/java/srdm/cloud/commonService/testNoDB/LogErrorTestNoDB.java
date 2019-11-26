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
public class LogErrorTestNoDB {

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
	 * SRDM Common Service Communication API Specification 3.10.1
	 * 操作ログ取得テスト getOpearationLogTest
	 * @throws Exception
	 */
	@Test
	public void getOperationLogTest() throws Exception{

		long startIndex = 1;
		long count = 10;
		String domainId = loginDomainId;

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* domainIdをコンテントボディにセットしない。E0011(必須パラメータチェックエラー)が返ってくる */
		System.out.println("* * * * getOperationLogTest: E0011 Case * * * *");
		mockMvc.perform(post("/log/getOperationLog/")
				.cookie(cookie)
				.content("{ \"startIndex\":\"" + startIndex + "\"" +
						  ",\"count\":\"" + count + "\"" +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainId"));


		/* domainIdの "D-" を除く。E0014(パラメータが有効値でない)が返ってくる */
		System.out.println("* * * * getOperationLogTest: E0014 Case * * * *");
		mockMvc.perform(post("/log/getOperationLog/")
				.cookie(cookie)
				.content("{ \"startIndex\":\"" + startIndex + "\"" +
						  ",\"count\":\"" + count + "\"" +
						  ",\"domainId\":\""+ domainId.replaceAll("D-", "") + "\"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainId"));


		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * getOperationLogTest: E0021 Case * * * *");

		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");	//エラー
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/log/getOperationLog/")
				.cookie(errorCookie)
				.content("{ \"startIndex\":\"" + startIndex + "\"" +
						  ",\"count\":\"" + count + "\"" +
						  ",\"domainId\":\""+ domainId + "\"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		accountManager.logout();

	}

	/***
	 * SRDM Common Service Communication API Specification 3.10.2
	 * 操作ログ削除テスト deleteOpearationLogTest
	 * @throws Exception
	 */
	@Test
	public void deleteOperationLogTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* domainIdをコンテントボディにセットしない。E0011(必須パラメータチェックエラー)が返ってくる */
		System.out.println("* * * * deleteOperationLogTest: E0011 Case * * * *");
		mockMvc.perform(post("/log/deleteOperationLog/")
				.cookie(cookie)
				.content("{}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainId"));

		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * deleteOperationLogTest: E0021 Case * * * *");

		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");	//エラー
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/log/deleteOperationLog/")
				.cookie(errorCookie)
				.content("{ \"domainId\":"+ "\"D-4dddb97a-3272-490f-b0fb-8762eaa69d47\"" +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		accountManager.logout();
	}

	/***
	 * SRDM Common Service Communication API Specification 3.10.3
	 * システム管理ログ取得テスト getSystemLogTest
	 * @throws Exception
	 */
	@Test
	public void getSystemLogTest() throws Exception{
		long startIndex = 1;
		long count = 10;
		String simpleFilterKey = "\"kind\"";
		String simpleFilterValue = "\"error\"";
		String order = "\"descending\"";
		String orderKey = "\"timestamp\"";

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* simpleFilterのkeyをコンテントボディにセットしない。E0011(必須パラメータチェックエラー)が返ってくる */
		System.out.println("* * * * getSystemLogTest: E0011 Case * * * *");
		mockMvc.perform(post("/log/getSystemLog/")
				.cookie(cookie)
				.content("{ \"startIndex\":\""+ startIndex + "\"" +
				          ",\"count\":\""+ count + "\"" +
						  ",\"simpleFilter\":[{" +
				                               "\"value\":"+ simpleFilterValue + "}]" +
						  ",\"orderBy\":[{ \"key\":" + orderKey + "," +
				                          "\"order\":" + order + "}] }")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("simpleFilter[0].key"));


		/* simpleFilter:keyを "KIND" にする。E0014(パラメータが有効値でない)が返ってくる */
		System.out.println("* * * * getSystemLogTest: E0014 Case * * * *");
		simpleFilterKey = "\"KIND\"";
		mockMvc.perform(post("/log/getSystemLog/")
				.cookie(cookie)
				.content("{ \"startIndex\":\""+ startIndex + "\"" +
				          ",\"count\":\""+ count + "\"" +
						  ",\"simpleFilter\":[{ \"key\":" + simpleFilterKey +"," +
				                               "\"value\":"+ simpleFilterValue + "}]" +
						  ",\"orderBy\":[{ \"key\":" + orderKey + "," +
				                          "\"order\":" + order + "}] }")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("simpleFilter[0].key"));


		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * getSystemLogTest: E0021 Case * * * *");

		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");	//エラー
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/log/getSystemLog/")
				.cookie(errorCookie)
				.content("{ \"domainId\":"+ "\"D-4dddb97a-3272-490f-b0fb-8762eaa69d47\"" +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		accountManager.logout();
	}

	/***
	 * SRDM Common Service Communication API Specification 3.10.4
	 * システム管理ログ削除テスト deleteOpearationLogTest
	 * @throws Exception
	 */
	@Test
	public void deleteSystemLog() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * deleteSystemLogTest: E0021 Case * * * *");

		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");	//エラー
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/log/deleteSystemLog/")
				.cookie(errorCookie)
				.content("{ \"domainId\":"+ "\"D-4dddb97a-3272-490f-b0fb-8762eaa69d47\"" +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		accountManager.logout();
	}


	/***
	 * SRDM Common Service Communication API Specification 3.10.5
	 * 操作ログ詳細項目取得テスト getOpearationLogDetailTest
	 * @throws Exception
	 */
	@Test
	public void getOperationDetailTest() throws Exception{

		String domainId = loginDomainId;
		String logId = "\"591ac893-973e-4f93-9014-9c72ef614fbe\"";

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* domainIdをコンテントボディにセットしない。E0011(必須パラメータチェックエラー)が返ってくる */
		System.out.println("* * * * getOperationLogDetailTest: E0011 Case * * * *");
		mockMvc.perform(post("/log/getOperationLogDetail/")
				.cookie(cookie)
				.content("{ \"logId\":" + logId + "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainId"));



		/* domainIdの "D-" を除く。E0014(パラメータが有効値でない)が返ってくる */
		System.out.println("* * * * getOperationLogDetailTest: E0014 Case * * * *");
		mockMvc.perform(post("/log/getOperationLogDetail/")
				.cookie(cookie)
				.content("{ \"domainId\":\"" + domainId.replaceAll("D-", "") + "\"" +
						  ",\"logId\":" + logId + "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainId"));


		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * getOperationLogDetailTest: E0021 Case * * * *");

		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");	//エラー
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/log/getOperationLogDetail/")
				.cookie(errorCookie)
				.content("{ \"domainId\":"+ "\"D-4dddb97a-3272-490f-b0fb-8762eaa69d47\"" +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		accountManager.logout();

	}

	/***
	 * SRDM Common Service Communication API Specification 3.10.6
	 * 操作ログ詳細項目取得テスト getSystemLogDetailTest
	 * @throws Exception
	 */
	@Test
	public void getSystemLogDetailTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* logIdをコンテントボディにセットしない。E0011(必須パラメータチェックエラー)が返ってくる */
		System.out.println("* * * * getSystemLogDetailTest: E0011 Case * * * *");
		mockMvc.perform(post("/log/getSystemLogDetail/")
				.cookie(cookie)
				.content("{}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("logId"));


		/* logIdの末尾に "error" をつける。E0014(パラメータが有効値でない)が返ってくる */
		System.out.println("* * * * getSystemLogDetailTest: E0014 Case * * * *");
		mockMvc.perform(post("/log/getSystemLogDetail/")
				.cookie(cookie)
				.content("{ \"logId\":"+ "\"c6db062f-06b4-4a9e-a636-2f0cfb7ebfa9error\"" +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("logId"));

		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * getSystemLogDetailTest: E0021 Case * * * *");

		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");	//エラー
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/log/getSystemLogDetail/")
				.cookie(errorCookie)
				.content("{ \"domainId\":"+ "\"D-4dddb97a-3272-490f-b0fb-8762eaa69d47\"" +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		accountManager.logout();

	}

}
