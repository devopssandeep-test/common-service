package srdm.cloud.commonService.domain.service.account;

import srdm.cloud.commonService.domain.model.UsernamePasswordAuthentication;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;
import srdm.common.exception.SrdmGeneralException;

public interface LoginService {

	String login(UsernamePasswordAuthentication authInfo) throws SrdmDataAccessException;
	String loginForSchedule(UsernamePasswordAuthentication authInfo) throws SrdmDataAccessException;
	boolean getLoginStatus(String sessionId);
	void logout(String sessionId, String caller) throws SrdmDataNotFoundException, SrdmDataAccessException;
	void logoutForSchedule(String sessionId) throws SrdmDataNotFoundException, SrdmDataAccessException;
	String loginForOnPremisesAgent(String loginId) throws SrdmGeneralException;
}
