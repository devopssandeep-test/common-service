package srdm.cloud.commonService.repositoryNoDB;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import srdm.cloud.commonService.domain.repository.domainExtension.FssDomainExtensionRepository;
import srdm.cloud.commonService.util.OxmProcessor;
import srdm.common.exception.SrdmDataAccessException;

@Repository
public class TestFssDomainExtensionRepositoryImpl implements FssDomainExtensionRepository {

	@Autowired
	OxmProcessor oxmProcessor;

	@Override
	public void add(String domainId) throws SrdmDataAccessException {

	}

	@Override
	public void delete(List<String> domainIdList) throws SrdmDataAccessException {

	}

}
