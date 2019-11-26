package srdm.cloud.commonService.util;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import srdm.common.constant.SrdmLogConstants.LogType;

/**
 * DBデータのエクスポートに関する情報を保持。
 * （今までは、sngltonで実装していたが、今回は、Beanに登録（Spring Framework管理に）することで
 * Framework側でsingltonとして扱ってくれる）
 *
 */
@Component
public class ExportDataInfo {
	private static final Logger logger = LoggerFactory.getLogger(ExportDataInfo.class);

	@ToString
	@Getter
	@Setter
	private static class Info {
		private long timestamp;
		private LogType type;					// エクスポートするデータの種類（opelog/sysmgtlog）
		private String domainId;				// typeが"opelog"の場合、対象domainIdを保持
		private String format;					// xml/csvを保持。
	}

	private Long requestId = -1L;
	private static ConcurrentMap<Long, Info> infoMap = new ConcurrentHashMap<Long, Info>();

	synchronized public long addOpelog(String domainId, String format) {

		// requestIdの加算
		next();
		Info info = new Info();
		info.setType(LogType.OPERATION);
		info.setDomainId(domainId);
		info.setFormat(format);
		info.setTimestamp(System.currentTimeMillis());
		infoMap.put(requestId, info);
		return requestId;
	}

	synchronized public long addSysmgtlog(String format) {

		// requestIdの加算
		next();
		Info info = new Info();
		info.setType(LogType.SYSTEM);
		info.setDomainId("");
		info.setFormat(format);
		info.setTimestamp(System.currentTimeMillis());
		infoMap.put(requestId, info);
		return requestId;
	}

	public void remove(Long requestId) {

		infoMap.remove(requestId);
		cleanup();
	}

	public LogType getType(Long requestId) {

		Info info = infoMap.get(requestId);
		LogType type = null;
		if(info != null) {
			type = info.getType();
		}
		return type;
	}

	public String getDomainId(Long requestId) {

		Info info = infoMap.get(requestId);
		String domainId = "";
		if(info != null) {
			domainId = info.getDomainId();
		}
		return domainId;
	}

	public String getFormat(Long requestId) {

		Info info = infoMap.get(requestId);
		String format = "";
		if(info != null) {
			format = info.getFormat();
		}
		return format;
	}

	/**
	 * リクエストID取得
	 * @return リクエストID
	 */
	private Long next() {

		if (Long.MAX_VALUE == requestId) {

			requestId = -1L;
		}

		requestId++;

		return (requestId);
	}

	/**
	 * Cleanup処理
	 */
	synchronized private void cleanup() {

		long before = System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000); // 24時間前より以前のデータは削除
		if(infoMap.isEmpty() == false) {
			for(Entry<Long, Info> entry : infoMap.entrySet()) {
				Info info = entry.getValue();
				if(info.getTimestamp() < before) {
					Long requestId = entry.getKey();
					infoMap.remove(requestId);
					logger.info("cleanup: requestId=" + requestId);
				}
			}
		}
	}
}
