package srdm.cloud.commonService.app.api.maintenance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import srdm.cloud.commonService.app.bean.CommonRequestData;
import srdm.cloud.commonService.app.bean.maintenance.StartOptimizationResBean;
import srdm.cloud.commonService.asynchronous.DbOptimization;

@RestController
@RequestMapping("/maintenance")
public class MaintenanceScheduleController {

//	private static final Logger logger = LoggerFactory.getLogger(MaintenanceController.class);

	@Autowired
	DbOptimization optimizationService;

	@RequestMapping(value="/startOptimization/", method=RequestMethod.POST)
	public StartOptimizationResBean startOptimization(
			CommonRequestData commonRequestData) {

		// スレッドの管理をFrameworkに任せる
		optimizationService.dbOptimize();

		StartOptimizationResBean resBean = new StartOptimizationResBean();
		return resBean;
	}
}
