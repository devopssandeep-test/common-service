package srdm.cloud.commonService.domain.model;

import lombok.Data;

@Data
public class UsernamePasswordAuthentication {

	private String loginMethod;				// UI／agentどちらのログインかを指定。
	private String accountId;				// loginForScheduleで使用
	private String accountName;				// loginで使用
	private String domainId;
	private String rawPassword;
	private String sessionId;
}
