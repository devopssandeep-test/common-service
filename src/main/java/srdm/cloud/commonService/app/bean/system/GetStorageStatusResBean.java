package srdm.cloud.commonService.app.bean.system;

import lombok.Data;
import lombok.EqualsAndHashCode;
import srdm.common.bean.JsonBaseResBean;

/**
 * GetStorageStatusResBeanクラス
 *
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class GetStorageStatusResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -3597748448749033930L;

	private String status;
}
