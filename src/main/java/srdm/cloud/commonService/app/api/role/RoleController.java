package srdm.cloud.commonService.app.api.role;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import srdm.cloud.commonService.app.bean.CommonRequestData;
import srdm.cloud.commonService.app.bean.role.CreateRoleReqBean;
import srdm.cloud.commonService.app.bean.role.CreateRoleResBean;
import srdm.cloud.commonService.app.bean.role.DeleteRoleReqBean;
import srdm.cloud.commonService.app.bean.role.DeleteRoleResBean;
import srdm.cloud.commonService.app.bean.role.EditRoleReqBean;
import srdm.cloud.commonService.app.bean.role.EditRoleResBean;
import srdm.cloud.commonService.app.bean.role.GetRoleListReqBean;
import srdm.cloud.commonService.app.bean.role.GetRoleListReqBean.GetRoleList;
import srdm.cloud.commonService.app.bean.role.GetRoleListResBean;
import srdm.cloud.commonService.app.bean.role.GetRoleReqBean;
import srdm.cloud.commonService.app.bean.role.GetRoleResBean;
import srdm.cloud.commonService.domain.model.EditRole;
import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.GetListRes;
import srdm.cloud.commonService.domain.model.Permission;
import srdm.cloud.commonService.domain.model.Role;
import srdm.cloud.commonService.domain.model.RoleAttribute;
import srdm.cloud.commonService.domain.model.SimpleRole;
import srdm.cloud.commonService.domain.service.domain.RoleService;
import srdm.common.constant.SrdmConstants;
import srdm.common.exception.SrdmBaseException;

@RestController
@RequestMapping(value="/role")
public class RoleController {

//	private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

	@Autowired
	RoleService roleService;

	/**
	 * ロールリスト取得
	 */
	@RequestMapping(value="/getRoleList/", method=RequestMethod.POST)
	public GetRoleListResBean getRoleList(
			@Validated(GetRoleList.class) @RequestBody GetRoleListReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		GetRoleListResBean resBean = new GetRoleListResBean();

		GetListReq getListReq = new GetListReq();
		if(reqBean.getStartIndex() != 0) {
			getListReq.setStartIndex(reqBean.getStartIndex());
		}
		if(reqBean.getCount() != 0) {
			getListReq.setCount(reqBean.getCount());
		}
		if(reqBean.getSimpleFilter() != null && reqBean.getSimpleFilter().isEmpty() == false) {
			getListReq.setSimpleFilter(reqBean.getSimpleFilter());
		}
		if(reqBean.getOrderBy() != null && reqBean.getOrderBy().isEmpty() == false) {
			getListReq.setOrderBy(reqBean.getOrderBy());
		}
		getListReq.getKeyMap().put("domainId", reqBean.getDomainId());

		GetListRes getListRes = roleService.getRoles(commonRequestData.getSessionId(), getListReq);
		resBean.setStartIndex(getListReq.getStartIndex());
		resBean.setCount(getListReq.getCount());
		resBean.setDomainId(reqBean.getDomainId());
		resBean.setTotalCount(getListRes.getTotalCount());
		resBean.setResultCount(getListRes.getList().size());
		getListRes.getList().stream()
		.forEach(role -> resBean.addRole(
				((SimpleRole)role).getRoleId(),
				((SimpleRole)role).getRoleName(),
				((SimpleRole)role).isRoleCanEdit(),
				((SimpleRole)role).isPrivateRole(),
				((SimpleRole)role).getDescription(),
				((SimpleRole)role).isLinkedAccount(),
				((SimpleRole)role).isCanDelete(),
				((SimpleRole)role).getSessionTimeout()));

		return resBean;
	}

	/**
	 * ロールの取得（単一）
	 */
	@RequestMapping(value="/getRole/", method=RequestMethod.POST)
	public GetRoleResBean getRole(
			@Validated @RequestBody GetRoleReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		GetRoleResBean resBean = new GetRoleResBean();

		Role role = roleService.getRole(commonRequestData.getSessionId(), reqBean.getDomainId(), reqBean.getRoleId());
		// TODO:Bean間のマッピングをどうするか？
		BeanUtils.copyProperties(role, resBean);
		resBean.setRoleCanEdit(role.getRoleAttribute().isRoleCanEdit());
		resBean.setDomainId(reqBean.getDomainId());
		resBean.setRoleId(reqBean.getRoleId());
		return resBean;
	}

	/**
	 * ロールの編集
	 */
	@RequestMapping(value="/editRole/", method=RequestMethod.POST)
	public EditRoleResBean editRole(
			@Validated @RequestBody EditRoleReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		EditRoleResBean resBean = new EditRoleResBean();

		/**
		 * ロール情報編集は、reqBeanの内容をそのままRoleのオブジェクトにコピーするのではなく、
		 * 編集専用のオブジェクトにコピーしないといけない。（追加する権限／削除する権限とかがあるので。）
		 * 内容的には、reqBeanそのままだがレイヤーを考えると別オブジェクトへコピーすべき。
		 */
		// TODO:Domain Objectをnewするというのが、本来の実装か？また、Bean間のマッピングをどうするか？
		EditRole editRole = new EditRole();
		BeanUtils.copyProperties(reqBean, editRole);
		editRole.setRoleCanEdit(reqBean.isRoleCanEdit());

		roleService.update(commonRequestData.getSessionId(), editRole);
		return resBean;
	}

	/**
	 * ロール作成
	 */
	@RequestMapping(value="/createRole/", method=RequestMethod.POST)
	public CreateRoleResBean createRole(
			@Validated @RequestBody CreateRoleReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		CreateRoleResBean resBean = new CreateRoleResBean();

		Role role = new Role();
		role.setRoleName(reqBean.getRoleName());
		RoleAttribute roleAttribute = new RoleAttribute();
		roleAttribute.setRoleCanEdit(reqBean.isRoleCanEdit() == true ? true : false);
		roleAttribute.setPrivateRole(false);
		role.setRoleAttribute(roleAttribute);
		role.setDescription(reqBean.getDescription());
		List<Permission> permissionList = new ArrayList<Permission>();
		for(String permName : reqBean.getPermissionList()) {
			Permission permission = new Permission();
			permission.setPermissionName(permName);
			permission.setAttribute(SrdmConstants.PERMISSION_MAP.get(permName));
			permissionList.add(permission);
		}
		// 自ドメインアカウント管理権限の指定が無ければ、追加
		if(reqBean.getPermissionList().contains(SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN) == false) {
			Permission permission = new Permission();
			permission.setPermissionName(SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN);
			permission.setAttribute(SrdmConstants.PERMISSION_MAP.get(SrdmConstants.PERM_NAME_ACCOUNTOFOWNDOMAIN));
			permissionList.add(permission);
		}
		// 閲覧権限の指定が無ければ、追加
		if(reqBean.getPermissionList().contains(SrdmConstants.PERM_NAME_BROWSE) == false) {
			Permission permission = new Permission();
			permission.setPermissionName(SrdmConstants.PERM_NAME_BROWSE);
			permission.setAttribute(SrdmConstants.PERMISSION_MAP.get(SrdmConstants.PERM_NAME_BROWSE));
			permissionList.add(permission);
		}
		role.setPermissionList(permissionList);
		role.setSessionTimeout(reqBean.getSessionTimeout());

		String id = roleService.create(commonRequestData.getSessionId(), reqBean.getDomainId(), role);
		resBean.setRoleId(id);

		return resBean;
	}

	/**
	 * ロール削除
	 */
	@RequestMapping(value="/deleteRole/", method=RequestMethod.POST)
	public DeleteRoleResBean deleteRole(
			@Validated @RequestBody DeleteRoleReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		roleService.delete(commonRequestData.getSessionId(), reqBean.getDomainId(), reqBean.getRoleIdList());
		DeleteRoleResBean resBean = new DeleteRoleResBean();
		return resBean;
	}
}
