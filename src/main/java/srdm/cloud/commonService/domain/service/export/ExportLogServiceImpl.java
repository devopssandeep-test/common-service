package srdm.cloud.commonService.domain.service.export;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.w3c.dom.Document;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;
import srdm.cloud.commonService.domain.model.Domain;
import srdm.cloud.commonService.domain.model.LogItem;
import srdm.cloud.commonService.domain.repository.domain.DomainRepository;
import srdm.cloud.commonService.domain.repository.log.OperationLogRepository;
import srdm.cloud.commonService.domain.repository.log.SystemManagementLogRepository;
import srdm.cloud.commonService.domain.repository.session.SrdmSessionRepository;
import srdm.cloud.commonService.domain.service.log.OpeLogWriteService;
import srdm.cloud.commonService.domain.service.log.SysMgtLogWriteService;
import srdm.cloud.commonService.util.ExportDataInfo;
import srdm.common.constant.SrdmConstants;
import srdm.common.constant.SrdmLogConstants;
import srdm.common.constant.SrdmLogConstants.LogType;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;
import srdm.common.exception.SrdmGeneralException;

@Service
public class ExportLogServiceImpl implements ExportLogService {

	private static final Logger logger = LoggerFactory.getLogger(ExportLogServiceImpl.class);

	private static final String PREFIX_OPELOG = "operationLog";
	private static final String PREFIX_SYSMGT = "systemManagementLog";
	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
	private static final String OPELOG_ROOT = "opelogList";
	private static final String SYSMGT_ROOT = "sysmgtlogList";

	private static final long EXPORT_START_INDEX = 1L;
	private static final long EXPORT_COUNT = 100L;

	@Autowired
	ExportDataInfo exportDataInfo;

	@Autowired
	OpeLogWriteService opelogWriteService;

	@Autowired
	SysMgtLogWriteService sysMgtLogWriteService;

	@Autowired
	DomainRepository domainRepository;

	@Autowired
	OperationLogRepository operationLogRepository;

	@Autowired
	SystemManagementLogRepository systemManagementLogRepository;

	@Autowired
	SrdmSessionRepository srdmSessionRepository;

	/**
	 * 操作ログのエクスポートリクエスト登録
	 */
	@Override
	public long exportOperationLog(String sessionId, String domainId, String format)
			throws SrdmDataAccessException, SrdmDataNotFoundException {

		// 指定ドメインが配下にあるのかのチェック
		String loginDomainId = srdmSessionRepository.getDomainId(sessionId);
		if (loginDomainId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "Unable to export the operation log.");
		}
		boolean isUnderDomain = domainRepository.isUnderDomain(loginDomainId, domainId);
		if (isUnderDomain == false) {
			logger.warn("domain is not included. domainId[{}]", domainId);
			throw new SrdmDataNotFoundException("domainId", "", "Unable to export the operation log.");
		}

		long requestId = exportDataInfo.addOpelog(domainId, format);
		logger.info("requestId:[{}]", requestId);

		return requestId;
	}

	/**
	 * システム管理ログのエクスポートリクエスト登録
	 */
	@Override
	public long exportSystemLog(String sessionId, String format) throws SrdmDataAccessException {

		long requestId = exportDataInfo.addSysmgtlog(format);
		logger.info("requestId:[{}]", requestId);

		return requestId;
	}

	/**
	 * ダウンロードファイル名取得
	 */
	@Override
	public String getDownloadFileName(String sessionId, Long requestId) throws SrdmDataNotFoundException {

		LogType type = exportDataInfo.getType(requestId);
		String prefix;
		if (type == null) {
			logger.error("requestId not found. requestId:[{}]", requestId);
			throw new SrdmDataNotFoundException("requestId not found.");
		} else {
			if (type == LogType.OPERATION) {
				prefix = PREFIX_OPELOG;
			} else {
				prefix = PREFIX_SYSMGT;
			}
		}

		// ログのタイプに応じてログファイル名のPrefixを決定
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		String fileName = String.format("%s-%04d%02d%02d%02d%02d%02d.json", prefix, cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY),
				cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));

		return fileName;
	}

	/**
	 * エクスポートデータのダウンロード
	 */
	@Override
	public void downloadExportLog(String sessionId, Long requestId, OutputStream os)
			throws SrdmGeneralException, SrdmDataNotFoundException, SrdmDataAccessException, IOException {

		LogType type = exportDataInfo.getType(requestId);
		if (type == null) {
			logger.error("requestId not found. requestId:[{}]", requestId);
			throw new SrdmDataNotFoundException("requestId not found.");
		} else {
			if (type == LogType.OPERATION) {
				String domainId = exportDataInfo.getDomainId(requestId);
				exportDataInfo.remove(requestId); // 管理情報削除
				downloadOperationLog(sessionId, domainId, os);
			} else if (type == LogType.SYSTEM) {
				exportDataInfo.remove(requestId); // 管理情報削除
				downloadSystemLog(sessionId, os);
			} else {
				// 通常はあり得ない
				logger.error("log export faild(log type error). requestId:[{}]", requestId);
			}
		}

	}

	// 操作ログのエクスポート
	private void downloadOperationLog(String sessionId, String domainId, OutputStream os)
			throws SrdmDataAccessException, IOException, SrdmDataNotFoundException {

		// 操作ログ詳細項目
		List<LogItem> itemList = new ArrayList<LogItem>();
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_ID, domainId));

		try {

			Domain domain = domainRepository.findOne(domainId);
			itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_DOMAIN_NAME, domain.getDomainName()));
		} catch (SrdmDataNotFoundException | SrdmDataAccessException e) {
			// 操作ログ記録時のdomainName取得エラー
			logger.error("[Operation Log write] domain name get error. domainId[{}]", domainId, e);
		}

		// 対象のdomainIdListを取得。但し、今後の対応でtargetDomainIdノードを追加し、それをキーに取得するように変更予定）
		List<String> domainIdList = domainRepository.findUnderDomainId(domainId);
		domainIdList.add(domainId);
		domainIdList = domainIdList.stream().distinct().collect(Collectors.toList());
	
		List<String> list;
		try {
			long nowTime = System.currentTimeMillis();

			list = operationLogRepository.export(domainIdList, nowTime, 0, 0);

			if (list.isEmpty()) {
				StringBuffer sb = new StringBuffer();
                String jsonStart = "{\""+OPELOG_ROOT+"\":[";
                String jsonEnd = "]}";
                sb.append(jsonStart);
                sb.append(jsonEnd);
                os.write(sb.toString().getBytes("UTF-8"));
			} else {
				String str = convertXml(list, SrdmLogConstants.OPELOG_OPERATION_EXPORT_LOG, sessionId);
				os.write(str.getBytes("UTF-8"));
			}
			os.flush();
			os.close();

			// 操作ログ記録（成功）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_EXPORT_LOG,
					SrdmLogConstants.OPELOG_CODE_NORMAL, itemList);
		} catch (SrdmDataAccessException | IOException e) {
			// 操作ログ記録（失敗：アクセスエラー）
			opelogWriteService.writeOperationLog(sessionId, SrdmLogConstants.OPELOG_OPERATION_EXPORT_LOG,
					SrdmLogConstants.OPELOG_CODE_EXPORT_OPELOG_FAILD, itemList);
			throw e;
		} catch (Exception err) {

		}
	}

	// システム管理ログのエクスポート
	private void downloadSystemLog(String sessionId, OutputStream os) throws SrdmDataAccessException, IOException {

		// 操作ログ詳細項目
		List<LogItem> itemList = new ArrayList<LogItem>();

		long startIndex = EXPORT_START_INDEX;
		List<String> list;
		try {
			long nowTime = System.currentTimeMillis();
			list = systemManagementLogRepository.export(nowTime, 0, 0);
			if (list.isEmpty()) {
				StringBuffer sb = new StringBuffer();
                String jsonStart = "{\""+SYSMGT_ROOT+"\":[";
                String jsonEnd = "]}";
                sb.append(jsonStart);
                sb.append(jsonEnd);
                os.write(sb.toString().getBytes("UTF-8"));

			} else {
				String str = convertXml(list, SrdmLogConstants.SYSMGT_OPERATION_EXPORT_LOG, sessionId);
				os.write(str.getBytes("UTF-8"));
			}

			os.flush();
			os.close();

			// システム管理ログ記録（成功）
			sysMgtLogWriteService.writeSystemManagementLog(sessionId, SrdmLogConstants.SYSMGT_OPERATION_EXPORT_LOG,
					SrdmLogConstants.SYSMGT_CODE_NORMAL, itemList);
		} catch (SrdmDataAccessException | IOException e) {
			// システム管理ログ記録（失敗：アクセスエラー）
			sysMgtLogWriteService.writeSystemManagementLog(sessionId, SrdmLogConstants.SYSMGT_OPERATION_EXPORT_LOG,
					SrdmLogConstants.SYSMGT_CODE_EXPORT_LOG_FAILD, itemList);
			throw e;
		} catch (Exception err) {

		}
	}

	private static String makeStartNode(String tag) {
		StringBuffer sb = new StringBuffer();
		sb.append("<");
		sb.append(tag);
		sb.append(">");
		return sb.toString();
	}

	private static String makeEndNode(String tag) {
		StringBuffer sb = new StringBuffer();
		sb.append("</");
		sb.append(tag);
		sb.append(">");
		return sb.toString();
	}

	/**
	 * 出力用にXML整形
	 *
	 * @param srcXmls
	 *            List<String>
	 * @param operation
	 *            操作
	 * @param sessionId
	 *            セッションID
	 * @return 整形されたXML
	 * @throws Exception
	 */
	private String convertXml(List<String> srcXmls, String operation, String sessionId) throws Exception {

		String convertXml = "";

		try {

			StringBuffer sb = new StringBuffer();

			for (String srcXml : srcXmls) {

				sb.append(srcXml);
			}

			String stringData = sb.toString();

			JSON json = JSONSerializer.toJSON(stringData);
			/*XMLSerializer xmlSerializer = new XMLSerializer();
			xmlSerializer.setTypeHintsEnabled(false);
			convertXml = xmlSerializer.write(json);

			convertXml = convertXml.replaceAll("\r\n", "");
			convertXml = convertXml.replaceAll("\r", "");
			convertXml = convertXml.replaceAll("\n", "");
			convertXml = convertXml.replaceAll(">[\\s]+<", "><");

			convertXml = convertXml.replaceAll("<o>", "");
			convertXml = convertXml.replaceAll("</o>", "");
			convertXml = convertXml.replaceAll("<e>", "");
			convertXml = convertXml.replaceAll("</e>", "");

			ByteArrayInputStream in = new ByteArrayInputStream(convertXml.getBytes("UTF-8"));
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();

			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

			StringWriter sw = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(sw));

			convertXml = sw.toString();*/
			convertXml = json.toString();
		} catch (Exception e) {

			throw (e);
		}
		return (convertXml);

	}

}
