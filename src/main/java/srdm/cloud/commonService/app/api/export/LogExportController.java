package srdm.cloud.commonService.app.api.export;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import srdm.cloud.commonService.app.bean.CommonRequestData;
import srdm.cloud.commonService.app.bean.export.ExportOperationLogReqBean;
import srdm.cloud.commonService.app.bean.export.ExportOperationLogResBean;
import srdm.cloud.commonService.app.bean.export.ExportSystemLogReqBean;
import srdm.cloud.commonService.app.bean.export.ExportSystemLogResBean;
import srdm.cloud.commonService.domain.service.export.ExportLogService;
import srdm.common.constant.SrdmConstants;
import srdm.common.exception.SrdmBaseException;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;
import srdm.common.exception.SrdmGeneralException;

@RestController
@RequestMapping(value="/export")
public class LogExportController {

	private static final Logger logger = LoggerFactory.getLogger(LogExportController.class);

	private static final String CONTENT_DISPOSITION_FORMAT = "attachment; filename=\"%s\"; filename*=UTF-8''%s";

	@Autowired
	ExportLogService exportLogService;

	@RequestMapping(value="/exportOperationLog/")
	public ExportOperationLogResBean exportOperationLog(
			@Validated @RequestBody ExportOperationLogReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		ExportOperationLogResBean resBean = new ExportOperationLogResBean();
		long requestId = exportLogService.exportOperationLog(
				commonRequestData.getSessionId(), reqBean.getDomainId(), reqBean.getFormat());
		resBean.setRequestId(requestId);
		return resBean;
	}

	@RequestMapping(value="/exportSystemLog/")
	public ExportSystemLogResBean exportSystemLog(
			@Validated @RequestBody ExportSystemLogReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		ExportSystemLogResBean resBean = new ExportSystemLogResBean();
		long requestId = exportLogService.exportSystemLog(
				commonRequestData.getSessionId(), reqBean.getFormat());
		resBean.setRequestId(requestId);
		return resBean;
	}

	@RequestMapping(value="/downloadExportData/", method=RequestMethod.GET)
	public void downlodadExportData(@RequestParam(required=true) long requestId,
			HttpServletResponse response,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		logger.info("requestId:[{}]",requestId);
		String fileName = exportLogService.getDownloadFileName(commonRequestData.getSessionId(), requestId);

		String headerValue;
		try {
			headerValue = String.format(CONTENT_DISPOSITION_FORMAT,
					fileName, UriUtils.encode(fileName, SrdmConstants.SYSTEM_CHARSET_NAME));
		} catch (UnsupportedEncodingException e) {
			throw new SrdmGeneralException(SrdmConstants.ERROR_E0032, SrdmConstants.ERROR_MESSAGE_E0032, e);
		}

		try {
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION, headerValue);
			response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
			exportLogService.downloadExportLog(commonRequestData.getSessionId(), requestId, response.getOutputStream());
		} catch(SrdmGeneralException | SrdmDataNotFoundException | SrdmDataAccessException e) {
			logger.error("Log export error.", e);
			response.resetBuffer();
			response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "");
			throw e;
		} catch (IOException e) {
			logger.error("Log export error(I/O error).", e);
			response.resetBuffer();
			response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "");
			throw new SrdmDataAccessException("Log export error.", e);
		}
	}

}
