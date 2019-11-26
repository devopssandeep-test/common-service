package srdm.cloud.commonService.domain.service.domain;

import java.util.List;

import srdm.cloud.commonService.domain.model.EditRole;
import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.GetListRes;
import srdm.cloud.commonService.domain.model.Role;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;
import srdm.common.exception.SrdmGeneralException;
import srdm.common.exception.SrdmParameterValidationException;

public interface RoleService {

	/**
	 * ロール関係
	 */
	Role getRole(String sessionId, String domainId, String roleId) throws SrdmDataNotFoundException, SrdmDataAccessException, SrdmGeneralException;
	GetListRes getRoles(String sessionId, GetListReq getListReq) throws SrdmDataAccessException, SrdmDataNotFoundException;

	String create(String sessionId, String domainId, Role role) throws SrdmDataAccessException, SrdmParameterValidationException, SrdmDataNotFoundException, SrdmGeneralException;
	void update(String sessionId, EditRole editRole) throws SrdmDataAccessException, SrdmDataNotFoundException, SrdmParameterValidationException, SrdmGeneralException;
	void delete(String sessionId, String domainId, List<String> roleIdList) throws SrdmDataAccessException, SrdmDataNotFoundException, SrdmGeneralException;
}
