package srdm.cloud.commonService.repositoryNoDB;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import srdm.cloud.commonService.domain.model.UserDetail;
import srdm.cloud.commonService.domain.repository.session.SrdmSessionRepository;
import srdm.cloud.shared.session.SessionDto;
import srdm.cloud.shared.session.SessionMap;
import srdm.cloud.shared.session.SessionService;

@Repository
public class TestSrdmSessionRepositoryImpl implements InitializingBean, SrdmSessionRepository {

	private static final Logger logger = LoggerFactory.getLogger(TestSrdmSessionRepositoryImpl.class);

	@Value("${srdm.login.max}")
	private int max;

	/**
	 * Bean登録完了時、SessionMapに対してセッションタイムアウトと最大ログイン数の値を設定
	 */
	@Override
	public void afterPropertiesSet() throws Exception {

		// 最大ログイン数を設定
		SessionMap.setMaxMapSize(max);

		logger.info("[SessionMap setting] session max login num[{}]", max);
	}

	/**
	 * セッション登録
	 */
	@Override
	public String addSession(UserDetail userDetail) {

		String sessionId;
		SessionDto sessionDto = new SessionDto();
		if(StringUtils.isEmpty(userDetail.getSessionId())) {
			sessionId = UUID.randomUUID().toString();
			sessionId = sessionId.replaceAll("-", "");
		} else {
			sessionId = userDetail.getSessionId();
		}
		sessionDto.setAccountId(userDetail.getAccountId());
		sessionDto.setDomainId(userDetail.getDomainId());
		try {
			sessionDto.setGroupId(Long.parseLong(userDetail.getGroupId()));
		} catch (NumberFormatException e) {
			logger.error("[addSession] groupId is not number.", e);
			return null;
		}
		sessionDto.setPermissionList(userDetail.getPermissionList());

		SessionMap sessionMap = SessionMap.getInstance();
		if(sessionMap.add(sessionId, sessionDto) == false) {
			logger.error("[addSession] [createSession]:Exceed maximum number of login.");
			return null;
		}
		return sessionId;
	}

	/**
	 * セッションを破棄
	 */
	@Override
	public boolean invalidateSession(String sessionId) {

		SessionMap sessionMap = SessionMap.getInstance();
		return sessionMap.invalidate(sessionId);
	}

	/**
	 * セッションチェック（チェックのみ）
	 */
	@Override
	public boolean checkSession(String sessionId) {

		SessionService sessionService = new SessionService();
		return sessionService.checkSession(sessionId);
	}

	/**
	 * セッションチェック（チェックと結果詳細を返す）
	 */
	@Override
	public String checkSessionResultAndReson(String sessionId) {

		String result = "";
		SessionService sessionService = new SessionService();
		boolean isValid = sessionService.checkSession(sessionId);
		if(isValid == false) {
			result = sessionService.getReasonForCheckError();
		}
		return result;
	}

	/**
	 * 権限有無チェック
	 */
	@Override
	public boolean hasPermission(String sessionId, String permission) {

		SessionService sessionService = new SessionService();
		return sessionService.hasPermission(sessionId, permission);
	}

	/*
	 * ログインアカウントID取得
	 * 存在しない場合は、空文字（""）を返す。
	 * (非 Javadoc)
	 * @see srdm.cloud.commonService.domain.repository.session.SrdmSessionRepository#getAccountId(java.lang.String)
	 */
	@Override
	public String getAccountId(String sessionId) {
		SessionService sessionService = new SessionService();
		return sessionService.getLoginAccountId(sessionId);
	}

	/*
	 * ログインアカウントのホームグループID取得
	 * 存在しない場合は、空文字（""）を返す。
	 * (非 Javadoc)
	 * @see srdm.cloud.commonService.domain.repository.session.SrdmSessionRepository#getGroupId(java.lang.String)
	 */
	@Override
	public String getGroupId(String sessionId) {
		SessionService sessionService = new SessionService();
		long groupId = sessionService.getLoginGroupId(sessionId);
		String strGroupId = "";
		if(groupId != -1) {
			strGroupId = String.valueOf(groupId);
		}
		return strGroupId;
	}

	/*
	 * ログインアカウントが所属するドメインIDを返す。
	 * 存在しない場合は、空文字（""）を返す。
	 * (非 Javadoc)
	 * @see srdm.cloud.commonService.domain.repository.session.SrdmSessionRepository#getDomainId(java.lang.String)
	 */
	@Override
	public String getDomainId(String sessionId) {
		SessionService sessionService = new SessionService();
		return sessionService.getLoginDomainId(sessionId);
	}

}
