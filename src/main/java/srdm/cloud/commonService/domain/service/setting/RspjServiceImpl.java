package srdm.cloud.commonService.domain.service.setting;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import srdm.cloud.commonService.domain.model.LogItem;
import srdm.cloud.commonService.domain.repository.rspj.RspjRepository;
import srdm.cloud.commonService.domain.service.log.SysMgtLogWriteService;
import srdm.common.constant.SrdmConstants.RspjStatus;
import srdm.common.constant.SrdmLogConstants;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmGeneralException;

@Service
public class RspjServiceImpl implements RspjService {

	private static final Logger logger = LoggerFactory.getLogger(RspjServiceImpl.class);

	@Autowired
	RspjRepository rspjRepository;

	@Autowired
	SysMgtLogWriteService sysMgtLogWriteService;

	@Override
	public String getRspStatus(String sessionId) {

		String rspjStatus = rspjRepository.getRspStatus();

		logger.info("RSPJ SystemManager Status code:[{}]", rspjStatus);

		String status;
		if(RspjStatus.CONVERT_STATUS.get(rspjStatus) != null) {
			status = RspjStatus.CONVERT_STATUS.get(rspjStatus);
		} else {
			status = RspjStatus.CONVERT_STATUS.get(RspjStatus.RSPJ_STATUS_CODE_S100);
		}

		return status;
	}

	@Override
	public String getEnableRsp(String sessionId) throws SrdmDataAccessException {

		String enableStatus = rspjRepository.getEnableRsp();

		logger.info("RSPJ enable Status:[{}]", enableStatus);

		return enableStatus;
	}

	@Override
	public void setEnableRsp(String sessionId, String rspEnableStatus) throws SrdmDataAccessException, SrdmGeneralException {

		// システム管理ログ用情報
		List<LogItem> itemList = new ArrayList<LogItem>();
		itemList.add(new LogItem(SrdmLogConstants.SYSMGT_ITEM_NAME_RSPJ_STATE, rspEnableStatus));

		try {
			rspjRepository.setEnableRsp(rspEnableStatus);
			// システム管理ログ記録（正常）
			sysMgtLogWriteService.writeSystemManagementLog(
					sessionId,
					SrdmLogConstants.SYSMGT_OPERATION_RSPJ_SETTING,
					SrdmLogConstants.SYSMGT_CODE_NORMAL,
					itemList);
		} catch (SrdmDataAccessException | SrdmGeneralException e) {
			// システム管理ログ記録（失敗:アクセスエラー等）
			sysMgtLogWriteService.writeSystemManagementLog(
					sessionId,
					SrdmLogConstants.SYSMGT_OPERATION_RSPJ_SETTING,
					SrdmLogConstants.SYSMGT_CODE_RSPJ_SETTING_FAILD,
					itemList);
			throw e;
		}

	}

}
