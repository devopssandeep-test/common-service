package srdm.cloud.commonService.domain.repository.setting;

import srdm.cloud.commonService.domain.model.EditNetworkSetting;
import srdm.common.exception.SrdmDataAccessException;

public interface SrdmCustomPropertiesRepository {

	void updateNetworkSetting(EditNetworkSetting networkSetting) throws SrdmDataAccessException;
}
