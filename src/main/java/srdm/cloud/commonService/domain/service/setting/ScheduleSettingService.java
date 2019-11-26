package srdm.cloud.commonService.domain.service.setting;

import srdm.cloud.commonService.domain.model.DeleteLogSetting;
import srdm.common.constant.SrdmLogConstants.LogType;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

public interface ScheduleSettingService {

	DeleteLogSetting getDeleteLogSetting(String sessionId, LogType logType) throws SrdmDataAccessException;
	void setDeleteLogSetting(String sessionId, DeleteLogSetting deleteLogSetting, LogType logType) throws SrdmDataAccessException, SrdmDataNotFoundException;
}
