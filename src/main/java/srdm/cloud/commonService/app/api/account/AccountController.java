package srdm.cloud.commonService.app.api.account;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import srdm.cloud.commonService.app.bean.CommonRequestData;
import srdm.cloud.commonService.app.bean.account.AccountLockReqBean;
import srdm.cloud.commonService.app.bean.account.AccountLockResBean;
import srdm.cloud.commonService.app.bean.account.AccountUnlockReqBean;
import srdm.cloud.commonService.app.bean.account.AccountUnlockResBean;
import srdm.cloud.commonService.app.bean.account.CreateAccountReqBean;
import srdm.cloud.commonService.app.bean.account.CreateAccountResBean;
import srdm.cloud.commonService.app.bean.account.DeleteAccountReqBean;
import srdm.cloud.commonService.app.bean.account.DeleteAccountResBean;
import srdm.cloud.commonService.app.bean.account.EditAccountReqBean;
import srdm.cloud.commonService.app.bean.account.EditAccountResBean;
import srdm.cloud.commonService.app.bean.account.GetAccountListReqBean;
import srdm.cloud.commonService.app.bean.account.GetAccountListReqBean.GetAccountList;
import srdm.cloud.commonService.app.bean.account.GetAccountListResBean;
import srdm.cloud.commonService.app.bean.account.GetAccountPermissionResBean;
import srdm.cloud.commonService.app.bean.account.GetAccountReqBean;
import srdm.cloud.commonService.app.bean.account.GetAccountResBean;
import srdm.cloud.commonService.app.bean.account.GetLoginAccountInfoResBean;
import srdm.cloud.commonService.app.bean.account.SetEmailNortifyReqBean;
import srdm.cloud.commonService.app.bean.account.SetEmailNortifyResBean;
import srdm.cloud.commonService.domain.model.AccountInfo;
import srdm.cloud.commonService.domain.model.AccountNortificationInfo;
import srdm.cloud.commonService.domain.model.EditAccount;
import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.GetListRes;
import srdm.cloud.commonService.domain.model.LoginAccountInfo;
import srdm.cloud.commonService.domain.model.SimpleAccount;
import srdm.cloud.commonService.domain.service.account.AccountService;
import srdm.cloud.commonService.domain.service.mail.MailSendService;
import srdm.common.exception.SrdmBaseException;

@RestController
@RequestMapping("/account")
public class AccountController {

	// private static final Logger logger =
	// LoggerFactory.getLogger(AccountController.class);

	@Autowired
	AccountService accountService;

	@Autowired
	MailSendService mailSendService;

	/**
	 * アカウント一覧の取得
	 *
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value = "/getAccountList/", method = RequestMethod.POST)
	public GetAccountListResBean getAccountList(
			@Validated(GetAccountList.class) @RequestBody GetAccountListReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		GetAccountListResBean resBean = new GetAccountListResBean();

		GetListReq getListReq = new GetListReq();
		if (reqBean.getStartIndex() != 0) {
			getListReq.setStartIndex(reqBean.getStartIndex());
		}
		if (reqBean.getCount() != 0) {
			getListReq.setCount(reqBean.getCount());
		}
		if (reqBean.getSimpleFilter() != null && reqBean.getSimpleFilter().isEmpty() == false) {
			getListReq.setSimpleFilter(reqBean.getSimpleFilter());
		}
		if (reqBean.getOrderBy() != null && reqBean.getOrderBy().isEmpty() == false) {
			getListReq.setOrderBy(reqBean.getOrderBy());
		}
		getListReq.getKeyMap().put("domainId", reqBean.getDomainId());

		GetListRes getListRes = accountService.getAccounts(commonRequestData.getSessionId(), getListReq);
		resBean.setStartIndex(getListReq.getStartIndex());
		resBean.setCount(getListReq.getCount());
		resBean.setDomainId(reqBean.getDomainId());
		resBean.setTotalCount(getListRes.getTotalCount());
		resBean.setResultCount(getListRes.getList().size());
		getListRes.getList().stream()
				.forEach(account -> resBean.addAccount(((SimpleAccount) account).getAccountId(),
						((SimpleAccount) account).getAccountName(), ((SimpleAccount) account).getRoleName(),
						((SimpleAccount) account).getAccountStatus(), ((SimpleAccount) account).isCanDelete()));
		return resBean;
	}

	/**
	 * ログインアカウントの権限一覧取得
	 *
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value = "/getAccountPermission/", method = RequestMethod.POST)
	public GetAccountPermissionResBean getAccountPermission(CommonRequestData commonRequestData)
			throws SrdmBaseException {

		GetAccountPermissionResBean resBean = new GetAccountPermissionResBean();

		LoginAccountInfo loginAccountInfo = accountService.getLoginAccountInfo(commonRequestData.getSessionId());
		/**
		 * permission listは、クラス名が同じだが、resBeanのpermissionListは、内部クラスで定義している為、
		 * 単純にSetterによる設定は、行えない。そのため、ひとつひとつ追加する。
		 * ※Lamda式での記述。（PermissionListの要素をひとつずつ取得し、permListに追加する。）
		 */
		loginAccountInfo.getPermissionList().stream().forEach(
				permission -> resBean.addPermission(permission.getPermissionName(), permission.getAttribute()));

		return resBean;
	}

	/**
	 * ログインアカウント情報取得
	 *
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value = "/getLoginAccountInfo/", method = RequestMethod.POST)
	public GetLoginAccountInfoResBean getLoginAccountInfo(CommonRequestData commonRequestData)
			throws SrdmBaseException {

		GetLoginAccountInfoResBean resBean = new GetLoginAccountInfoResBean();
		LoginAccountInfo loginAccountInfo = accountService.getLoginAccountInfo(commonRequestData.getSessionId());
		BeanUtils.copyProperties(loginAccountInfo, resBean);

		return resBean;
	}

	/**
	 * アカウント情報取得（指定アカウント）
	 *
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value = "/getAccount/", method = RequestMethod.POST)
	public GetAccountResBean getAccount(@Validated @RequestBody GetAccountReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		GetAccountResBean resBean = new GetAccountResBean();

		AccountInfo accountInfo = accountService.getAccount(commonRequestData.getSessionId(), reqBean.getAccountId());

		// TODO:Bean間のマッピングをどうするか？
		BeanUtils.copyProperties(accountInfo, resBean);
		resBean.setTimeZoneId(accountInfo.getTimeZone());

		return resBean;
	}

	/**
	 * アカウント作成
	 *
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value = "/createAccount/", method = RequestMethod.POST)
	public CreateAccountResBean createAccount(@Validated @RequestBody CreateAccountReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		CreateAccountResBean resBean = new CreateAccountResBean();

		// TODO:Domain Objectをnewするというのが、本来の実装か？また、Bean間のマッピングをどうするか？
		AccountInfo accountInfo = new AccountInfo();
		BeanUtils.copyProperties(reqBean, accountInfo);
		accountInfo.setTimeZone(reqBean.getTimeZoneId());
		accountInfo.setPermanentAccount(reqBean.getIsPermanentAccount());
		accountInfo.setPrivateRole(reqBean.getIsPrivateRole());
		
		String accountId = accountService.create(commonRequestData.getSessionId(), accountInfo);
		resBean.setAccountId(accountId);

		return resBean;
	}

	/**
	 * アカウント削除
	 *
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value = "/deleteAccount/", method = RequestMethod.POST)
	public DeleteAccountResBean deleteAccount(@Validated @RequestBody DeleteAccountReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		accountService.delete(commonRequestData.getSessionId(), reqBean.getAccountIdList());

		DeleteAccountResBean resBean = new DeleteAccountResBean();

		return resBean;
	}

	/**
	 * アカウント編集
	 *
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value = "/editAccount/", method = RequestMethod.POST)
	public EditAccountResBean editAccount(@Validated @RequestBody EditAccountReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		EditAccountResBean resBean = new EditAccountResBean();

		/**
		 * アカウント情報編集は、reqBeanの内容をそのままAccountのオブジェクトにコピーするのではなく、
		 * 編集専用のオブジェクトにコピーしないといけない。（パスワードの更新とログインパスワードのチェックがあるので）
		 */
		// TODO:Domain Objectをnewするというのが、本来の実装か？また、Bean間のマッピングをどうするか？
		EditAccount editAccount = new EditAccount();
		BeanUtils.copyProperties(reqBean, editAccount);
		editAccount.setChangePasswordFlag(Boolean.parseBoolean(reqBean.getChangePasswordFlag()));
		editAccount.setPermanentAccount(reqBean.isPermanentAccount());

		accountService.update(commonRequestData.getSessionId(), editAccount, reqBean.isPrivateRole());
		return resBean;
	}

	/**
	 * アカウントロック解除
	 * 
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value = "/accountUnlock/", method = RequestMethod.POST)
	public AccountUnlockResBean accountUnlock(@Validated @RequestBody AccountUnlockReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		accountService.accountUnlock(commonRequestData.getSessionId(), reqBean.getAccountIdList());
		AccountUnlockResBean resBean = new AccountUnlockResBean();

		return resBean;
	}

	/**
	 * アカウント情報のメール通知
	 * 
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value = "/setEmailNortify/", method = RequestMethod.POST)
	public SetEmailNortifyResBean setEmailNortify(@Validated @RequestBody SetEmailNortifyReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		AccountNortificationInfo accountNortificationInfo = new AccountNortificationInfo();
		BeanUtils.copyProperties(reqBean, accountNortificationInfo);

		mailSendService.sendEmailNortify(commonRequestData.getSessionId(), accountNortificationInfo);

		SetEmailNortifyResBean resBean = new SetEmailNortifyResBean();
		return resBean;
	}

	/**
	 * アカウントロック
	 * 
	 * @param reqBean
	 * @param commonRequestData
	 * @return
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value = "/accountLock/", method = RequestMethod.POST)
	public AccountLockResBean accountLock(@Validated @RequestBody AccountLockReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		accountService.accountLock(commonRequestData.getSessionId(), reqBean.getAccountIdList());
		AccountLockResBean resBean = new AccountLockResBean();

		return resBean;
	}

}
