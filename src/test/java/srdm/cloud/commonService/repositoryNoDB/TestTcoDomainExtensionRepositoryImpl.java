package srdm.cloud.commonService.repositoryNoDB;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import srdm.cloud.commonService.domain.repository.domainExtension.TcoDomainExtensionRepository;
import srdm.cloud.commonService.util.OxmProcessor;
import srdm.common.exception.SrdmDataAccessException;

@Repository
public class TestTcoDomainExtensionRepositoryImpl implements TcoDomainExtensionRepository {

	@Autowired
	OxmProcessor oxmProcessor;

	@Override
	public void add(String domainId) throws SrdmDataAccessException {

	}

	@Override
	public void delete(List<String> domainIdList) throws SrdmDataAccessException {
	}

}
