package srdm.cloud.commonService.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.Cookie;

import srdm.cloud.shared.session.SessionDto;
import srdm.cloud.shared.session.SessionMap;

/*
 *
 * テスト時のログイン管理
 */
public class AccountManagerForTest {

	private String sessionId = "";

	public AccountManagerForTest() {
		super();
	}

	/*
	 * テストAPI呼出し時にセットするCookieの生成および、セッション情報の生成
	 */
	public Cookie login(String loginDomainId, String accountId, String[] permissionNameList, String targetGroupId) {

		sessionId = UUID.randomUUID().toString();
		sessionId = sessionId.replaceAll("-", "");

		SessionDto sessionDto = new SessionDto();
		sessionDto.setAccountId(accountId);
		sessionDto.setDomainId(loginDomainId);
		try {
			sessionDto.setGroupId(Long.parseLong(targetGroupId));
		} catch (NumberFormatException e) {
			System.out.println("groupId is not number.");
		}
		List<String> permissionList = new ArrayList<String>();
		for(int i=0; i<permissionNameList.length; i++){
			permissionList.add(permissionNameList[i]);
		}
		sessionDto.setPermissionList(permissionList);
		SessionMap sessionMap = SessionMap.getInstance();
		if(sessionMap.add(sessionId, sessionDto) == false) {
			System.out.println("session add error.");
		}

		Cookie cookie = new Cookie("dsessionId_8085",sessionId);
		cookie.setMaxAge(60 * 30);
		cookie.setPath("/");
		cookie.setSecure(false);

		return cookie;
	}

	public void logout() {
		SessionMap sessionMap = SessionMap.getInstance();
		sessionMap.invalidate(sessionId);
		sessionId = "";
	}
}
