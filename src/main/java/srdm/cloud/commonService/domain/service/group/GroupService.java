package srdm.cloud.commonService.domain.service.group;

import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.GetListRes;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

public interface GroupService {

	GetListRes getGroups(String sessionId, GetListReq getListReq) throws SrdmDataAccessException, SrdmDataNotFoundException;
}
