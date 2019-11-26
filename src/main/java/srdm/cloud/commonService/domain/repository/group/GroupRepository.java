package srdm.cloud.commonService.domain.repository.group;

import java.util.List;

import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.SimpleGroup;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

public interface GroupRepository {

	SimpleGroup findOne(String groupId) throws SrdmDataNotFoundException, SrdmDataAccessException;
	List<SimpleGroup> findAllByParentGroupIdWithPagable(GetListReq getListReq) throws SrdmDataAccessException;

	long count(String groupId) throws SrdmDataNotFoundException, SrdmDataAccessException;

	boolean isUnderGroup(String srcGroupId, String targetGroupId) throws SrdmDataAccessException;
}
