package srdm.cloud.commonService.app.api.setting;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import srdm.cloud.commonService.app.bean.CommonRequestData;
import srdm.cloud.commonService.app.bean.setting.GetNetworkSettingResBean;
import srdm.cloud.commonService.app.bean.setting.GetProtocolSettingResBean;
import srdm.cloud.commonService.app.bean.setting.GetSmtpSettingResBean;
import srdm.cloud.commonService.app.bean.setting.SetNetworkSettingReqBean;
import srdm.cloud.commonService.app.bean.setting.SetNetworkSettingResBean;
import srdm.cloud.commonService.app.bean.setting.SetSmtpSettingReqBean;
import srdm.cloud.commonService.app.bean.setting.SetSmtpSettingResBean;
import srdm.cloud.commonService.domain.model.EditNetworkSetting;
import srdm.cloud.commonService.domain.model.EditSmtpSetting;
import srdm.cloud.commonService.domain.model.NetworkSetting;
import srdm.cloud.commonService.domain.model.ProtocolSetting;
import srdm.cloud.commonService.domain.model.SmtpSetting;
import srdm.cloud.commonService.domain.service.setting.SystemSettingService;
import srdm.common.exception.SrdmBaseException;

@RestController
@RequestMapping(value="/setting")
public class SystemSettingController {

	@Autowired
	SetSMTPSettingValidator setSMTPSettingValidator;

	@Autowired
	SystemSettingService systemSettingService;

	@InitBinder("setSmtpSettingReqBean")
	public void initBinderForSetSMTPSettingValidator(WebDataBinder binder) {
		binder.addValidators(setSMTPSettingValidator);
	}

	/**
	 * ネットワーク設定取得
	 */
	@RequestMapping(value="/getNetworkSetting/", method=RequestMethod.POST)
	public GetNetworkSettingResBean getNetworkSetting(
			CommonRequestData commonRequestData) throws SrdmBaseException {

		NetworkSetting networkSetting = systemSettingService.getNetworkSetting(commonRequestData.getSessionId());
		GetNetworkSettingResBean resBean = new GetNetworkSettingResBean();
		BeanUtils.copyProperties(networkSetting, resBean);

		return resBean;
	}

	/**
	 * ネットワーク設定更新
	 */
	@RequestMapping(value="/setNetworkSetting/", method=RequestMethod.POST)
	public SetNetworkSettingResBean setNetworkSetting(
			@Validated @RequestBody SetNetworkSettingReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		EditNetworkSetting editNetworkSetting = new EditNetworkSetting();
		BeanUtils.copyProperties(reqBean, editNetworkSetting);

		systemSettingService.updateNetworkSetting(commonRequestData.getSessionId(), editNetworkSetting);

		SetNetworkSettingResBean resBean = new SetNetworkSettingResBean();
		return resBean;
	}

	/**
	 * SMTP設定取得
	 */
	@RequestMapping(value="/getSMTPSetting/", method=RequestMethod.POST)
	public GetSmtpSettingResBean getSmtpSetting(
			CommonRequestData commonRequestData) throws SrdmBaseException {

		SmtpSetting smptSetting = systemSettingService.getSmtpSetting();
		GetSmtpSettingResBean resBean = new GetSmtpSettingResBean();
		BeanUtils.copyProperties(smptSetting, resBean);
		resBean.setSmtpHost(smptSetting.getHost());
		resBean.setSmtpPort(smptSetting.getPort());
		resBean.setUseSSL(smptSetting.getUseSsl());

		return resBean;
	}

	/**
	 * SMTP設定更新
	 */
	@RequestMapping(value="/setSMTPSetting/", method=RequestMethod.POST)
	public SetSmtpSettingResBean setSmtpSetting(
			@Validated @RequestBody SetSmtpSettingReqBean setSmtpSettingReqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		EditSmtpSetting editSmtpSetting = new EditSmtpSetting();
		BeanUtils.copyProperties(setSmtpSettingReqBean, editSmtpSetting);
		editSmtpSetting.setUseAuth(Boolean.parseBoolean(setSmtpSettingReqBean.getUseAuth()));
		editSmtpSetting.setPwdChgFlag(Boolean.parseBoolean(setSmtpSettingReqBean.getPwdChgFlag()));
		editSmtpSetting.setUseSSL(Boolean.parseBoolean(setSmtpSettingReqBean.getUseSSL()));
		systemSettingService.updateSmtpSetting(commonRequestData.getSessionId(), editSmtpSetting);

		SetSmtpSettingResBean resBean = new SetSmtpSettingResBean();
		return resBean;
	}
	
	/**
	 * 
	 * @param commonRequestData
	 * @return
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value = "/getProtocolSetting/", method = RequestMethod.POST)
	public GetProtocolSettingResBean getProtocolSetting(CommonRequestData commonRequestData) throws SrdmBaseException {
		// Get protocol settings 
		ProtocolSetting protocolSetting = systemSettingService.getProtocolSetting(commonRequestData.getSessionId());
		GetProtocolSettingResBean resBean = new GetProtocolSettingResBean();
		BeanUtils.copyProperties(protocolSetting, resBean);

		return resBean;
	}
}
