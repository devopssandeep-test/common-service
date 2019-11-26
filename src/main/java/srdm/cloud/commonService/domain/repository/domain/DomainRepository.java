package srdm.cloud.commonService.domain.repository.domain;

import java.util.List;

import srdm.cloud.commonService.domain.model.Domain;
import srdm.cloud.commonService.domain.model.EditDomain;
import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.SimpleDomain;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

public interface DomainRepository {

	Domain findOne(String domainId) throws SrdmDataNotFoundException, SrdmDataAccessException;
	List<SimpleDomain> findAllByParentDomainIdWithPagable(GetListReq getListReq) throws SrdmDataAccessException;

	List<String> findUnderDomainId(String domainId) throws SrdmDataAccessException;

	long count(String parentDomainId) throws SrdmDataNotFoundException, SrdmDataAccessException;

	String create(Domain domain) throws SrdmDataAccessException;
	void update(EditDomain domain) throws SrdmDataAccessException, SrdmDataNotFoundException;
	void delete(List<String> domainIdList) throws SrdmDataAccessException;

	boolean isUnderDomain(String srcDomainId, String targetDomainId) throws SrdmDataAccessException;
	boolean isExistDomain(String domainId) throws SrdmDataAccessException;
	boolean isExistDomainName(String domainName, String excludeDomainId) throws SrdmDataAccessException;

	void updateTheme(String domainId, String theme) throws SrdmDataNotFoundException, SrdmDataAccessException;
}
