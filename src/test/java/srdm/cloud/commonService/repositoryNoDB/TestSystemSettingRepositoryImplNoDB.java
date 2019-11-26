package srdm.cloud.commonService.repositoryNoDB;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import srdm.cloud.commonService.domain.model.EditNetworkSetting;
import srdm.cloud.commonService.domain.model.EditSmtpSetting;
import srdm.cloud.commonService.domain.model.NetworkSetting;
import srdm.cloud.commonService.domain.model.SmtpSetting;
import srdm.cloud.commonService.domain.model.SystemSettingNetwork;
import srdm.cloud.commonService.domain.repository.setting.SystemSettingRepository;
import srdm.cloud.commonService.util.CryptoUtil;
import srdm.cloud.commonService.util.OxmProcessor;
import srdm.common.exception.SrdmDataAccessException;

@Repository
public class TestSystemSettingRepositoryImplNoDB implements SystemSettingRepository{

	@Autowired
	OxmProcessor oxmProcessor;

	@Autowired
	CryptoUtil cryptoUtil;

	@Override
	public SmtpSetting getSmtpSetting() throws SrdmDataAccessException {
		// TODO 自動生成されたメソッド・スタブ
		System.out.println("called System1");

				SmtpSetting smtpSetting = new SmtpSetting();
				smtpSetting.setFromAddress("user01@mail.com");
				smtpSetting.setHost("mail.host.com");
				smtpSetting.setPassword("password");
				smtpSetting.setPort("25");
				smtpSetting.setUseAuth("true");
				smtpSetting.setUserName("user01");
				smtpSetting.setUseSsl("false");

				return smtpSetting;
	}

	@Override
	public void updateSmtpSetting(EditSmtpSetting smtpSetting) throws SrdmDataAccessException {
		// TODO 自動生成されたメソッド・スタブ
		System.out.println("called System2");

	}

	@Override
	public SystemSettingNetwork getSystemSettingNetwork() throws SrdmDataAccessException {
		// TODO 自動生成されたメソッド・スタブ
		System.out.println("called System3");


		SystemSettingNetwork networkSetting = new SystemSettingNetwork();
		networkSetting.setEnablePublicHttpPort(true);
		networkSetting.setEnablePublicHttpsPort(true);
		networkSetting.setEnablePublicIp(true);
		networkSetting.setEnableSsl(false);
		networkSetting.setPublicHttpPort("18085");
		networkSetting.setPublicHttpsPort("18086");
		networkSetting.setPublicIp("192.168.205.128");
		return networkSetting;

	}

	@Override
	public NetworkSetting getNetworkSetting() throws SrdmDataAccessException {
		// TODO 自動生成されたメソッド・スタブ
		System.out.println("called System4");
				NetworkSetting networkSetting = new NetworkSetting();

				networkSetting.setHttpPortEnable("true");
				networkSetting.setHttpsPortEnable("true");
				networkSetting.setIpAddressEnable("true");
				networkSetting.setTunnelPortEnable("true");
				networkSetting.setHttpPort("18085");
				networkSetting.setHttpsPort("18086");
				networkSetting.setIpAddress("192.168.205.128");
				networkSetting.setTunnelPort("18084");
				return networkSetting;
	}

	@Override
	public void updateNetworkSetting(EditNetworkSetting networkSetting) throws SrdmDataAccessException {
		// TODO 自動生成されたメソッド・スタブ
		System.out.println("called System5: updateNetworkSetting");
		System.out.println("ipAddressEnable:" + networkSetting.getIpAddressEnable());
		System.out.println("ipAddress:" + networkSetting.getIpAddress());
		System.out.println("httpPortEnable:" + networkSetting.getHttpPortEnable());
		System.out.println("httpPort:" + networkSetting.getHttpPort());
		System.out.println("httpsPortEnable:" + networkSetting.getHttpsPortEnable());
		System.out.println("httpsPort:" + networkSetting.getHttpsPort());
		System.out.println("tunnelPortEnable:" + networkSetting.getTunnelPortEnable());
		System.out.println("tunnelPort:" + networkSetting.getTunnelPort());
	}

}
