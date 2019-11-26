package srdm.cloud.commonService.app.api.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import srdm.cloud.commonService.app.bean.CommonRequestData;
import srdm.cloud.commonService.app.bean.system.GetStorageStatusResBean;
import srdm.cloud.commonService.domain.service.system.StorageStatusService;

@RestController
@RequestMapping("/system")
public class StorageController {

//	private static final Logger logger = LoggerFactory.getLogger(StorageController.class);

	@Autowired
	StorageStatusService strageStatusService;

	@RequestMapping(value="/getStorageStatus/", method=RequestMethod.POST)
	public GetStorageStatusResBean getStorageStatus(
			CommonRequestData commonRequestData) {

		// TODO:暫定実装。レスポンスクラスをServiceで生成するのを止める事。
		GetStorageStatusResBean resBean = strageStatusService.getStorageStatus();

		return resBean;
	}
}
