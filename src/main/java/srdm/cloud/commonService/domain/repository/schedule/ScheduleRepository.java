package srdm.cloud.commonService.domain.repository.schedule;

import srdm.cloud.commonService.domain.model.DeleteLogSetting;
import srdm.common.constant.SrdmLogConstants.LogType;
import srdm.common.exception.SrdmDataAccessException;

public interface ScheduleRepository {

	boolean isExistMibSchedule(String groupId) throws SrdmDataAccessException;
	void insertMibSchedule(String groupId, String domainId, String accountId) throws SrdmDataAccessException;
	DeleteLogSetting findDeleteLogSchedule(LogType logType) throws SrdmDataAccessException;
	void updateDeleteLogSchedule(LogType logType, DeleteLogSetting deleteLogSetting) throws SrdmDataAccessException;

	void shutdownScheduleService() throws SrdmDataAccessException;
}
