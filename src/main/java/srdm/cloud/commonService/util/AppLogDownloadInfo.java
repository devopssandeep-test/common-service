package srdm.cloud.commonService.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * システムログダウンロードのリクエストに関する情報を保持。
 * （今までは、sngltonで実装していたが、今回は、Beanに登録（Spring Framework管理に）することで
 * Framework側でsingltonとして扱ってくれる）
 *
 */
@Component
public class AppLogDownloadInfo {
	private static final Logger logger = LoggerFactory.getLogger(AppLogDownloadInfo.class);

	@ToString
	@Getter
	@Setter
	private static class Info {
		private long timestamp;
		private String logType;			// ダウンロードするログの種類を保持
		List<Resource> resourceList;
	}

	private Long requestId = -1L;
	private static ConcurrentMap<Long, Info> infoMap = new ConcurrentHashMap<Long, Info>();

	synchronized public long add(String logType, List<Resource> resourceList) {

		// requestIdの加算
		next();
		Info info = new Info();
		info.setLogType(logType);
		info.setResourceList(resourceList);
		info.setTimestamp(System.currentTimeMillis());
		infoMap.put(requestId, info);
		return requestId;
	}

	public void remove(Long requestId) {

		infoMap.remove(requestId);
		cleanup();
	}

	public String getLogType(Long requestId) {

		Info info = infoMap.get(requestId);
		String logType = "";
		if(info != null) {
			logType = info.getLogType();
		}
		return logType;
	}

	public List<Resource> getResourceList(Long requestId) {

		Info info = infoMap.get(requestId);
		List<Resource> resourceList;
		if(info != null) {
			resourceList = info.getResourceList();
		} else {
			resourceList = new ArrayList<Resource>();
		}
		return resourceList;
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
