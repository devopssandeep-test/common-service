package srdm.cloud.commonService.domain.service.setting;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import srdm.cloud.commonService.domain.model.DeleteLogSetting;
import srdm.cloud.commonService.domain.repository.schedule.ScheduleRepository;
import srdm.cloud.commonService.domain.repository.session.SrdmSessionRepository;
import srdm.common.constant.SrdmLogConstants;
import srdm.common.constant.SrdmLogConstants.LogType;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

@Service
public class ScheduleSettingServiceImpl implements ScheduleSettingService {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ScheduleSettingServiceImpl.class);

	@Autowired
	ScheduleRepository scheduleRepository;

	@Autowired
	SrdmSessionRepository srdmSessionRepository;

	@Override
	public DeleteLogSetting getDeleteLogSetting(String sessionId, LogType logType) throws SrdmDataAccessException {

		DeleteLogSetting deleteLogSetting;
		if(logType == SrdmLogConstants.LogType.SYSTEM) {
			logger.info("getDeleteSystemLogSetting.");
		} else {
			logger.info("getDeleteLogSetting.");
		}
		deleteLogSetting = scheduleRepository.findDeleteLogSchedule(logType);
		return deleteLogSetting;
	}

	@Override
	public void setDeleteLogSetting(String sessionId, DeleteLogSetting deleteLogSetting, LogType logType) throws SrdmDataAccessException, SrdmDataNotFoundException {

		String accountId = srdmSessionRepository.getAccountId(sessionId);
		String domainId = srdmSessionRepository.getDomainId(sessionId);
		if(accountId.isEmpty() || domainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("accountId or domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "Account info dose not exist.");
		}

		deleteLogSetting.setDomainId(domainId);
		deleteLogSetting.setAccountId(accountId);
		if(logType == SrdmLogConstants.LogType.SYSTEM) {
			logger.info("updateDeleteSystemLogSetting.");
		} else {
			logger.info("updateDeleteLogSetting.");
		}
		scheduleRepository.updateDeleteLogSchedule(logType, deleteLogSetting);
	}

}
