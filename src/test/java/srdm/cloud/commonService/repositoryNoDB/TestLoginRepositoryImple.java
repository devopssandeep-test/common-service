package srdm.cloud.commonService.repositoryNoDB;

import org.springframework.stereotype.Repository;

import srdm.cloud.commonService.domain.repository.account.LoginRepository;

@Repository
public class TestLoginRepositoryImple implements LoginRepository {

	@Override
	public boolean checkLoginId(String loginId) {
		return true;
	}

}
