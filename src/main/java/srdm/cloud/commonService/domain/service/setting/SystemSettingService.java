package srdm.cloud.commonService.domain.service.setting;

import srdm.cloud.commonService.domain.model.EditNetworkSetting;
import srdm.cloud.commonService.domain.model.EditSmtpSetting;
import srdm.cloud.commonService.domain.model.NetworkSetting;
import srdm.cloud.commonService.domain.model.ProtocolSetting;
import srdm.cloud.commonService.domain.model.SmtpSetting;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;
import srdm.common.exception.SrdmGeneralException;

public interface SystemSettingService {

	SmtpSetting getSmtpSetting() throws SrdmDataAccessException;
	void updateSmtpSetting(String sessionId, EditSmtpSetting smtpSetting) throws SrdmDataAccessException;
	NetworkSetting getNetworkSetting(String sessionId) throws SrdmDataAccessException;
	void updateNetworkSetting(String sessionId, EditNetworkSetting networkSetting) throws SrdmDataAccessException, SrdmGeneralException;
	ProtocolSetting getProtocolSetting(String sessionId) throws SrdmDataAccessException;
}
