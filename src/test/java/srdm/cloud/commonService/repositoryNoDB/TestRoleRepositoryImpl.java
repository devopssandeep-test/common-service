package srdm.cloud.commonService.repositoryNoDB;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import srdm.cloud.commonService.domain.model.EditRole;
import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.Permission;
import srdm.cloud.commonService.domain.model.Role;
import srdm.cloud.commonService.domain.model.RoleAttribute;
import srdm.cloud.commonService.domain.model.SimpleRole;
import srdm.cloud.commonService.domain.repository.domain.RoleRepository;
import srdm.cloud.commonService.util.OxmProcessor;
import srdm.cloud.commonService.util.SrdmIdGenerator;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

@Repository
public class TestRoleRepositoryImpl implements RoleRepository {

	//private static final Logger logger = LoggerFactory.getLogger(RoleRepositoryImpl.class);

	@Autowired
	OxmProcessor oxmProcessor;

	@Autowired
	SrdmIdGenerator srdmIdGenerator;

	/**
	 * ロール取得（単一）
	 * @throws SrdmDataNotFoundException
	 * @throws SrdmDataAccessException
	 */
	@Override
	public Role findOne(String domainId, String roleId) throws SrdmDataNotFoundException, SrdmDataAccessException {

		Role role = new Role();
		List<Permission> permissionList = new ArrayList<Permission>();
		String[] permissionNameList = {
				"cloudService", "group", "system",
				"fileDistribution","deviceCloning", "powerManagement",
				"service", "agentInstall", "fieldSupportSystem",
				"firmwareUpdate", "domain", "account",
				"accountOfOwnDomain", "browse", "advancedFeatureBasic"};

		String[] permissionAttributeList = {
				"visible", "visible", "visible",
				"visible", "visible", "visible",
				"visible", "visible", "visible",
				"visible", "visible", "visible",
				"visible", "visible", "visible"};


		for(int i=0; i<permissionNameList.length; i++){
			Permission permission = new Permission();
			permission.setPermissionName(permissionNameList[i]);
			permission.setAttribute(permissionAttributeList[i]);
			permissionList.add(permission);
		}

		role.setRoleId(roleId);
		role.setRoleName("test");
		RoleAttribute roleAttribute = new RoleAttribute();
		roleAttribute.setRoleCanEdit(false);
		roleAttribute.setPrivateRole(false);
		role.setRoleAttribute(roleAttribute);
		role.setPermissionList(permissionList);
		role.setDescription("test role");
		return role;
	}

	/**
	 * ロール編集
	 * @throws SrdmDataAccessException
	 * @throws SrdmDataNotFoundException
	 */
	@Override
	public void update(EditRole editRole) throws SrdmDataAccessException, SrdmDataNotFoundException {
		// DBアクセス（スタブ）
	}

	/**
	 * ロールリストの件数取得
	 * @throws SrdmDataAccessException
	 * @throws SrdmDataNotFoundException
	 */
	@Override
	public String count(GetListReq getListReq) throws SrdmDataAccessException, SrdmDataNotFoundException {
		String count = "1";
		return count;
	}

	/**
	 * ロールリスト取得
	 * @throws SrdmDataAccessException
	 */
	@Override
	public List<SimpleRole> findAllWithPagable(GetListReq getListReq) throws SrdmDataAccessException {

		List<SimpleRole> list = new ArrayList<SimpleRole>();
		SimpleRole role = new SimpleRole();

		role.setCanDelete(true);
		role.setDescription("test");
		role.setLinkedAccount(false);
		role.setRoleName("test role");
		role.setRoleId(srdmIdGenerator.generateRoleId());
		role.setRoleCanEdit(false);
		list.add(role);

		return list;
	}

	/**
	 * ロール作成
	 */
	@Override
	public String create(String domainId, Role role) throws SrdmDataAccessException {

		// roleId set
		String id = srdmIdGenerator.generateRoleId();
		return id;
	}

	/**
	 * ロール削除
	 */
	@Override
	public void delete(String domainId, List<String> roleIdList) throws SrdmDataAccessException {
		// DBアクセス(スタブ)
	}

	/**
	 * ロールの存在チェック
	 */
	@Override
	public boolean isExist(String domainId, String roleId) throws SrdmDataAccessException {

		boolean bRet = true;
		return bRet;
	}

	/**
	 * ロールリスト（指定ドメイン配下のものを全て取得）
	 */
	@Override
	public List<Role> findAllByDomainId(String domainId) throws SrdmDataAccessException {

		Role role = new Role();
		Permission permission = new Permission();
		List<Permission> permissionList = new ArrayList<Permission>();

		permission.setPermissionName("account");
		permission.setAttribute("visible");
		permissionList.add(permission);

		role.setRoleId(srdmIdGenerator.generateRoleId());
		role.setRoleName("test from" + domainId);
		RoleAttribute roleAttribute = new RoleAttribute();
		roleAttribute.setRoleCanEdit(false);
		roleAttribute.setPrivateRole(false);
		role.setRoleAttribute(roleAttribute);
		role.setPermissionList(permissionList);
		role.setDescription("test role");

		List<Role> list = new ArrayList<Role>();
		return list;
	}

	@Override
	public void updatePrivateRoleAttribute(String domainId, String roleId, boolean editPrivateRole)
			throws SrdmDataAccessException, SrdmDataNotFoundException {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public Role getRoleDetails(String domainId, String roleId)
			throws SrdmDataNotFoundException, SrdmDataAccessException {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
