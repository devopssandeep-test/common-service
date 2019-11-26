package srdm.cloud.commonService.domain.repository.setting;

import srdm.cloud.commonService.domain.model.EditNetworkSetting;
import srdm.cloud.commonService.domain.model.EditSmtpSetting;
import srdm.cloud.commonService.domain.model.NetworkSetting;
import srdm.cloud.commonService.domain.model.SmtpSetting;
import srdm.cloud.commonService.domain.model.SystemSettingNetwork;
import srdm.common.exception.SrdmDataAccessException;

public interface SystemSettingRepository {

	SmtpSetting getSmtpSetting() throws SrdmDataAccessException;
	void updateSmtpSetting(EditSmtpSetting smtpSetting) throws SrdmDataAccessException;

	SystemSettingNetwork getSystemSettingNetwork() throws SrdmDataAccessException;

	NetworkSetting getNetworkSetting() throws SrdmDataAccessException;
	void updateNetworkSetting(EditNetworkSetting networkSetting) throws SrdmDataAccessException;
}
