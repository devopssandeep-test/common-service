package srdm.cloud.commonService.app.api.setting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import srdm.cloud.commonService.app.bean.CommonRequestData;
import srdm.cloud.commonService.app.bean.setting.GetEnableRspResBean;
import srdm.cloud.commonService.app.bean.setting.GetRspStatusResBean;
import srdm.cloud.commonService.app.bean.setting.SetEnableRspReqBean;
import srdm.cloud.commonService.app.bean.setting.SetEnableRspResBean;
import srdm.cloud.commonService.domain.service.setting.RspjService;
import srdm.common.exception.SrdmBaseException;

@RestController
@RequestMapping(value="/setting/")
public class RspjSettingController {

	@Autowired
	RspjService rspjService;

	@RequestMapping(value="/getRspStatus/", method=RequestMethod.POST)
	public GetRspStatusResBean getRspStatus(
			CommonRequestData commonRequestData) {

		GetRspStatusResBean resBean = new GetRspStatusResBean();

		String status = rspjService.getRspStatus(commonRequestData.getSessionId());
		resBean.setStatus(status);
		return resBean;
	}

	@RequestMapping(value="/getEnableRsp/", method=RequestMethod.POST)
	public GetEnableRspResBean getEnableRsp(
			CommonRequestData commonRequestData) throws SrdmBaseException {

		GetEnableRspResBean resBean = new GetEnableRspResBean();

		String status = rspjService.getEnableRsp(commonRequestData.getSessionId());
		resBean.setRspEnableStatus(status);
		return resBean;
	}

	@RequestMapping(value="/setEnableRsp/", method=RequestMethod.POST)
	public SetEnableRspResBean setEnableRsp(
			@Validated @RequestBody SetEnableRspReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		rspjService.setEnableRsp(commonRequestData.getSessionId(), reqBean.getRspEnableStatus());
		SetEnableRspResBean resBean = new SetEnableRspResBean();
		return resBean;
	}
}
