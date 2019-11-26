package srdm.cloud.commonService.domain.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import srdm.cloud.commonService.app.bean.OrderBy;
import srdm.cloud.commonService.app.bean.SimpleFilter;

/**
 * Listデータ取得用リクエストオブジェクト
 *
 */
@Data
public class GetListReq implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3047446623156714924L;

	private long startIndex = 1;
	private long count = 10;

	private List<SimpleFilter> simpleFilter = new ArrayList<SimpleFilter>();
	private List<OrderBy> orderBy = new ArrayList<OrderBy>();

	private Map<String, String> keyMap = new HashMap<String,String>();			// 取得時の条件（id）等を指定
	private Map<String, List<String>> keyListMap = new HashMap<String,List<String>>();	// 取得時の除外IDリストを指定
}
