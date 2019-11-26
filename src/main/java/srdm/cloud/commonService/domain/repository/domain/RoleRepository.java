package srdm.cloud.commonService.domain.repository.domain;

import java.util.List;

import srdm.cloud.commonService.domain.model.EditRole;
import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.Role;
import srdm.cloud.commonService.domain.model.SimpleRole;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

public interface RoleRepository {

	Role findOne(String domainId, String roleId) throws SrdmDataNotFoundException, SrdmDataAccessException;
	List<SimpleRole> findAllWithPagable(GetListReq getListReq) throws SrdmDataAccessException;

	List<Role> findAllByDomainId(String domainId) throws SrdmDataAccessException;	

	String count(GetListReq getListReq) throws SrdmDataAccessException, SrdmDataNotFoundException;

	String create(String domainId, Role role) throws SrdmDataAccessException;
	void update(EditRole editRole) throws SrdmDataAccessException, SrdmDataNotFoundException;
	void updatePrivateRoleAttribute(String domainId, String roleId, boolean editPrivateRole) throws SrdmDataAccessException, SrdmDataNotFoundException;
	void delete(String domainId, List<String> roleIdList) throws SrdmDataAccessException;
	boolean isExist(String domainId, String roleId) throws SrdmDataAccessException;
	Role getRoleDetails(String domainId, String roleId) throws SrdmDataNotFoundException, SrdmDataAccessException;
}
