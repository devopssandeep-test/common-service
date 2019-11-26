package srdm.cloud.commonService.domain.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * Listデータ取得用レスポンスオブジェクト
 *
 */
@Data
public class GetListRes implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 507016855663550744L;

	private long resultCount;
	private long totalCount;

	List<?> list;
}
