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
public class MaintenanceErrorTestNoDB {

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
	 * SRDM Common Service Communication API Specification 3.4.1
	 * メンテナンス情報取得テスト getMaintenanceInfoTest
	 * @throws Exception
	 */
	@Test
	public void getMaintenanceInfoTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * getMaintenanceInfoTest: E0021 Case * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");	//エラー
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/maintenance/getMaintenanceInfo/")
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
	 * SRDM Common Service Communication API Specification 3.4.2
	 * メンテナンス情報登録テスト setMaintenanceInfoTest
	 * @throws Exception
	 */
	@Test
	public void setMaintenanceInfoTest() throws Exception{

		String maintenanceMessage = "\"11/4 Test MaintenanceMessage!!\"";

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* domainIdをコンテントボディにセットしない。E0011(必須パラメータチェックエラー)が返ってくる */
		System.out.println("* * * * setMaintenanceInfoTest: E0011 Case * * * *");
		mockMvc.perform(post("/maintenance/setMaintenanceInfo/")
				.cookie(cookie)
				.content("{}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("message"));


		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * setMaintenanceInfoTest: E0021 Case * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");	//エラー
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/maintenance/setMaintenanceInfo/")
				.cookie(errorCookie)
				.content("{\"message\":"+ maintenanceMessage +"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0021"));

		accountManager.logout();

	}

	/***
	 * SRDM Common Service Communication API Specification 3.4.3
	 * 定期メンテナンス設定取得テスト getScheduledMaintenanceSettingsTest
	 * @throws Exception
	 */
	@Test
	public void getScheduledMaintenanceSettingsTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * getScheduledMaintenanceSettingsTest: E0021 Case * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");	//エラー
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/maintenance/getScheduledMaintenanceSettings/")
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
	 * SRDM Common Service Communication API Specification 3.4.4
	 * 定期メンテナンス設定テスト setScheduledMaintenanceSettingsTest
	 * @throws Exception
	 */
	@Test
	public void setScheduledMaintenanceSettingsTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		boolean execFlag = false;
		boolean sendFlag = true;
		String execType = "\"everyday\"";
		String weekDay[] = {"\"MONDAY\"","\"TUESDAY\"","\"WEDNESDAY\"","\"THURSDAY\"","\"FRIDAY\"","\"SATURDAY\"","\"SUNDAY\""};
		//long monthDate[] = {2,20};
		long execDateTimestamp = 1473666200240L;
		long execTimeHour = 22;
		long execTimeMinute = 22;
		String timeZoneId = "\"Etc/GMT+0\"";
		String dateTimeFormat = "\"MM/dd/yyyy HH:mm:ss\"";
		String language = "\"en\"";
		String toAddress = "\"aaaa@bbb.com\"";
		String ccAddress = "\"\"";
		String bccAddress = "\"\"";



		/* execFloag をコンテントボディにセットしない。E0011(必須パラメータチェックエラー)が返ってくる */
		System.out.println("* * * * setMaintenanceInfoTest: E0011 Case * * * *");
		mockMvc.perform(post("/maintenance/setScheduledMaintenanceSettings/")
				.cookie(cookie)
				.content("{ \"sendFlag\":\""+ sendFlag + "\"" +
						  ",\"execType\":"+ execType +
						  ",\"weekDay\":" + "[" + weekDay[0] +","+ weekDay[1] + "]" +
						  ",\"monthDate\":" + "[]" +
						  ",\"execTimeHour\":\"" + execTimeHour + "\"" +
						  ",\"execTimeMinute\":\"" + execTimeMinute + "\"" +
						  ",\"timeZoneId\":" + timeZoneId +
						  ",\"dateTimeFormat\":" + dateTimeFormat +
						  ",\"execDateTimestamp\":" + execDateTimestamp +
						  ",\"language\":" + language +
						  ",\"toAddress\":" + toAddress +
						  ",\"ccAddress\":" + ccAddress +
						  ",\"bccAddress\":" + bccAddress +
						 "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0011"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("execFlag"));


		/* toAddressの "@"を除く。E0014(必須パラメータチェックエラー)が返ってくる */
		System.out.println("* * * * setMaintenanceInfoTest: E0014 Case * * * *");
		mockMvc.perform(post("/maintenance/setScheduledMaintenanceSettings/")
				.cookie(cookie)
				.content("{ \"execFlag\":\"" + execFlag + "\"" +
						  ",\"sendFlag\":\""+ sendFlag + "\"" +
						  ",\"execType\":"+ execType +
						  ",\"weekDay\":" + "[" + weekDay[0] +","+ weekDay[1] + "]" +
						  ",\"monthDate\":" + "[]" +
						  ",\"execTimeHour\":\"" + execTimeHour + "\"" +
						  ",\"execTimeMinute\":\"" + execTimeMinute + "\"" +
						  ",\"timeZoneId\":" + timeZoneId +
						  ",\"dateTimeFormat\":" + dateTimeFormat +
						  ",\"execDateTimestamp\":" + execDateTimestamp +
						  ",\"language\":" + language +
						  ",\"toAddress\":" + toAddress.replaceAll("@", "") +
						  ",\"ccAddress\":" + ccAddress +
						  ",\"bccAddress\":" + bccAddress +
						 "}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList[0].errorCode").value("E0014"))
				.andExpect(jsonPath("$.common.errorList[0].errorField").value("toAddress"));



		/* sessionIdに"e25f5c5363914d87ab5c197222c05c79"を設定する。E0021(指定されたセッションIdが存在しない) が返ってくる */
		System.out.println("* * * * setScheduledMaintenanceSettingsTest: E0021 Case * * * *");
		Cookie errorCookie = new Cookie("dsessionId_8085","e25f5c5363914d87ab5c197222c05c79");	//エラー
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		mockMvc.perform(post("/maintenance/setScheduledMaintenanceSettings/")
				.cookie(errorCookie)
				.content("{ \"execFlag\":\"" + execFlag + "\"" +
						  ",\"sendFlag\":\""+ sendFlag + "\"" +
						  ",\"execType\":"+ execType +
						  ",\"weekDay\":" + "[" + weekDay[0] +","+ weekDay[1] + "]" +
						  ",\"monthDate\":" + "[]" +
						  ",\"execTimeHour\":\"" + execTimeHour + "\"" +
						  ",\"execTimeMinute\":\"" + execTimeMinute + "\"" +
						  ",\"timeZoneId\":" + timeZoneId +
						  ",\"dateTimeFormat\":" + dateTimeFormat +
						  ",\"execDateTimestamp\":" + execDateTimestamp +
						  ",\"language\":" + language +
						  ",\"toAddress\":" + toAddress +
						  ",\"ccAddress\":" + ccAddress +
						  ",\"bccAddress\":" + bccAddress +
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
	 * SRDM Common Service Communication API Specification 3.4.5
	 * メンテナンス状態取得テスト getMaintenanceStatusTest
	 * @throws Exception
	 */
	//@Test		//エラーコードなし
	public void getMaintenanceStatustTest() throws Exception{

		AccountManagerForTest accountManager = new AccountManagerForTest();
		cookie = accountManager.login(loginDomainId, accountId, permissionNameList, targetGroupId);

		mockMvc.perform(post("/maintenance/getMaintenanceStatus/")
				.cookie(cookie)
				.content("{}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andDo(print())
				.andExpect(jsonPath("$.common.errorList").isEmpty())
				.andExpect(jsonPath("$.status").isNotEmpty())
				.andExpect(jsonPath("$.maintenanceCode").isNotEmpty());

		accountManager.logout();
	}

}
