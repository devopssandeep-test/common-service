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
public class RoleTestNoDB {

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

		/* テストケース1 ロールリスト取得 */
		System.out.println("* * * * getRoleListTest:Normal1 Case * * * *");
		mockMvc.perform(post("/role/getRoleList/")
				.cookie(cookie)
				.content("{ \"startIndex\":\""+startIndex+"\""+
						  ",\"count\":\""+ count +"\"" +
						  ",\"domainId\":\""+ loginDomainId +"\"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());



		/* テストケース2 simpleFilterを使い、降順にしてロールリスト取得 */
		System.out.println("* * * * getRoleListTest:Normal2 Case * * * *");
		mockMvc.perform(post("/role/getRoleList/")
				.cookie(cookie)
				.content("{ \"startIndex\":\"" + startIndex + "\""+
						  ",\"count\":\""+ count +"\"" +
						  ",\"domainId\":\""+ loginDomainId +"\"" +
						  ",\"simpleFilter\":[{ \"key\":" + simpleFilterKey + "," +
						                       "\"value\":"+ simpleFilterValue + "}]" +
						  ",\"orderBy\":[{ \"key\":" + orderKey + ","+
						                  "\"order\":" + order +"}] }")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());


		/* テストケース3 simpleFilterを使い、description:For Developerでロールリスト取得 */
		simpleFilterKey = "\"description\"";
		simpleFilterValue = "\"For Administrator account.\"";
		System.out.println("* * * * getRoleListTest:Normal3 Case * * * *");
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
				.andExpect(jsonPath("$.common.errorList").isEmpty());

		accountManager.logout();
	}

	/***
	 * SRDM Common Service Communication API Specification 3.8.2
	 * ロール作成テスト createRoleTest
	 * @throws Exception
	 */
	@Test	//単体チェック以外はアノテーションをコメントアウト
	public void createRoleTest() throws Exception{
		String roleName = "\"TestRole-1\"";
		String description = "\"Created by createRoleTest()\"";
		String permissionList[] = {"\"group\"","\"system\"","\"fileDistribution\"","\"advancedFeatureBasic\"",
									 "\"deviceCloning\"","\"powerManagement\"","\"account\""};

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* テストケース1 */
		System.out.println("* * * * createRoleTest:Normal1 Case * * * *");
		mockMvc.perform(post("/role/createRole/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ TestDomain.topDomainId + "\"" +
						  ",\"roleName\":"+ roleName +
						  ",\"isRoleCanEdit\":true" +
						  ",\"sessionTimeout\":30" +
						  ",\"description\":"+ description +
						  ",\"permissionList\":["+ permissionList[1] + "," +
						  						   permissionList[2] + "," +
						  						   permissionList[3] + "," +
						  						   permissionList[4] + "]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty())
				.andExpect(jsonPath("$.roleId").isNotEmpty());

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

		/* テストケース1  */
		System.out.println("* * * * getRoleTest:Normal1 Case * * * *");
		mockMvc.perform(post("/role/getRole/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ loginDomainId +"\"" +
						  ",\"roleId\":" + roleId +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.common.errorList").isEmpty())
				.andDo(print());

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

		String roleId = "\""+ TestRole.adminRoleId +"\"";	// テストで作成したロール
		String editRoleName = "\"TestRole3-Edited\"";
		String editDescription = "\"Edited by editRoleTest()\"";

		/* テストケース1  */
		System.out.println("* * * * editRoleTest:Normal1 Case * * * *");
		String mutableRole = "\"false\"";
		String domainId = "\"" + TestDomain.topDomainId + "\"";
		mockMvc.perform(post("/role/editRole/")
				.cookie(cookie)
				.content("{ \"domainId\":"+ domainId +
						  ",\"roleId\":" + roleId +
						  ",\"roleName\":"+ editRoleName +
						  ",\"isRoleCanEdit\":" + mutableRole +
						  ",\"sessionTimeout\":-1" +
						  ",\"description\":"+ editDescription +
						  ",\"grantedPermissionList\":[]"+
						  ",\"deprivedPermissionList\":[]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());

		/* テストケース2  */
		System.out.println("* * * * editRoleTest:Normal2 Case * * * *");
		mockMvc.perform(post("/role/editRole/")
				.cookie(cookie)
				.content("{ \"domainId\":"+ domainId +
						  ",\"roleId\":" + roleId +
						  ",\"roleName\":"+ editRoleName +
						  ",\"isRoleCanEdit\":" + mutableRole +
						  ",\"sessionTimeout\":-1" +
						  ",\"description\":"+ editDescription +
						  ",\"grantedPermissionList\":[]"+
						  ",\"deprivedPermissionList\":[]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());

		/* テストケース3  */
		System.out.println("* * * * editRoleTest:Normal3 Case * * * *");
		mutableRole = "\"true\"";
		mockMvc.perform(post("/role/editRole/")
				.cookie(cookie)
				.content("{ \"domainId\":"+ domainId +
						  ",\"roleId\":" + roleId +
						  ",\"roleName\":"+ editRoleName +
						  ",\"isRoleCanEdit\":" + mutableRole +
						  ",\"sessionTimeout\":-1" +
						  ",\"description\":"+ editDescription +
						  ",\"grantedPermissionList\":[]"+
						  ",\"deprivedPermissionList\":[]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());

		accountManager.logout();

	}

	/***
	 * SRDM Common Service Communication API Specification 3.8.5
	 * ロール削除テスト deleteRoleTest
	 * @throws Exception
	 */
	@Test		//単体チェック以外はアノテーションをコメントアウト
	public void deleteRoleTest() throws Exception {

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		String deleteRoleList[] = {
				"\"R-"+ UUID.randomUUID().toString() +"\"",
				"\"R-"+ UUID.randomUUID().toString() +"\"",
				"\"R-"+ UUID.randomUUID().toString() +"\""
			};

		/* テストケース1 */
		System.out.println("* * * * deleteRoleTest:Normal1 Case * * * *");
		mockMvc.perform(post("/role/deleteRole/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ loginDomainId +"\"" +
						  ",\"roleIdList\":["+ deleteRoleList[0] +"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.common.errorList").isEmpty())
				.andDo(print());


		/* テストケース2 */
		System.out.println("* * * * deleteRoleTest:Normal2 Case * * * *");
		mockMvc.perform(post("/role/deleteRole/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ loginDomainId + "\"" +
						  ",\"roleIdList\":["+ deleteRoleList[1] + "," +
						  					   deleteRoleList[2] +"]}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.common.errorList").isEmpty())
				.andDo(print());

		accountManager.logout();
	}
}
