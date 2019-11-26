package srdm.cloud.commonService.app.api.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import srdm.cloud.commonService.app.bean.CommonRequestData;
import srdm.cloud.commonService.app.bean.log.StartDeleteLogReqBean;
import srdm.cloud.commonService.app.bean.log.StartDeleteLogResBean;
import srdm.cloud.commonService.app.bean.log.StartDeleteSysMgtLogReqBean;
import srdm.cloud.commonService.app.bean.log.StartDeleteSysMgtLogResBean;
import srdm.cloud.commonService.domain.service.log.LogService;

@RestController
@RequestMapping(value="/log")
public class LogScheduleController {

//	private static final Logger logger = LoggerFactory.getLogger(LogScheduleController.class);

	@Autowired
	LogService logService;

	/**
	 * 操作ログの定期削除
	 */
	@RequestMapping(value="/startDeleteLog/", method=RequestMethod.POST)
	public StartDeleteLogResBean startDeleteLog(
			@Validated @RequestBody StartDeleteLogReqBean reqBean,
			CommonRequestData commonRequestData) {

		logService.deleteOperationLogSchedule(commonRequestData.getSessionId(), reqBean.getPeriod());

		StartDeleteLogResBean resBean = new StartDeleteLogResBean();
		return resBean;
	}

	/**
	 * システム管理ログの定期削除
	 */
	@RequestMapping(value="/startDeleteSysMgtLog/", method=RequestMethod.POST)
	public StartDeleteSysMgtLogResBean startDeleteSysMgtLog(
			@Validated @RequestBody StartDeleteSysMgtLogReqBean reqBean,
			CommonRequestData commonRequestData) {

		logService.deleteSystemLogSchedule(commonRequestData.getSessionId(), reqBean.getPeriod());

		StartDeleteSysMgtLogResBean resBean = new StartDeleteSysMgtLogResBean();
		return resBean;
	}
}
