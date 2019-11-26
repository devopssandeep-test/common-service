package srdm.cloud.commonService.domain.repository.domainExtension;

import java.util.List;

import srdm.cloud.commonService.domain.model.MibDomainExtension;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

public interface MibDomainExtensionRepository {

	void add(String domainId, String groupId) throws SrdmDataAccessException;
	void update(String domainId, String targetGroupId) throws SrdmDataAccessException;
	void delete(List<String> domainIdList) throws SrdmDataAccessException;

	MibDomainExtension findOne(String domainId) throws SrdmDataNotFoundException, SrdmDataAccessException;
}
