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
public class DomainTestNoDB {

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

		/* テストケース1 */
		System.out.println("* * * * getDomainListTest:Normal1 Case * * * *");
		mockMvc.perform(post("/domain/getDomainList/")
				.cookie(cookie)
				.content("{ \"startIndex\":"+ startIndex +
						  ",\"count\":" + count +
						  ",\"domainId\":"+ domainId +"}")
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

		/* テストケース1 */
		System.out.println("* * * * createDomainListTest:Normal1 Case * * * *");
		mockMvc.perform(post("/domain/createDomain/")
				.cookie(cookie)
				.content("{ \"domainName\":"+ domainName +
						  ",\"parentDomainId\":"+ parentDomainId +
						  ",\"targetGroupId\":"+ targetGroupId +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());

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

		/* テストケース1 */
		System.out.println("* * * * getDomainTest:Normal1 Case * * * *");
		mockMvc.perform(post("/domain/getDomain/")
				.cookie(cookie)
				.content("{ \"domainId\":"+ loginDomainId +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print());

		accountManager.logout();
	}


	/***
	 * SRDM Common Service Communication API Specification 2.5.0
	 * ドメイン情報編集テスト editDomainTest
	 * @throws Exception
	 */
	@Test
	public void editDomainTest() throws Exception{

		String testDomainId1 = "\"D-"+ UUID.randomUUID().toString() +"\"";

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* テストケース1 */
		System.out.println("* * * * editDomainTest:Normal1 Case * * * *");
		String editDomainId = testDomainId1;
		String editDomainName = "\"TestDomain-3\"";
		String editTargetGroupId = "\"0\"";


		System.out.println("* * * * editDomainListTest:Normal1 Case * * * *");
		mockMvc.perform(post("/domain/editDomain/")
				.cookie(cookie)
				.content("{ \"domainId\":"+ editDomainId +
				          ",\"domainName\":"+ editDomainName +
				          ",\"targetGroupId\":"+ editTargetGroupId +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());

		accountManager.logout();
	}

	/***
	 * SRDM Common Service Communication API Specification 3.7.5
	 * ドメイン削除テスト deleteDomainTest
	 * @throws Exception
	 */
	@Test
	public void deleteDomainTest() throws Exception{

		String testDomainId1 = "\"D-" + UUID.randomUUID().toString() +"\"";
		String testDomainId2 = "\"D-"+ UUID.randomUUID().toString() +"\"";

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		String deleteDomainList[] = {testDomainId1, testDomainId2};

		/* テストケース2 複数ドメイン削除*/
		System.out.println("* * * * deleteDomainTest:Normal2 Case * * * *");
		mockMvc.perform(post("/domain/deleteDomain/")
				.cookie(cookie)
				.content("{ \"domainIdList\":"+ "[" + deleteDomainList[0] + "," + deleteDomainList[1] + "]" + "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());

		accountManager.logout();

	}

	/***
	 * SRDM Common Service Communication API Specification 3.13.1
	 * ドメイン画面表示パターンテスト getThemeSettingTest
	 * @throws Exception
	 */
	@Test
	public void getThemeSettingTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* テストケース1 */
		System.out.println("* * * * getThemeSettingTest:Normal1 Case * * * *");
		mockMvc.perform(post("/domain/getThemeSetting/")
				.cookie(cookie)
				.content("{}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty())
				.andExpect(jsonPath("$.theme").isNotEmpty());

		accountManager.logout();
	}

	/***
	 * SRDM Common Service Communication API Specification 3.13.2
	 * ドメイン画面表示パターンテスト getThemeSettingTest
	 * @throws Exception
	 */
	@Test
	public void setThemeSettingTest() throws Exception{

		String theme = "\"Enterprise\"";

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* テストケース1 */
		System.out.println("* * * * setThemeSettingTest:Normal1 Case * * * *");
		mockMvc.perform(post("/domain/setThemeSetting/")
				.cookie(cookie)
				.content("{ \"theme\":" + theme + "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());


		/* テストケース2 */
		System.out.println("* * * * setThemeSettingTest:Normal2 Case * * * *");
		theme = "\"Tahoe\"";
		mockMvc.perform(post("/domain/setThemeSetting/")
				.cookie(cookie)
				.content("{ \"theme\":" + theme + "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());


		/* テストケース3 */
		System.out.println("* * * * setThemeSettingTest:Normal3 Case * * * *");
		theme = "\"EnterpriseBlue\"";
		mockMvc.perform(post("/domain/setThemeSetting/")
				.cookie(cookie)
				.content("{ \"theme\":" + theme + "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());


		/* テストケース4 */
		System.out.println("* * * * setThemeSettingTest:Normal4 Case * * * *");
		theme = "\"EnterpriseRed\"";
		mockMvc.perform(post("/domain/setThemeSetting/")
				.cookie(cookie)
				.content("{ \"theme\":" + theme + "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());


		/* テストケース5 */
		System.out.println("* * * * setThemeSettingTest:Normal5 Case * * * *");
		theme = "\"EnterpriseGreen\"";
		mockMvc.perform(post("/domain/setThemeSetting/")
				.cookie(cookie)
				.content("{ \"theme\":" + theme + "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());


		/* テストケース6 */
		System.out.println("* * * * setThemeSettingTest:Normal6 Case * * * *");
		theme = "\"Graphite\"";
		mockMvc.perform(post("/domain/setThemeSetting/")
				.cookie(cookie)
				.content("{ \"theme\":" + theme + "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());

		accountManager.logout();

	}

}
