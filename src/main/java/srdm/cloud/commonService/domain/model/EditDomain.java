package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import lombok.Data;

/**
 * ドメイン情報編集時のソースデータ用クラス クラス内容を見直し、通常のDomainクラスを継承した（もしくは、内部に持った）形で作成し、
 * 追加で必要な情報を持つクラスにするとか・・・。
 */
@Data
public class EditDomain implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -2617715982840959542L;

	private String domainId;
	private String domainName;
	private String targetGroupId;
}
