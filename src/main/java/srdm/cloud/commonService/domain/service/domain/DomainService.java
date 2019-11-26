package srdm.cloud.commonService.domain.service.domain;

import java.util.List;

import srdm.cloud.commonService.domain.model.CreateDomain;
import srdm.cloud.commonService.domain.model.Domain;
import srdm.cloud.commonService.domain.model.DomainDetail;
import srdm.cloud.commonService.domain.model.EditDomain;
import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.GetListRes;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;
import srdm.common.exception.SrdmGeneralException;
import srdm.common.exception.SrdmParameterValidationException;

public interface DomainService {

	/**
	 * ドメイン関係
	 */
	Domain getDomain(String sessionId, String domainId) throws SrdmDataNotFoundException, SrdmDataAccessException;
	GetListRes getDomainsByParentDomainId(String sessionId, GetListReq getListReq) throws SrdmDataAccessException, SrdmDataNotFoundException;

	DomainDetail getDomainDetail(String sessionId, String domainId) throws SrdmDataNotFoundException, SrdmDataAccessException;

	String getThemeSetting(String sessionId) throws SrdmDataNotFoundException, SrdmDataAccessException;
	void setThemeSetting(String sessionId, String theme) throws SrdmDataNotFoundException, SrdmDataAccessException;

	String create(String sessionId, CreateDomain createDomain) throws SrdmDataAccessException, SrdmGeneralException, SrdmDataNotFoundException, SrdmParameterValidationException;
	void update(String sessionId, EditDomain editDomain) throws SrdmDataAccessException, SrdmDataNotFoundException, SrdmGeneralException, SrdmParameterValidationException;
	void delete(String sessionId, List<String> domainIdList) throws SrdmDataAccessException, SrdmDataNotFoundException, SrdmGeneralException;
}
