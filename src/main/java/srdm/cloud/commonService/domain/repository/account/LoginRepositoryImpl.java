package srdm.cloud.commonService.domain.repository.account;

import org.springframework.stereotype.Repository;

import srdm.cloud.shared.system.ServerInfoService;

@Repository
public class LoginRepositoryImpl implements LoginRepository {

	@Override
	public boolean checkLoginId(String loginId) {

		return ServerInfoService.isOnPremisesLoginID(loginId);
	}

}
