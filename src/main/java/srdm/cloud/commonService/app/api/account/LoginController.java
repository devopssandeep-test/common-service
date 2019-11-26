package srdm.cloud.commonService.app.api.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import srdm.cloud.commonService.app.bean.CommonRequestData;
import srdm.cloud.commonService.app.bean.account.GetLoginStatusResBean;
import srdm.cloud.commonService.app.bean.account.LoginForOnPremisesAgentReqBean;
import srdm.cloud.commonService.app.bean.account.LoginForScheduleReqBean;
import srdm.cloud.commonService.app.bean.account.LoginReqBean;
import srdm.cloud.commonService.app.bean.account.LoginResBean;
import srdm.cloud.commonService.app.bean.account.LogoutResBean;
import srdm.cloud.commonService.domain.model.UsernamePasswordAuthentication;
import srdm.cloud.commonService.domain.service.account.LoginService;
import srdm.common.constant.SrdmConstants;
import srdm.common.exception.SrdmBaseException;

@RestController
@RequestMapping(value="/account")
public class LoginController {

	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	LoginService loginService;

	/**
	 * UIからのログイン処理
	 */
	@RequestMapping(value="/{method:login|loginForAgent}/", method=RequestMethod.POST)
	public LoginResBean login(
			@PathVariable String method,
			@Validated @RequestBody LoginReqBean reqBean) throws SrdmBaseException {

		LoginResBean resBean = new LoginResBean();

		UsernamePasswordAuthentication usernamePasswordAuthentication = new UsernamePasswordAuthentication();
		// 認証情報をセット
		if(method.equals("loginForAgent") == true) {
			usernamePasswordAuthentication.setLoginMethod(SrdmConstants.LOGIN_FROM_AGENT);
		} else {
			usernamePasswordAuthentication.setLoginMethod(SrdmConstants.LOGIN_FROM_UI);
		}
		usernamePasswordAuthentication.setAccountName(reqBean.getAccountName());
		usernamePasswordAuthentication.setDomainId(reqBean.getDomainId());
		usernamePasswordAuthentication.setRawPassword(reqBean.getPassword());
		if(StringUtils.isEmpty(reqBean.getSessionId())) {
			usernamePasswordAuthentication.setSessionId("");
		} else {
			usernamePasswordAuthentication.setSessionId(reqBean.getSessionId());
		}

		// ログイン認証
		String sessionId = loginService.login(usernamePasswordAuthentication);
		if(StringUtils.isEmpty(sessionId)) {
			// ログイン失敗
			logger.warn("Login faild.");
			resBean.setValidateResultFlg(false);
		} else {
			// ログイン成功
			logger.info("Login success.");
			resBean.setSessionId(sessionId);
			resBean.setValidateResultFlg(true);
		}

		return resBean;
	}

	/**
	 * Schedule Serviceからのログイン処理
	 */
	@RequestMapping(value="/loginForSchedule/", method=RequestMethod.POST)
	public LoginResBean loginForSchedule(
			@Validated @RequestBody LoginForScheduleReqBean reqBean) throws SrdmBaseException {

		LoginResBean resBean = new LoginResBean();

		UsernamePasswordAuthentication usernamePasswordAuthentication = new UsernamePasswordAuthentication();
		// 認証情報をセット
		usernamePasswordAuthentication.setAccountId(reqBean.getAccountId());
		usernamePasswordAuthentication.setDomainId(reqBean.getDomainId());

		// ログイン認証
		String sessionId = loginService.loginForSchedule(usernamePasswordAuthentication);
		if(StringUtils.isEmpty(sessionId)) {
			// ログイン失敗
			logger.warn("Login faild.");
			resBean.setValidateResultFlg(false);
		} else {
			// ログイン成功
			logger.info("Login success.");
			resBean.setSessionId(sessionId);
			resBean.setValidateResultFlg(true);
		}

		return resBean;
	}

	/**
	 * ログイン状態チェック
	 * @param commonRequestData
	 * @return
	 */
	@RequestMapping(value="/getLoginStatus/", method=RequestMethod.POST)
	public GetLoginStatusResBean getLoginStatus(
			CommonRequestData commonRequestData) {

		GetLoginStatusResBean resBean = new GetLoginStatusResBean();
		boolean isEnableAccount = loginService.getLoginStatus(commonRequestData.getSessionId());
		logger.info("Account is {}.", (isEnableAccount ? "login" : "logout"));
		resBean.setLoginFlg(isEnableAccount);
		return resBean;
	}

	/**
	 * ログアウト処理
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value={"/{method:logout|logoutForAgent}/"}, method=RequestMethod.POST)
	public LogoutResBean logout(
			@PathVariable String method,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		String caller;
		if(method.equals("logoutForAgent") == true) {
			caller = SrdmConstants.LOGIN_FROM_AGENT;
		} else {
			caller = SrdmConstants.LOGIN_FROM_UI;
		}
		loginService.logout(commonRequestData.getSessionId(), caller);
		logger.info("Account is logout. sessionId[{}]", commonRequestData.getSessionId());
		LogoutResBean resBean = new LogoutResBean();
		return resBean;
	}

	/**
	 * ScheduleServiceからのログアウト処理
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value="/logoutForSchedule/", method=RequestMethod.POST)
	public LogoutResBean logoutForSchedule(
			CommonRequestData commonRequestData) throws SrdmBaseException {

		loginService.logoutForSchedule(commonRequestData.getSessionId());
		logger.info("Account is logout. sessionId[{}]", commonRequestData.getSessionId());
		LogoutResBean resBean = new LogoutResBean();
		return resBean;
	}

	/**
	 * オンプレミスエージェントからのログイン
	 *
	 * @param reqBean
	 * @return
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value="/loginForOnPremisesAgent/", method=RequestMethod.POST)
	public LoginResBean loginForOnPremisesAgent(
			@Validated @RequestBody LoginForOnPremisesAgentReqBean reqBean) throws SrdmBaseException {

		LoginResBean resBean = new LoginResBean();

		// ログイン認証
		String sessionId = loginService.loginForOnPremisesAgent(reqBean.getLoginId());
		if(StringUtils.isEmpty(sessionId)) {
			// ログイン失敗
			logger.warn("Login faild.");
			resBean.setValidateResultFlg(false);
		} else {
			// ログイン成功
			logger.info("Login success.");
			resBean.setSessionId(sessionId);
			resBean.setValidateResultFlg(true);
		}

		return resBean;


	}

}
