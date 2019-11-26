package srdm.cloud.commonService.app.api.domain;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import srdm.cloud.commonService.app.bean.CommonRequestData;
import srdm.cloud.commonService.app.bean.domain.CreateDomainReqBean;
import srdm.cloud.commonService.app.bean.domain.CreateDomainResBean;
import srdm.cloud.commonService.app.bean.domain.DeleteDomainReqBean;
import srdm.cloud.commonService.app.bean.domain.DeleteDomainResBean;
import srdm.cloud.commonService.app.bean.domain.EditDomainReqBean;
import srdm.cloud.commonService.app.bean.domain.EditDomainResBean;
import srdm.cloud.commonService.app.bean.domain.GetDomainListReqBean;
import srdm.cloud.commonService.app.bean.domain.GetDomainListResBean;
import srdm.cloud.commonService.app.bean.domain.GetDomainReqBean;
import srdm.cloud.commonService.app.bean.domain.GetDomainResBean;
import srdm.cloud.commonService.app.bean.domain.GetThemeSettingResBean;
import srdm.cloud.commonService.app.bean.domain.SetThemeSettingReqBean;
import srdm.cloud.commonService.app.bean.domain.SetThemeSettingResBean;
import srdm.cloud.commonService.domain.model.CreateDomain;
import srdm.cloud.commonService.domain.model.DomainDetail;
import srdm.cloud.commonService.domain.model.EditDomain;
import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.GetListRes;
import srdm.cloud.commonService.domain.model.SimpleDomain;
import srdm.cloud.commonService.domain.service.domain.DomainService;
import srdm.common.exception.SrdmBaseException;

@RestController
@RequestMapping(value = "/domain")
public class DomainController {

	// private static final Logger logger =
	// LoggerFactory.getLogger(DomainController.class);

	@Autowired
	DomainService domainService;

	/**
	 * ドメイン一覧の取得（parentDomainIdをキーに取得）
	 */
	@RequestMapping(value = "/getDomainList/", method = RequestMethod.POST)
	public GetDomainListResBean getDomainList(@Validated @RequestBody GetDomainListReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		GetDomainListResBean resBean = new GetDomainListResBean();

		GetListReq getListReq = new GetListReq();
		if (reqBean.getStartIndex() != 0) {
			getListReq.setStartIndex(reqBean.getStartIndex());
		}
		if (reqBean.getCount() != 0) {
			getListReq.setCount(reqBean.getCount());
		}
		getListReq.getKeyMap().put("domainId", reqBean.getDomainId());
		if (reqBean.getExpand() != null && reqBean.getExpand().isEmpty() == false) {
			getListReq.getKeyMap().put("expand", reqBean.getExpand());
		} else {
			// expandの指定が無い場合は、"no"を設定
			getListReq.getKeyMap().put("expand", "no");
		}

		GetListRes getListRes = domainService.getDomainsByParentDomainId(commonRequestData.getSessionId(), getListReq);
		resBean.setStartIndex(getListReq.getStartIndex());
		resBean.setCount(getListReq.getCount());
		resBean.setDomainId(reqBean.getDomainId());
		resBean.setTotalCount(getListRes.getTotalCount());
		resBean.setResultCount(getListRes.getList().size());
		getListRes.getList().stream().forEach(domain -> resBean.addDomain(((SimpleDomain) domain).getDomainId(),
				((SimpleDomain) domain).getDomainName(), ((SimpleDomain) domain).getParentDomainId()));

		return resBean;
	}

	@RequestMapping(value = "/createDomain/", method = RequestMethod.POST)
	public CreateDomainResBean createDomain(@Validated @RequestBody CreateDomainReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		CreateDomainResBean resBean = new CreateDomainResBean();

		CreateDomain createDomain = new CreateDomain();
		BeanUtils.copyProperties(reqBean, createDomain);

		String domainId = domainService.create(commonRequestData.getSessionId(), createDomain);
		resBean.setDomainId(domainId);
		return resBean;
	}

	/**
	 * ドメイン情報取得
	 */
	@RequestMapping(value = "/getDomain/", method = RequestMethod.POST)
	public GetDomainResBean getDomain(@Validated @RequestBody GetDomainReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		GetDomainResBean resBean = new GetDomainResBean();
		DomainDetail domain = domainService.getDomainDetail(commonRequestData.getSessionId(), reqBean.getDomainId());
		BeanUtils.copyProperties(domain, resBean);

		return resBean;
	}

	@RequestMapping(value = "/editDomain/", method = RequestMethod.POST)
	public EditDomainResBean editDomain(@Validated @RequestBody EditDomainReqBean reqBeaan,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		EditDomainResBean resBean = new EditDomainResBean();

		/**
		 * ドメイン情報編集は、reqBeanの内容をそのままDomainのオブジェクトにコピーするのではなく、
		 * 編集専用のオブジェクトにコピーしないといけない。（追加する権限／削除する権限とかがあるので。）
		 * 内容的には、reqBeanそのままだがレイヤーを考えると別オブジェクトへコピーすべき。
		 */
		// TODO:Domain Objectをnewするというのが、本来の実装か？また、Bean間のマッピングをどうするか？
		EditDomain editDomain = new EditDomain();
		BeanUtils.copyProperties(reqBeaan, editDomain);

		domainService.update(commonRequestData.getSessionId(), editDomain);
		return resBean;
	}

	@RequestMapping(value = "/deleteDomain/", method = RequestMethod.POST)
	public DeleteDomainResBean deleteDomain(@Validated @RequestBody DeleteDomainReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		domainService.delete(commonRequestData.getSessionId(), reqBean.getDomainIdList());
		DeleteDomainResBean resBean = new DeleteDomainResBean();
		return resBean;
	}

	/**
	 * Theme設定取得
	 */
	@RequestMapping(value = "/getThemeSetting/", method = RequestMethod.POST)
	public GetThemeSettingResBean getThemeSetting(CommonRequestData commonRequestData) throws SrdmBaseException {

		String theme = domainService.getThemeSetting(commonRequestData.getSessionId());

		GetThemeSettingResBean resBean = new GetThemeSettingResBean();
		resBean.setTheme(theme);

		return resBean;
	}

	/**
	 * Theme設定更新
	 */
	@RequestMapping(value = "/setThemeSetting/", method = RequestMethod.POST)
	public SetThemeSettingResBean setThemeSetting(@Validated @RequestBody SetThemeSettingReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		domainService.setThemeSetting(commonRequestData.getSessionId(), reqBean.getTheme());
		SetThemeSettingResBean resBean = new SetThemeSettingResBean();
		return resBean;
	}
}
