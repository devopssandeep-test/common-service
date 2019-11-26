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
public class RoleErrorTestNoDB {

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
	 * SRDM Common Service Communication API Specification 3.8.1
	 * ロールリスト情報取得テスト getRoleListTest
	 * @throws Exception
	 */
	@Test
	public void getRoleListTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		long startIndex = 1;
		long count = 10;
		String simpleFilterKey = "\"roleName\"";
		String simpleFilterValue = "\"admin role\"";
		String order = "\"descending\"";
		String orderKey = "\"roleName\"";

		/* simpleFilter.keyをコンテントボディにセットしない。E0011(必須パラメータチェックエラー)が返ってくる */
		System.out.println("* * * * getRoleListTest: E0011 Case * * * *");
		mockMvc.perform(post("/role/getRoleList/")
				.cookie(cookie)
				.content("{ \"startIndex\":\"" + startIndex + "\""+
						  ",\"count\":\""+ count +"\"" +
						  ",\"domainId\":\""+ loginDomainId + "\"" +
						  ",\"simpleFilter\":[{ "+
						                       "\"value\":"+ simpleFilterValue + "}]" +
						  ",\"orderBy\":[{ \"key\":" + orderKey + ","+
						                  "\"order\":" + order +"}] }")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("simpleFilter[0].key"));


		/* simpleFilter.keyを"RoleName"にする。E0014(パラメータチェックが有効値でない)が返ってくる */
		System.out.println("* * * * getRoleListTest: E0014 Case * * * *");
		simpleFilterKey = "\"RoleName\"";
		mockMvc.perform(post("/role/getRoleList/")
				.cookie(cookie)
				.content("{ \"startIndex\":\"" + startIndex + "\""+
						  ",\"count\":\""+ count +"\"" +
						  ",\"domainId\":\""+ loginDomainId + "\"" +
						  ",\"simpleFilter\":[{ \"key\":" + simpleFilterKey + "," +
						                       "\"value\":"+ simpleFilterValue + "}]" +
						  ",\"orderBy\":[{ \"key\":" + orderKey + ","+
						                  "\"order\":" + order +"}] }")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("simpleFilter[0].key"));
		simpleFilterKey = "\"roleName\"";


		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * getRoleListTest: E0021 Case * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/role/getRoleList/")
				.cookie(errorCookie)
				.content("{ \"startIndex\":\"" + startIndex + "\""+
						  ",\"count\":\""+ count +"\"" +
						  ",\"domainId\":"+ "\"D-4dddb97a-3272-490f-b0fb-8762eaa69d45\"" +
						  ",\"simpleFilter\":[{ \"key\":" + simpleFilterKey + "," +
						                       "\"value\":"+ simpleFilterValue + "}]" +
						  ",\"orderBy\":[{ \"key\":" + orderKey + ","+
						                  "\"order\":" + order +"}] }")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		accountManager.logout();
	}

	/***
	 * SRDM Common Service Communication API Specification 3.8.2
	 * ロール作成テスト createRoleTest
	 * @throws Exception
	 */
	@Test
	public void createRoleTest() throws Exception{
		String roleName = "\"TestRole-4\"";
		String description = "\"Created by createRoleTest()\"";
		String permissionList[] = {"\"group\"",};
		String mutableRole = "\"true\"";

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* domainIdコンテントボディにセットしない。E0011(必須パラメータチェックエラー)が返ってくる */
		System.out.println("* * * * createRoleTest: E0011 Case * * * *");
		mockMvc.perform(post("/role/createRole/")
				.cookie(cookie)
				.content("{ \"roleName\":"+ roleName +
						  ",\"isRoleCanEdit\":" + mutableRole +
						  ",\"description\":"+ description +
						  ",\"sessionTimeout\":30" +
						  ",\"permissionList\":["+ permissionList[0]+"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainId"));


		/* mutableをコンテントボディにセットしない。E0011(必須パラメータチェックエラー)が返ってくる */
		System.out.println("* * * * createRoleTest: E0011 Case * * * *");
		mockMvc.perform(post("/role/createRole/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ loginDomainId + "\"" +
						  ",\"roleName\":"+ roleName +
						  ",\"description\":"+ description +
						  ",\"sessionTimeout\":30" +
						  ",\"permissionList\":["+ permissionList[0]+"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("isRoleCanEdit"));

		/* mutableの値がない。E0011(必須パラメータチェックエラー)が返ってくる */
		System.out.println("* * * * createRoleTest: E0011 Case * * * *");
		mockMvc.perform(post("/role/createRole/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ loginDomainId + "\"" +
						  ",\"isRoleCanEdit\":null" +
						  ",\"roleName\":"+ roleName +
						  ",\"description\":"+ description +
						  ",\"sessionTimeout\":30" +
						  ",\"permissionList\":["+ permissionList[0]+"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("isRoleCanEdit"));

		/* domainIdの "D-" を除く。E0014(パラメータが有効値でない)が返ってくる */
		System.out.println("* * * * createRoleTest: E0014 Case * * * *");
		mockMvc.perform(post("/role/createRole/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ loginDomainId.replaceAll("D-", "") + "\"" +
						  ",\"isRoleCanEdit\":" + mutableRole +
						  ",\"roleName\":"+ roleName +
						  ",\"description\":"+ description +
						  ",\"sessionTimeout\":30" +
						  ",\"permissionList\":["+ permissionList[0]+"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainId"));

		/* mutableに空("")の値をセットする。E0011(必須パラメータチェックエラー)が返される */
		System.out.println("* * * * createRoleTest: E0014 Case * * * *");
		mutableRole = "\"\"";
		mockMvc.perform(post("/role/createRole/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ loginDomainId + "\"" +
						  ",\"isRoleCanEdit\":" + mutableRole +
						  ",\"roleName\":"+ roleName +
						  ",\"description\":"+ description +
						  ",\"sessionTimeout\":30" +
						  ",\"permissionList\":["+ permissionList[0]+"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));

		/* mutableにtrue/false以外の値をセットする。E0014(パラメータが有効値でない)が返ってくる */
		/**
		 * 不正な値("dummy")を指定している為、E0014が正しいと思われるが、
		 * Pattern指定では、ダブルクォートで括った場合等エラーになってしまう。
		 * そのため、現状の実装では、Patterrn指定を行っておらず、JSON⇒Objectへのマッピング時に
		 * Exceptionを検知している為。
		 */
		System.out.println("* * * * createRoleTest: E0004 Case * * * *");
		mutableRole = "\"dummy\"";
		mockMvc.perform(post("/role/createRole/")
				.cookie(cookie)
				.content("{ \"domainId\":"+ loginDomainId +
						  ",\"isRoleCanEdit\":" + mutableRole +
						  ",\"roleName\":"+ roleName +
						  ",\"description\":"+ description +
						  ",\"sessionTimeout\":30" +
						  ",\"permissionList\":["+ permissionList[0]+"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0004"));

		/* mutableにJSONフォマット不正の値をセットする。E0004(JSONパースエラー)が返ってくる */
		System.out.println("* * * * createRoleTest: E0004 Case * * * *");
		mutableRole = "\"false";
		mockMvc.perform(post("/role/createRole/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ loginDomainId + "\"" +
						  ",\"isRoleCanEdit\":" + mutableRole +
						  ",\"roleName\":"+ roleName +
						  ",\"description\":"+ description +
						  ",\"sessionTimeout\":30" +
						  ",\"permissionList\":["+ permissionList[0]+"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0004"));

		/* sessionTimeoutが無い E0011(必須パラメータチェックエラー)が返ってくる */
		System.out.println("* * * * createRoleTest: E0011 Case * * * *");
		mutableRole = "\"true\"";
		mockMvc.perform(post("/role/createRole/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ loginDomainId + "\"" +
						  ",\"isRoleCanEdit\":" + mutableRole +
						  ",\"roleName\":"+ roleName +
						  ",\"description\":"+ description +
						  ",\"permissionList\":["+ permissionList[0]+"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));

		/* sessionTimeout範囲外(-1) E0014(パラメータが有効値でない)が返ってくる */
		System.out.println("* * * * createRoleTest: E0014 Case * * * *");
		mutableRole = "\"true\"";
		mockMvc.perform(post("/role/createRole/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ loginDomainId + "\"" +
						  ",\"isRoleCanEdit\":" + mutableRole +
						  ",\"roleName\":"+ roleName +
						  ",\"description\":"+ description +
						  ",\"sessionTimeout\":-1" +
						  ",\"permissionList\":["+ permissionList[0]+"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"));

		/* sessionTimeout範囲外(Long.MAX_VALUE + 1) E0014(パラメータが有効値でない)が返ってくる */
		System.out.println("* * * * createRoleTest: E0014 Case * * * *");
		mutableRole = "\"true\"";
		mockMvc.perform(post("/role/createRole/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ loginDomainId + "\"" +
						  ",\"isRoleCanEdit\":" + mutableRole +
						  ",\"roleName\":"+ roleName +
						  ",\"description\":"+ description +
						  ",\"sessionTimeout\":" + String.valueOf(Long.MAX_VALUE + 1) +
						  ",\"permissionList\":["+ permissionList[0]+"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"));

		/* sessionTimeoutが文字列。E0004(JSONパースエラー)が返ってくる */
		System.out.println("* * * * createRoleTest: E0004 Case * * * *");
		mutableRole = "\"true\"";
		mockMvc.perform(post("/role/createRole/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ loginDomainId + "\"" +
						  ",\"isRoleCanEdit\":" + mutableRole +
						  ",\"roleName\":"+ roleName +
						  ",\"description\":"+ description +
						  ",\"sessionTimeout\":\"dummy\"" +
						  ",\"permissionList\":["+ permissionList[0]+"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0004"));

		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * createRoleTest: E0021 Case * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);
		mutableRole = "\"true\"";

		mockMvc.perform(post("/role/createRole/")
				.cookie(errorCookie)
				.content("{ \"domainId\":\""+ loginDomainId + "\"" +
						  ",\"isRoleCanEdit\":" + mutableRole +
						  ",\"roleName\":"+ roleName +
						  ",\"description\":"+ description +
						  ",\"sessionTimeout\":30" +
						  ",\"permissionList\":["+ permissionList[0]+"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		accountManager.logout();

	}

	/***
	 * SRDM Common Service Communication API Specification 3.8.3
	 * ロール情報取得テスト getRoleTest
	 * @throws Exception
	 */
	@Test
	public void getRoleTest() throws Exception {

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		String roleId = "\"R-c0c9c026-ba24-4698-8ccb-ff42ed8a26f2\"";	// admin


		/* domainIdコンテントボディにセットしない。E0011(必須パラメータチェックエラー)が返ってくる */
		System.out.println("* * * * getRoleTest: E0011 Case * * * *");
		mockMvc.perform(post("/role/getRole/")
				.cookie(cookie)
				.content("{ \"roleId\":" + roleId +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainId"));



		/* domainIdの "D-" を除く。E0014(パラメータが有効値でない)が返ってくる */
		System.out.println("* * * * getRoleTest: E0014 Case * * * *");
		mockMvc.perform(post("/role/getRole/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ loginDomainId.replaceAll("D-", "") + "\"" +
						  ",\"roleId\":" + roleId +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainId"));


		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * getRoleTest: E0021 Case * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/role/getRole/")
				.cookie(errorCookie)
				.content("{ \"domainId\":\""+ loginDomainId + "\"" +
						  ",\"roleId\":" + "\"R-c0c9c026-ba24-4698-8ccb-ff42ed8a26f5\"" +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		accountManager.logout();

	}

	/***
	 * SRDM Common Service Communication API Specification 3.8.4
	 * ロール情報更新テスト editRoleTest
	 * @throws Exception
	 */
	@Test
	public void editRoleTest() throws Exception {

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		String roleId = "\"R-83da388c-b564-4d67-aabc-cb93cd08c39c\"";	// テストで作成したロール
		String editRoleName = "\"TestRole-2\"";
		String editDescription = "\"Edited by editRoleTest()\"";
		String editGrantedPermissionList[] = {""};
		String editDeprivedPermissionList[] = {""};
		String mutableRole = "\"false\"";


		/* domainIdをコンテントボディにセットしない。E0011(必須パラメータチェックエラー)が返ってくる */
		System.out.println("* * * * editRoleTest: E0011 Case * * * *");
		mockMvc.perform(post("/role/editRole/")
				.cookie(cookie)
				.content("{ \"roleId\":" + roleId +
						  ",\"roleName\":"+ editRoleName +
						  ",\"isRoleCanEdit\":" + mutableRole +
						  ",\"description\":"+ editDescription +
						  ",\"sessionTimeout\":30" +
						  ",\"grantedPermissionList\":[" + editGrantedPermissionList[0] +"]"+
						  ",\"deprivedPermissionList\":["+ editDeprivedPermissionList[0] +"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainId"));

		/* domainIdの "D-"を除く。E0014(パラメータが有効値でない)が返ってくる */
		System.out.println("* * * * editRoleTest: E0014 Case * * * *");
		mockMvc.perform(post("/role/editRole/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ loginDomainId.replaceAll("D-", "") + "\"" +
						  ",\"roleId\":" + roleId +
						  ",\"roleName\":"+ editRoleName +
						  ",\"isRoleCanEdit\":" + mutableRole +
						  ",\"description\":"+ editDescription +
						  ",\"sessionTimeout\":30" +
						  ",\"grantedPermissionList\":[" + editGrantedPermissionList[0] +"]"+
						  ",\"deprivedPermissionList\":["+ editDeprivedPermissionList[0] +"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"));

		/* mutableRoleに空("")の値をセットする。E0011(必須パラメータチェックエラー)が返される */
		System.out.println("* * * * editRoleTest: E0014 Case * * * *");
		mutableRole = "\"\"";
		mockMvc.perform(post("/role/editRole/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ loginDomainId + "\"" +
				          ",\"roleId\":" + roleId +
						  ",\"roleName\":"+ editRoleName +
						  ",\"isRoleCanEdit\":" + mutableRole +
						  ",\"description\":"+ editDescription +
						  ",\"sessionTimeout\":30" +
						  ",\"grantedPermissionList\":[" + editGrantedPermissionList[0] +"]"+
						  ",\"deprivedPermissionList\":["+ editDeprivedPermissionList[0] +"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));

		/* mutableRoleに無効な値をセットする。E0014(パラメータが有効値でない)が返ってくる */
		/**
		 * 不正な値("dummy")を指定している為、E0014が正しいと思われるが、
		 * Pattern指定では、ダブルクォートで括った場合等エラーになってしまう。
		 * そのため、現状の実装では、Patterrn指定を行っておらず、JSON⇒Objectへのマッピング時に
		 * Exceptionを検知している為。
		 */
		System.out.println("* * * * editRoleTest: E0004 Case * * * *");
		mutableRole = "\"dummy\"";
		mockMvc.perform(post("/role/editRole/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ loginDomainId + "\"" +
				          ",\"roleId\":" + roleId +
						  ",\"roleName\":"+ editRoleName +
						  ",\"isRoleCanEdit\":" + mutableRole +
						  ",\"description\":"+ editDescription +
						  ",\"sessionTimeout\":30" +
						  ",\"grantedPermissionList\":[" + editGrantedPermissionList[0] +"]"+
						  ",\"deprivedPermissionList\":["+ editDeprivedPermissionList[0] +"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0004"));

		/* mutableRoleにJSONフォーマット不正の値をセットする。E0004(JSONパースエラー)が返ってくる */
		System.out.println("* * * * editRoleTest: E0014 Case * * * *");
		mutableRole = "\"false";
		mockMvc.perform(post("/role/editRole/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ loginDomainId + "\"" +
				          ",\"roleId\":" + roleId +
						  ",\"roleName\":"+ editRoleName +
						  ",\"isRoleCanEdit\":" + mutableRole +
						  ",\"description\":"+ editDescription +
						  ",\"sessionTimeout\":30" +
						  ",\"grantedPermissionList\":[" + editGrantedPermissionList[0] +"]"+
						  ",\"deprivedPermissionList\":["+ editDeprivedPermissionList[0] +"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0004"));

		/* sessionTimeoutが無い E0011(必須パラメータチェックエラー)が返ってくる */
		System.out.println("* * * * editRoleTest: E0011 Case * * * *");
		mutableRole = "\"false\"";
		mockMvc.perform(post("/role/editRole/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ loginDomainId + "\"" +
				          ",\"roleId\":" + roleId +
						  ",\"roleName\":"+ editRoleName +
						  ",\"isRoleCanEdit\":" + mutableRole +
						  ",\"description\":"+ editDescription +
						  ",\"grantedPermissionList\":[" + editGrantedPermissionList[0] +"]"+
						  ",\"deprivedPermissionList\":["+ editDeprivedPermissionList[0] +"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"));

		/* sessionTimeout範囲外(-2) E0014(パラメータが有効値でない)が返ってくる */
		System.out.println("* * * * editRoleTest: E0014 Case * * * *");
		mutableRole = "\"false\"";
		mockMvc.perform(post("/role/editRole/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ loginDomainId + "\"" +
				          ",\"roleId\":" + roleId +
						  ",\"roleName\":"+ editRoleName +
						  ",\"isRoleCanEdit\":" + mutableRole +
						  ",\"description\":"+ editDescription +
						  ",\"sessionTimeout\":-2" +
						  ",\"grantedPermissionList\":[" + editGrantedPermissionList[0] +"]"+
						  ",\"deprivedPermissionList\":["+ editDeprivedPermissionList[0] +"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"));

		/* sessionTimeout範囲外(Long.MAX_VALUE + 1) E0014(パラメータが有効値でない)が返ってくる */
		System.out.println("* * * * editRoleTest: E0014 Case * * * *");
		mutableRole = "\"false\"";
		mockMvc.perform(post("/role/editRole/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ loginDomainId + "\"" +
				          ",\"roleId\":" + roleId +
						  ",\"roleName\":"+ editRoleName +
						  ",\"isRoleCanEdit\":" + mutableRole +
						  ",\"description\":"+ editDescription +
						  ",\"sessionTimeout\":" + String.valueOf(Long.MAX_VALUE + 1) +
						  ",\"grantedPermissionList\":[" + editGrantedPermissionList[0] +"]"+
						  ",\"deprivedPermissionList\":["+ editDeprivedPermissionList[0] +"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"));

		/* sessionTimeoutが文字列。E0004(JSONパースエラー)が返ってくる */
		System.out.println("* * * * editRoleTest: E0004 Case * * * *");
		mutableRole = "\"false\"";
		mockMvc.perform(post("/role/editRole/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ loginDomainId + "\"" +
				          ",\"roleId\":" + roleId +
						  ",\"roleName\":"+ editRoleName +
						  ",\"isRoleCanEdit\":" + mutableRole +
						  ",\"description\":"+ editDescription +
						  ",\"sessionTimeout\":\"dummy\"" +
						  ",\"grantedPermissionList\":[" + editGrantedPermissionList[0] +"]"+
						  ",\"deprivedPermissionList\":["+ editDeprivedPermissionList[0] +"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0004"));

		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * editRoleTest: E0021 Case * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");
		mutableRole = "\"false\"";
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/role/editRole/")
				.cookie(errorCookie)
				.content("{ \"domainId\":\""+ loginDomainId + "\"" +
						  ",\"roleId\":" + roleId +
						  ",\"roleName\":"+ editRoleName +
						  ",\"isRoleCanEdit\":" + mutableRole +
						  ",\"description\":"+ editDescription +
						  ",\"sessionTimeout\":30" +
						  ",\"grantedPermissionList\":[" + editGrantedPermissionList[0] +"]"+
						  ",\"deprivedPermissionList\":["+ editDeprivedPermissionList[0] +"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		accountManager.logout();
	}

	/***
	 * SRDM Common Service Communication API Specification 3.8.5
	 * ロール削除テスト deleteRoleTest
	 * @throws Exception
	 */
	@Test
	public void deleteRoleTest() throws Exception {

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		String deleteRoleList[] = {"\"R-83da388c-b564-4d67-aabc-cb93cd08c39c\""};


		/* domainIdをコンテントボディにセットしない。E0011(必須パラメータチェックエラー)が返ってくる */
		System.out.println("* * * * deleteRoleTest: E0011 Case * * * *");
		mockMvc.perform(post("/role/deleteRole/")
				.cookie(cookie)
				.content("{ \"roleIdList\":["+ deleteRoleList[0] +"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainId"));



		/* domainIdの "D-"を除く。E0014(パラメータが有効値でない)が返ってくる */
		System.out.println("* * * * deleteRoleTest: E0014 Case * * * *");
		mockMvc.perform(post("/role/deleteRole/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ loginDomainId.replaceAll("D-", "") + "\"" +
						  ",\"roleIdList\":["+ deleteRoleList[0] +"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("domainId"));


		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * deleteRoleTest: E0021 Case * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/role/deleteRole/")
				.cookie(errorCookie)
				.content("{ \"domainId\":"+ "\"D-4dddb97a-3272-490f-b0fb-8762eaa69d47\"" +
						  ",\"roleIdList\":["+ deleteRoleList[0] +"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		accountManager.logout();
	}
}
