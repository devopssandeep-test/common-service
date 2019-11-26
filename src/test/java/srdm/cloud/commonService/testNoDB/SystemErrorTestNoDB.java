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
public class SystemErrorTestNoDB {

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
	 * SRDM Common Service Communication API Specification 3.5.1
	 * 空き容量チェックテスト getStorageStatusTest
	 * @throws Exception
	 */
	@Test
	public void getStorageStatusTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * getStorageStatusTest: E0021 Case * * * *");

		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");	//エラー
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/system/getStorageStatus/")
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
}
