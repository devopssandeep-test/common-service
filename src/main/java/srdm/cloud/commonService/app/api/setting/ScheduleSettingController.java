package srdm.cloud.commonService.app.api.setting;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import srdm.cloud.commonService.app.bean.CommonRequestData;
import srdm.cloud.commonService.app.bean.setting.GetDeleteLogScheduleResBean;
import srdm.cloud.commonService.app.bean.setting.GetDeleteSystemLogScheduleResBean;
import srdm.cloud.commonService.app.bean.setting.SetDeleteLogScheduleReqBean;
import srdm.cloud.commonService.app.bean.setting.SetDeleteLogScheduleResBean;
import srdm.cloud.commonService.app.bean.setting.SetDeleteSystemLogScheduleReqBean;
import srdm.cloud.commonService.app.bean.setting.SetDeleteSystemLogScheduleResBean;
import srdm.cloud.commonService.domain.model.DeleteLogSetting;
import srdm.cloud.commonService.domain.service.setting.ScheduleSettingService;
import srdm.common.constant.SrdmLogConstants;
import srdm.common.exception.SrdmBaseException;

@RestController
@RequestMapping(value="/setting")
public class ScheduleSettingController {

//	private static final Logger logger = LoggerFactory.getLogger(ScheduleSettingController.class);

	@Autowired
	ScheduleSettingService scheduleSettingService;

	/**
	 * 定期ログ削除設定取得
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value="/getDeleteLogSchedule/", method=RequestMethod.POST)
	public GetDeleteLogScheduleResBean getDeleteLogSchedule(
			CommonRequestData commonRequestData) throws SrdmBaseException {

		DeleteLogSetting deleteLogSetting = scheduleSettingService.getDeleteLogSetting(commonRequestData.getSessionId(), SrdmLogConstants.LogType.OPERATION);

		GetDeleteLogScheduleResBean resBean = new GetDeleteLogScheduleResBean();
		BeanUtils.copyProperties(deleteLogSetting, resBean);

		return resBean;
	}

	/**
	 * 定期ログ削除設定更新
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value="/setDeleteLogSchedule/", method=RequestMethod.POST)
	public SetDeleteLogScheduleResBean setDeleteLogSchedule(
			@Validated @RequestBody SetDeleteLogScheduleReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		DeleteLogSetting deleteLogSetting = new DeleteLogSetting();
		BeanUtils.copyProperties(reqBean, deleteLogSetting);
		deleteLogSetting.setExecFlag(Boolean.parseBoolean(reqBean.getExecFlag()));

		scheduleSettingService.setDeleteLogSetting(commonRequestData.getSessionId(), deleteLogSetting, SrdmLogConstants.LogType.OPERATION);

		SetDeleteLogScheduleResBean resBean = new SetDeleteLogScheduleResBean();
		return resBean;
	}

	/**
	 * 定期ログ削除設定（システム管理ログ）取得
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value="/getDeleteSystemLogSchedule/", method=RequestMethod.POST)
	public GetDeleteSystemLogScheduleResBean getDeleteSystemLogSchedule(
			CommonRequestData commonRequestData) throws SrdmBaseException {

		DeleteLogSetting deleteLogSetting = scheduleSettingService.getDeleteLogSetting(commonRequestData.getSessionId(), SrdmLogConstants.LogType.SYSTEM);

		GetDeleteSystemLogScheduleResBean resBean = new GetDeleteSystemLogScheduleResBean();
		BeanUtils.copyProperties(deleteLogSetting, resBean);

		return resBean;
	}

	/**
	 * 定期ログ削除設定（システム管理ログ）更新
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value="/setDeleteSystemLogSchedule/", method=RequestMethod.POST)
	public SetDeleteSystemLogScheduleResBean setDeleteSystemLogSchedule(
			@Validated @RequestBody SetDeleteSystemLogScheduleReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		DeleteLogSetting deleteLogSetting = new DeleteLogSetting();
		BeanUtils.copyProperties(reqBean, deleteLogSetting);
		deleteLogSetting.setExecFlag(Boolean.parseBoolean(reqBean.getExecFlag()));

		scheduleSettingService.setDeleteLogSetting(commonRequestData.getSessionId(), deleteLogSetting, SrdmLogConstants.LogType.SYSTEM);

		SetDeleteSystemLogScheduleResBean resBean = new SetDeleteSystemLogScheduleResBean();
		return resBean;
	}
}
