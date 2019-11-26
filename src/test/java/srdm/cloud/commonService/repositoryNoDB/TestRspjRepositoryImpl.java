package srdm.cloud.commonService.repositoryNoDB;

import org.springframework.stereotype.Repository;

import srdm.cloud.commonService.domain.repository.rspj.RspjRepository;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmGeneralException;

@Repository
public class TestRspjRepositoryImpl implements RspjRepository {

	@Override
	public String getRspStatus() {
		// TODO 自動生成されたメソッド・スタブ
		System.out.println("called Rspj1");
		return "S102";
	}

	@Override
	public String getEnableRsp() throws SrdmDataAccessException {
		// TODO 自動生成されたメソッド・スタブ
		System.out.println("called Rspj2 : getEnableRsp");
		return "enalbe";
	}

	@Override
	public void setEnableRsp(String rspEnableStatus) throws SrdmDataAccessException, SrdmGeneralException {
		// TODO 自動生成されたメソッド・スタブ
		System.out.println("called Rspj3 : setEnableRsp");
		System.out.println("rspEnableStatus :" + rspEnableStatus);

	}

	@Override
	public void loadNetworkSetting() throws SrdmDataAccessException {
		// TODO 自動生成されたメソッド・スタブ
		System.out.println("called Rspj4 : loadNetworkSetting");

	}

}
