package srdm.cloud.commonService.domain.repository.session;

import srdm.cloud.commonService.domain.model.UserDetail;

public interface SrdmSessionRepository {

	String addSession(UserDetail userDetail);
	boolean invalidateSession(String sessionId);

	boolean checkSession(String sessionId);
	String checkSessionResultAndReson(String sessionId);
	boolean hasPermission(String sessionId, String permission);

	String getAccountId(String sessionId);
	String getGroupId(String sessionId);
	String getDomainId(String sessionId);
}
