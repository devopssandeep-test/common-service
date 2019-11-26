package srdm.cloud.commonService.domain.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * ロール情報編集時のソースデータ用クラス
 * クラス内容を見直し、通常のRoleクラスを継承した（もしくは、内部に持った）形で作成し、
 * 追加で必要な情報を持つクラスにするとか・・・。
 */
@Data
public class EditRole implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -2145699543656921414L;
	@JsonProperty
	private String domainId;
	@JsonProperty
	private String roleId;
	@JsonProperty
	private String roleName;
	@JsonProperty
	private boolean isRoleCanEdit;
	@JsonProperty
	private String description;
	@JsonProperty
	private List<String> grantedPermissionList;
	@JsonProperty
	private List<String> deprivedPermissionList;
	@JsonProperty
	private long sessionTimeout;
}
