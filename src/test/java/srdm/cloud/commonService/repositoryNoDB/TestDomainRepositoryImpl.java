package srdm.cloud.commonService.repositoryNoDB;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import srdm.cloud.commonService.domain.model.Domain;
import srdm.cloud.commonService.domain.model.EditDomain;
import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.Permission;
import srdm.cloud.commonService.domain.model.Role;
import srdm.cloud.commonService.domain.model.SimpleDomain;
import srdm.cloud.commonService.domain.repository.domain.DomainRepository;
import srdm.cloud.commonService.util.OxmProcessor;
import srdm.cloud.commonService.util.SrdmIdGenerator;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

@Repository
public class TestDomainRepositoryImpl implements DomainRepository {

	//private static final Logger logger = LoggerFactory.getLogger(DomainRepositoryImpl.class);

	@Autowired
	OxmProcessor oxmProcessor;

	@Autowired
	SrdmIdGenerator srdmIdGenerator;

	/**
	 * ドメイン取得（単一）
	 * @throws SrdmDataNotFoundException
	 * @throws SrdmDataAccessException
	 */
	@Override
	public Domain findOne(String domainId) throws SrdmDataNotFoundException, SrdmDataAccessException {

		Domain domain = new Domain();
		domain.setDomainId(domainId/*"D-51a29222-8f56-499e-849c-4e66968c316c"*/);
		domain.setDomainName("Developer");
		domain.setParentDomainId("1");
		domain.setTheme("Graphite");
		List<Permission> permissionList = new ArrayList<Permission>();
		String[] permissionNameList = {
				"cloudService", "group", "system",
				"fileDistribution", "deviceCloning", "powerManagement",
				"service", "agentInstall", "fieldSupportSystem",
				"firmwareUpdate", "domain", "account",
				"accountOfOwnDomain", "browse", "advancedFeatureBasic"};

		String[] permissionAttributeList = {
				"visible", "visible", "visible",
				"visible", "visible", "visible",
				"visible", "visible", "visible",
				"visible", "visible", "visible",
				"visible", "visible", "visible"};

		Role role = new Role();
		role.setRoleId("R-bb5bdf35-0d87-44ea-86e3-4e56336ce398");
		role.setRoleName("developer role");
		role.setDescription("For Developer account.");

		for(int i=0; i<permissionNameList.length; i++){
			Permission permission = new Permission();
			permission.setPermissionName(permissionNameList[i]);
			permission.setAttribute(permissionAttributeList[i]);
			permissionList.add(permission);
		}

		role.setPermissionList(permissionList);
		List<Role> roleList = new ArrayList<Role>();
		roleList.add(role);
		domain.setRoleList(roleList);

		return domain;
	}

	@Override
	public boolean isUnderDomain(String srcDomainId, String targetDomainId) throws SrdmDataAccessException {

		boolean bRet = true;
		return bRet;
	}

	/**
	 * ドメインの存在チェック
	 * true:指定ドメインが存在／false:指定ドメインが存在しない。
	 * @throws SrdmDataAccessException
	 */
	@Override
	public boolean isExistDomain(String domainId) throws SrdmDataAccessException {

		boolean bRet = true;
		return bRet;
	}

	/**
	 * Themeの更新
	 */
	@Override
	public void updateTheme(String domainId, String theme) throws SrdmDataAccessException {
		// DBアクセス（スタブ）
	}

	/**
	 * 配下のdomainId一覧を取得
	 */
	@Override
	public List<String> findUnderDomainId(String domainId) throws SrdmDataAccessException {

		List<String> listResult = new ArrayList<String>();

		for(int i=0; i<7; i++){
			listResult.add(srdmIdGenerator.generateDomainId());
		}
		return listResult;
	}

	/**
	 * parentDomainIdを指定してドメイン一覧を取得
	 */
	@Override
	public List<SimpleDomain> findAllByParentDomainIdWithPagable(GetListReq getListReq) throws SrdmDataAccessException {

		List<SimpleDomain> list = new ArrayList<SimpleDomain>();

		SimpleDomain domain1 = new SimpleDomain();
		SimpleDomain domain2 = new SimpleDomain();
		SimpleDomain domain3 = new SimpleDomain();

		domain1.setDomainId(srdmIdGenerator.generateDomainId());
		domain1.setDomainName("test1");
		domain1.setParentDomainId("D-51a29222-8f56-499e-849c-4e66968c316c");
		domain2.setDomainId(srdmIdGenerator.generateDomainId());
		domain2.setDomainName("test2");
		domain2.setParentDomainId("D-51a29222-8f56-499e-849c-4e66968c316c");
		domain3.setDomainId(srdmIdGenerator.generateDomainId());
		domain3.setDomainName("test3");
		domain3.setParentDomainId("D-51a29222-8f56-499e-849c-4e66968c316c");
		list.add(domain1);
		list.add(domain2);
		list.add(domain3);
		return list;
	}

	/**
	 * parentDomainIdを指定して件数を取得
	 */
	@Override
	public long count(String parentDomainId) throws SrdmDataNotFoundException, SrdmDataAccessException {

		long count = 7l;
		return count;
	}

	/**
	 * ドメインを作成
	 */
	@Override
	public String create(Domain domain) throws SrdmDataAccessException {

		String id = "D-51a29222-8f56-499e-849c-4e66968c316d";
		return id;
	}

	/**
	 * ドメインを更新
	 * @throws SrdmDataNotFoundException
	 */
	@Override
	public void update(EditDomain domain) throws SrdmDataAccessException, SrdmDataNotFoundException {
		// DBアクセス（スタブ）
	}

	/**
	 * ドメインを削除
	 */
	@Override
	public void delete(List<String> domainIdList) throws SrdmDataAccessException {
		// DBアクセス（スタブ）
	}

	/**
	 * 同一ドメイン名のチェック
	 * excludeDomainIdに指定したdomainIdを除いてチェックする。
	 * （ドメイン作成／編集時、自身のドメイン名を除くために使用）
	 * true:同一名のドメインが存在／false:同一名のドメインが存在しない
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public boolean isExistDomainName(String domainName, String excludeDomainId) throws SrdmDataAccessException {

		boolean bRet = false;
		return bRet;
	}

}
