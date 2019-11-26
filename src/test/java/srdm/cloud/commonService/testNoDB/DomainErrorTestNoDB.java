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
public class DomainErrorTestNoDB {

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
	 * SRDM Common Service Communication API Specification 3.7.1
	 * ドメイン一覧取得テスト getDomainListTest
	 * @throws Exception
	 */
	@Test
	public void getDomainListTest() throws Exception {
		long startIndex = 1;
		long count = 10;
		String domainId = "\"" + loginDomainId + "\"";

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* domainIdをコンテントボディにセットしない。E0011(必須パラメータチェックエラー) が返ってくる */
		System.out.println("* * * * getDomainListTest: E0011 Case * * * *");
		mockMvc.perform(post("/domain/getDomainList/")
				.cookie(cookie)
				.content("{ \"startIndex\":"+ startIndex +
						  ",\"count\":" + count +
						  "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainId"));


		/* domainIdの "D-" を除く。E0014(パラメータが有効値でない) が返ってくる */
		System.out.println("* * * * LoginTest: sessionId=null Case * * * *");
		mockMvc.perform(post("/domain/getDomainList/")
				.cookie(cookie)
				.content("{ \"startIndex\":"+ startIndex +
						  ",\"count\":" + count +
						  ",\"domainId\":"+ domainId.replaceAll("D-", "") +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainId"));



		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * getDomainListTest: E0021 Case * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/domain/getDomainList/")
				.cookie(errorCookie)
				.content("{\"startIndex\":\""+ startIndex +
						"\",\"count\":\""+ count +
						"\",\"domainId\":"+ domainId +
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
	 * SRDM Common Service Communication API Specification 3.7.2
	 * ドメイン作成テスト createDomainTest
	 * @throws Exception
	 */
	@Test
	public void createDomainTest() throws Exception{

		String domainName = "\"TestDomain-1\"";
		String parentDomainId = "\"" + loginDomainId + "\"";
		String targetGroupId = "\"100100\"";

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* domainNameをコンテントボディにセットしない。E0011(必須パラメータチェックエラー) が返ってくる */
		System.out.println("* * * * createDomainTest: E0011 Case * * * *");
		mockMvc.perform(post("/domain/createDomain/")
				.cookie(cookie)
				.content("{ \"parentDomainId\":"+ parentDomainId +
						  ",\"targetGroupId\":"+ targetGroupId +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainName"));


		/* parentDomainId の "D-"を除く。E0014(パラメータが有効値でない) が返ってくる */
		System.out.println("* * * * createDomainTest: E0014 Case * * * *");
		mockMvc.perform(post("/domain/createDomain/")
				.cookie(cookie)
				.content("{ \"domainName\":"+ domainName +
						  ",\"parentDomainId\":"+ parentDomainId.replaceAll("D-", "") +
						  ",\"targetGroupId\":"+ targetGroupId +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("parentDomainId"));


		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * createDomainTest: E0021 Case * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/domain/createDomain/")
				.cookie(errorCookie)
				.content("{ \"domainName\":"+ domainName +
						  ",\"parentDomainId\":"+ parentDomainId  +
						  ",\"targetGroupId\":"+ "\"123456\"" +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		accountManager.logout();

	}

	/***
	 * SRDM Common Service Communication API Specification 3.7.3
	 * ドメイン情報取得テスト getDomainTest
	 * @throws Exception
	 */
	@Test
	public void getDomainTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* domainId をコンテントボディにセットしない。E0011(必須パラメータチェックエラー) が返ってくる */
		System.out.println("* * * * getDomainTest: E0011 Case * * * *");
		mockMvc.perform(post("/domain/getDomain/")
				.cookie(cookie)
				.content("{}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainId"));


		/* domainIdの"D-" を 除く。E0014(パラメータが有効値でない) が返ってくる */
		System.out.println("* * * * getDomainTest: E0014 Case * * * *");
		mockMvc.perform(post("/domain/getDomain/")
				.cookie(cookie)
				.content("{ \"domainId\":"+ "\"8cf6f8b1-733b-4d89-8f61-79d781929111\"" +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainId"));


		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * getDomainTest: E0021 Case * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/domain/getDomain/")
				.cookie(errorCookie)
				.content("{ \"domainId\":"+ "\"D-8cf6f8b1-733b-4d89-8f61-79d781929113\"" +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		accountManager.logout();
	}


	/***
	 * SRDM Common Service Communication API Specification 3.7.4
	 * ドメイン情報編集テスト editDomainTest
	 * @throws Exception
	 */
	@Test
	public void editDomainTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		String editDomainId = "\"D-8cf6f8b1-733b-4d89-8f61-79d781929111\"";
		String editDomainName = "\"TestDomain-2\"";
		String editTargetGroupId = "\"0\"";


		/* domainIdをコンテントボディにセットしない。E0011(必須パラメータチェックエラー) が返ってくる */
		System.out.println("* * * * editDomainTest: E0011 Case * * * *");
		mockMvc.perform(post("/domain/editDomain/")
				.cookie(cookie)
				.content("{ \"domainName\":"+ editDomainName +
				          ",\"targetGroupId\":"+ editTargetGroupId +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainId"));


		/* editDomainIdの "D-"を除く。E0014(パラメータが有効値でない有効値でない) が返ってくる */
		System.out.println("* * * * editDomainTest: E0014 Case * * * *");
		mockMvc.perform(post("/domain/editDomain/")
				.cookie(cookie)
				.content("{ \"domainId\":"+ editDomainId.replaceAll("D-", "") +
				          ",\"domainName\":"+ editDomainName +
				          ",\"targetGroupId\":"+ editTargetGroupId +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainId"));


		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * editDomainTest: E0021 Case * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");
		errorCookie.setMaxAge(60 * 30);
		errorCookie.setPath("/");
		errorCookie.setSecure(false);

		mockMvc.perform(post("/domain/editDomain/")
				.cookie(errorCookie)
				.content("{ \"domainId\":"+ editDomainId +
				          ",\"domainName\":"+ editDomainName +
				          ",\"targetGroupId\":"+ editTargetGroupId +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		accountManager.logout();

	}

	/***
	 * SRDM Common Service Communication API Specification 3.7.5
	 * ドメイン削除テスト deleteDomainTest
	 * @throws Exception
	 */
	@Test
	public void deleteDomainTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		String deleteDomainList[] = {"\"D-8cf6f8b1-733b-4d89-8f61-79d781929111\""};


		/* deleteDomainIdListをコンテントボディにセットしない。E0011(必須パラメータチェックエラー) が返ってくる */
		System.out.println("* * * * deleteDomainTest: E0011 Case * * * *");
		mockMvc.perform(post("/domain/deleteDomain/")
				.cookie(cookie)
				.content("{}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainIdList"));


		/* deleteDomainIdの "D-"を除く。E0014(パラメータが有効でない) が返ってくる */
		System.out.println("* * * * deleteDomainTest: E0014 Case * * * *");
		mockMvc.perform(post("/domain/deleteDomain/")
				.cookie(cookie)
				.content("{ \"domainIdList\":"+ "[" + deleteDomainList[0].replaceAll("D-", "") + "]" + "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainIdList"));


		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * deleteDomainTest: E0021 Case * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/domain/deleteDomain/")
				.cookie(errorCookie)
				.content("{ \"domainIdList\":"+ "[" + deleteDomainList[0] + "]" + "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		accountManager.logout();


	}

	/***
	 * SRDM Common Service Communication API Specification 3.13.1
	 * ドメイン画面表示パターン取得テスト getThemeSettingTest
	 * @throws Exception
	 */
	@Test
	public void getThemeSettingTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * getThemeSettingTest: E0021 Case * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/domain/getThemeSetting/")
				.cookie(errorCookie)
				.content("{}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		accountManager.logout();
	}

	/***
	 * SRDM Common Service Communication API Specification 3.13.2
	 * ドメイン画面表示パターン設定テスト getThemeSettingTest
	 * @throws Exception
	 */
	@Test
	public void setThemeSettingTest() throws Exception{

		String theme = "\"Enterprise\"";

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* themeをコンテントボディに設定しない。E0011(必須パラメータチェックエラー) が返ってくる */
		System.out.println("* * * * setThemeSettingTest: E0011 Case * * * *");
		mockMvc.perform(post("/domain/setThemeSetting/")
				.cookie(cookie)
				.content("{}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("theme"));


		/* themeを"Enterpris"にする。E0014(パラメータが有効値でない) が返ってくる */
		System.out.println("* * * * setThemeSettingTest: E0011 Case * * * *");
		theme = "\"Enterpris\"";
		mockMvc.perform(post("/domain/setThemeSetting/")
				.cookie(cookie)
				.content("{ \"theme\":" + theme + "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("theme"));
		theme = "\"Enterprise\"";


		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * setThemeSettingTest: E0021 Case * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/domain/setThemeSetting/")
				.cookie(errorCookie)
				.content("{ \"theme\":" + theme + "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		accountManager.logout();

	}

}
