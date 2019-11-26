package srdm.cloud.commonService.domain.service.system;

import org.springframework.stereotype.Service;

import srdm.cloud.commonService.app.bean.system.GetStorageStatusResBean;
import srdm.cloud.shared.system.ServerCheckService;
import srdm.common.constant.SrdmConstants;

@Service
public class StorageStatusService {
	public GetStorageStatusResBean getStorageStatus() {
		GetStorageStatusResBean resBean = new GetStorageStatusResBean();
		ServerCheckService scs = new ServerCheckService();

		int status = scs.checkDiskWarning();
		switch (status) {
		case 0: // OK
			resBean.setStatus("ok");
			break;
		case 3: // Disk Full Warning
			resBean.setStatus("warning");
			break;
		case 1:
		case 2:
			// 1,2はDiskFullError (LoginCheckInterceptorでチェックしない)
		default:
			resBean.setStatus("error");
			resBean.addError(SrdmConstants.ERROR_E0026, "", "", SrdmConstants.ERROR_MESSAGE_E0026_01);
			break;
		}

		return resBean;
	}
}
