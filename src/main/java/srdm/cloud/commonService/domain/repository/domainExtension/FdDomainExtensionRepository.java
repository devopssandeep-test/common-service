package srdm.cloud.commonService.domain.repository.domainExtension;

import java.util.List;

import srdm.common.exception.SrdmDataAccessException;

public interface FdDomainExtensionRepository {

	void add(String domainId) throws SrdmDataAccessException;
	void delete(List<String> domainIdList) throws SrdmDataAccessException;
}
