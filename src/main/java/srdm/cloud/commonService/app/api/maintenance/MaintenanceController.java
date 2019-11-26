package srdm.cloud.commonService.app.api.maintenance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import srdm.cloud.commonService.app.bean.CommonRequestData;
import srdm.cloud.commonService.app.bean.maintenance.GetMaintenanceInfoResBean;
import srdm.cloud.commonService.app.bean.maintenance.GetMaintenanceStatusResBean;
import srdm.cloud.commonService.app.bean.maintenance.GetScheduledMaintenanceSettingsResBean;
import srdm.cloud.commonService.app.bean.maintenance.SetMaintenanceInfoReqBean;
import srdm.cloud.commonService.app.bean.maintenance.SetMaintenanceInfoResBean;
import srdm.cloud.commonService.app.bean.maintenance.SetScheduledMaintenanceSettingsReqBean;
import srdm.cloud.commonService.app.bean.maintenance.SetScheduledMaintenanceSettingsResBean;
import srdm.cloud.commonService.domain.service.maintenance.MaintenanceService;
import srdm.common.exception.SrdmBaseException;

@RestController
@RequestMapping("/maintenance")
public class MaintenanceController {

//	private static final Logger logger = LoggerFactory.getLogger(MaintenanceController.class);

	@Autowired
	SetScheduledMaintenanceSettingsValidator setScheduledMaintenanceSettingsValidator;

	@Autowired
	MaintenanceService maintenanceService;

	@InitBinder("setScheduledMaintenanceSettingsReqBean")
	public void initBinderForSetScheduledMaintenanceSettings(WebDataBinder binder) {
		binder.addValidators(setScheduledMaintenanceSettingsValidator);
	}

	@RequestMapping(value="/getMaintenanceInfo/", method=RequestMethod.POST)
	public GetMaintenanceInfoResBean getMaintenanceInfo(
			CommonRequestData commonRequestData) throws SrdmBaseException {

		GetMaintenanceInfoResBean resBean = maintenanceService.getMaintenanceInfo();

		return resBean;
	}

	@RequestMapping(value="/setMaintenanceInfo/", method=RequestMethod.POST)
	public SetMaintenanceInfoResBean setMaintenanceInfo(
			@Validated @RequestBody SetMaintenanceInfoReqBean reqBean) throws SrdmBaseException {

		SetMaintenanceInfoResBean resBean = maintenanceService.setMaintenanceInfo(reqBean);

		return resBean;
	}

	@RequestMapping(value="/getScheduledMaintenanceSettings/", method=RequestMethod.POST)
	public GetScheduledMaintenanceSettingsResBean getScheduledMaintenanceSettings(
			CommonRequestData commonRequestData) throws SrdmBaseException {

		GetScheduledMaintenanceSettingsResBean resBean = maintenanceService.getScheduledMaintenanceSettings();

		return resBean;
	}

	@RequestMapping(value="/setScheduledMaintenanceSettings/", method=RequestMethod.POST)
	public SetScheduledMaintenanceSettingsResBean setScheduledMaintenanceSettings(
			@Validated @RequestBody SetScheduledMaintenanceSettingsReqBean setScheduledMaintenanceSettingsReqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		SetScheduledMaintenanceSettingsResBean resBean =
				maintenanceService.setScheduledMaintenanceSettings(commonRequestData.getSessionId(), setScheduledMaintenanceSettingsReqBean);

		return resBean;
	}

	@RequestMapping(value="/getMaintenanceStatus/", method=RequestMethod.POST)
	public GetMaintenanceStatusResBean getMaintenanceStatus(
			CommonRequestData commonRequestData) {

		GetMaintenanceStatusResBean resBean = maintenanceService.getMaintenanceStatus();

		return resBean;
	}
}
