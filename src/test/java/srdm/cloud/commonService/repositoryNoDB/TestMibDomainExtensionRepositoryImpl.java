package srdm.cloud.commonService.repositoryNoDB;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import srdm.cloud.commonService.domain.model.MibDomainExtension;
import srdm.cloud.commonService.domain.repository.domainExtension.MibDomainExtensionRepository;
import srdm.cloud.commonService.util.OxmProcessor;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

@Repository
public class TestMibDomainExtensionRepositoryImpl implements MibDomainExtensionRepository {


	@Autowired
	OxmProcessor oxmProcessor;

	@Override
	public MibDomainExtension findOne(String domainId) throws SrdmDataNotFoundException, SrdmDataAccessException {

		MibDomainExtension domainExtension = new MibDomainExtension();
		domainExtension.setDomainId(domainId);
		domainExtension.setGroupId("100100");
		domainExtension.setGroupName("test");

		return domainExtension;
	}

	@Override
	public void add(String domainId, String groupId) throws SrdmDataAccessException {

	}

	@Override
	public void update(String domainId, String targetGroupId) throws SrdmDataAccessException {

	}

	@Override
	public void delete(List<String> domainIdList) throws SrdmDataAccessException {

	}

}
