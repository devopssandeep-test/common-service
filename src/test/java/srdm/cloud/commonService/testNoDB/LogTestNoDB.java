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
public class LogTestNoDB {

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
		String simpleFilterKey = "\"accountName\"";
		String simpleFilterValue = "\"admin\"";
		String order = "\"descending\"";
		String orderByKey = "\"timestamp\"";

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* テストケース1 操作ログを10件取得する */
		System.out.println("* * * * getOperationLogTest:Normal1 Case * * * *");
		mockMvc.perform(post("/log/getOperationLog/")
				.cookie(cookie)
				.content("{ \"startIndex\":\"" + startIndex + "\"" +
						  ",\"count\":\"" + count + "\"" +
						  ",\"domainId\":\""+ domainId + "\"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());

		/* テストケース2 accountName:adminでフィルターして、order:descending orderByKey:timestamp で取得する */
		System.out.println("* * * * getOperationLogTest:Normal2 Case * * * *");
		mockMvc.perform(post("/log/getOperationLog/")
				.cookie(cookie)
				.content("{ \"startIndex\":\"" + startIndex + "\"" +
						  ",\"count\":\"" + count + "\"" +
						  ",\"domainId\":\""+ domainId + "\"" +
						  ",\"simpleFilter\":[{ \"key\": "+ simpleFilterKey + "," +
						                       "\"value\":"+ simpleFilterValue + "}]" +
						  ",\"orderBy\":[{ \"key\":" + orderByKey + "," +
						  				  "\"order\":" + order +"}] }")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());


		/* テストケース3 kind:informationでフィルターして、order:descending orderByKey:domainId で取得する */
		simpleFilterKey = "\"kind\"";
		simpleFilterValue = "\"information\"";
		orderByKey = "\"domainId\"";

		System.out.println("* * * * getOperationLogTest:Normal3 Case * * * *");
		mockMvc.perform(post("/log/getOperationLog/")
				.cookie(cookie)
				.content("{ \"startIndex\":\"" + startIndex + "\"" +
						  ",\"count\":\"" + count + "\"" +
						  ",\"domainId\":\""+ domainId +"\"" +
						  ",\"simpleFilter\":[{ \"key\": "+ simpleFilterKey + "," +
						                       "\"value\":"+ simpleFilterValue + "}]" +
						  ",\"orderBy\":[{ \"key\":" + orderByKey + "," +
						  				  "\"order\":" + order +"}] }")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());


		/* テストケース4 domainId でフィルターして、order:descending orderByKey:accountName で取得する */
		simpleFilterKey = "\"domainId\"";
		simpleFilterValue = "\"" + loginDomainId + "\"";
		orderByKey = "\"accountName\"";

		System.out.println("* * * * getOperationLogTest:Normal4 Case * * * *");
		mockMvc.perform(post("/log/getOperationLog/")
				.cookie(cookie)
				.content("{ \"startIndex\":" + startIndex +
						  ",\"count\":" + count +
						  ",\"domainId\":\""+ domainId + "\"" +
						  ",\"simpleFilter\":[{ \"key\": "+ simpleFilterKey + "," +
						                       "\"value\":"+ simpleFilterValue + "}]" +
						  ",\"orderBy\":[{ \"key\":" + orderByKey + "," +
						  				  "\"order\":" + order +"}] }")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());


		/* テストケース5 operation:login でフィルターして、order:descending orderByKey:code で取得する */
		simpleFilterKey = "\"operation\"";
		simpleFilterValue = "\"login\"";
		orderByKey = "\"code\"";

		System.out.println("* * * * getOperationLogTest:Normal5 Case * * * *");
		mockMvc.perform(post("/log/getOperationLog/")
				.cookie(cookie)
				.content("{ \"startIndex\":\"" + startIndex + "\"" +
						  ",\"count\":\"" + count + "\"" +
						  ",\"domainId\":\""+ domainId + "\"" +
						  ",\"simpleFilter\":[{ \"key\": "+ simpleFilterKey + "," +
						                       "\"value\":"+ simpleFilterValue + "}]" +
						  ",\"orderBy\":[{ \"key\":" + orderByKey + "," +
						  				  "\"order\":" + order +"}] }")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());


		/* テストケース6 code:0000 でフィルターして、order:descending orderByKey:operation で取得する */
		simpleFilterKey = "\"code\"";
		simpleFilterValue = "\"0000\"";
		orderByKey = "\"operation\"";
		order="\"ascending\"";

		System.out.println("* * * * getOperationLogTest:Normal6 Case * * * *");
		mockMvc.perform(post("/log/getOperationLog/")
				.cookie(cookie)
				.content("{ \"startIndex\":\"" + startIndex + "\"" +
						  ",\"count\":\"" + count + "\"" +
						  ",\"domainId\":\""+ domainId + "\"" +
						  ",\"simpleFilter\":[{ \"key\": "+ simpleFilterKey + "," +
						                       "\"value\":"+ simpleFilterValue + "}]" +
						  ",\"orderBy\":[{ \"key\":" + orderByKey + "," +
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
	 * SRDM Common Service Communication API Specification 3.10.2
	 * 操作ログ削除テスト deleteOpearationLogTest
	 * @throws Exception
	 */
	@Test
	public void deleteOperationLogTest() throws Exception{
		String domainId = loginDomainId;

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* テストケース1 */
		System.out.println("* * * * deleteOperationLogTest:Normal1 Case * * * *");
		mockMvc.perform(post("/log/deleteOperationLog/")
				.cookie(cookie)
				.content("{ \"domainId\":\""+ domainId +"\"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());

		accountManager.logout();
	}

	/***
	 * SRDM Common Service Communication API Specification 3.10.3
	 * システム管理ログ取得テスト deleteOpearationLogTest
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

		/* テストケース1 システム管理ログ10件取得 */
		System.out.println("* * * * getSystemLogTest:Normal1 Case * * * *");
		mockMvc.perform(post("/log/getSystemLog/")
				.cookie(cookie)
				.content("{ \"startIndex\":\""+ startIndex + "\"" +
				          ",\"count\":\""+ count + "\"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());

		/* テストケース2 kind:error でフィルターして、order:descending orderKey:timestamp で取得する */
		System.out.println("* * * * getSystemLogTest:Normal2 Case * * * *");
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
				.andExpect(jsonPath("$.common.errorList").isEmpty());


		/* テストケース3 accountName:adminでフィルターして、order:descending orderByKey:timestamp で取得する */
		System.out.println("* * * * getSystemLogTest:Normal3 Case * * * *");
		mockMvc.perform(post("/log/getSystemLog/")
				.cookie(cookie)
				.content("{ \"startIndex\":\"" + startIndex + "\"" +
						  ",\"count\":\"" + count + "\"" +
						  ",\"simpleFilter\":[{ \"key\": "+ simpleFilterKey + "," +
						                       "\"value\":"+ simpleFilterValue + "}]" +
						  ",\"orderBy\":[{ \"key\":" + orderKey + "," +
						  				  "\"order\":" + order +"}] }")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());


		/* テストケース4 kind:informationでフィルターして、order:descending orderByKey:domainId で取得する */
		simpleFilterKey = "\"kind\"";
		simpleFilterValue = "\"information\"";
		orderKey = "\"domainId\"";

		System.out.println("* * * * getSystemLogTest:Normal4 Case * * * *");
		mockMvc.perform(post("/log/getSystemLog/")
				.cookie(cookie)
				.content("{ \"startIndex\":\"" + startIndex + "\"" +
						  ",\"count\":\"" + count + "\"" +
						  ",\"simpleFilter\":[{ \"key\": "+ simpleFilterKey + "," +
						                       "\"value\":"+ simpleFilterValue + "}]" +
						  ",\"orderBy\":[{ \"key\":" + orderKey + "," +
						  				  "\"order\":" + order +"}] }")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());


		/* テストケース5 domainId でフィルターして、order:descending orderByKey:accountName で取得する */
		simpleFilterKey = "\"domainId\"";
		simpleFilterValue = "\"" + loginDomainId + "\"";
		orderKey = "\"accountName\"";

		System.out.println("* * * * getSystemLogTest:Normal5 Case * * * *");
		mockMvc.perform(post("/log/getSystemLog/")
				.cookie(cookie)
				.content("{ \"startIndex\":\"" + startIndex + "\"" +
						  ",\"count\":\"" + count + "\"" +
						  ",\"simpleFilter\":[{ \"key\": "+ simpleFilterKey + "," +
						                       "\"value\":"+ simpleFilterValue + "}]" +
						  ",\"orderBy\":[{ \"key\":" + orderKey + "," +
						  				  "\"order\":" + order +"}] }")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());


		/* テストケース6 operation:login でフィルターして、order:descending orderByKey:code で取得する */
		simpleFilterKey = "\"operation\"";
		simpleFilterValue = "\"login\"";
		orderKey = "\"code\"";
		order="\"ascending\"";

		System.out.println("* * * * getSystemLogTest:Normal6 Case * * * *");
		mockMvc.perform(post("/log/getSystemLog/")
				.cookie(cookie)
				.content("{ \"startIndex\":\"" + startIndex + "\"" +
						  ",\"count\":\"" + count + "\"" +
						  ",\"simpleFilter\":[{ \"key\": "+ simpleFilterKey + "," +
						                       "\"value\":"+ simpleFilterValue + "}]" +
						  ",\"orderBy\":[{ \"key\":" + orderKey + "," +
						  				  "\"order\":" + order +"}] }")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());


		/* テストケース7 code:0000 でフィルターして、order:descending orderByKey:operation で取得する */
		simpleFilterKey = "\"code\"";
		simpleFilterValue = "\"0000\"";
		orderKey = "\"operation\"";

		System.out.println("* * * * getSystemLogTest:Normal7 Case * * * *");
		mockMvc.perform(post("/log/getSystemLog/")
				.cookie(cookie)
				.content("{ \"startIndex\":\"" + startIndex + "\"" +
						  ",\"count\":\"" + count + "\"" +
						  ",\"simpleFilter\":[{ \"key\": "+ simpleFilterKey + "," +
						                       "\"value\":"+ simpleFilterValue + "}]" +
						  ",\"orderBy\":[{ \"key\":" + orderKey + "," +
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
	 * SRDM Common Service Communication API Specification 3.10.4
	 * システム管理ログ削除テスト deleteSystemLogTest
	 * @throws Exception
	 */
	//@Test //単体チェック以外はアノテーションをコメントアウト
	public void deleteSystemLogTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* テストケース1 */
		System.out.println("* * * * deleteSystemLogTest:Normal1 Case * * * *");
		mockMvc.perform(post("/log/deleteSystemLog/")
				.cookie(cookie)
				.content("{}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());

		accountManager.logout();
	}


	/***
	 * SRDM Common Service Communication API Specification 3.10.5
	 * 操作ログ詳細項目取得テスト getOperationLogDetailTest
	 * @throws Exception
	 */
	@Test
	public void getOperationDetailTest() throws Exception{

		String domainId = loginDomainId;
		String logId = "\"" + UUID.randomUUID().toString() + "\"" ;

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* テストケース1 */
		System.out.println("* * * * getSystemLogTest:Normal1 Case * * * *");
		mockMvc.perform(post("/log/getOperationLogDetail/")
				.cookie(cookie)
				.content("{ \"domainId\":\"" + domainId + "\"" +
						  ",\"logId\":" + logId + "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());

		accountManager.logout();

	}

	/***
	 * SRDM Common Service Communication API Specification 3.10.6
	 * 操作ログ詳細項目取得テスト getSystemLogDetailTest
	 * @throws Exception
	 */
	@Test
	public void getSystemLogDetailTest() throws Exception{

		String logId = "\""+ UUID.randomUUID().toString() +"\"";

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* テストケース1 */
		System.out.println("* * * * getSystemLogTest:Normal1 Case * * * *");
		mockMvc.perform(post("/log/getSystemLogDetail/")
				.cookie(cookie)
				.content("{ \"logId\":"+ logId +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty());

		accountManager.logout();
	}

}
