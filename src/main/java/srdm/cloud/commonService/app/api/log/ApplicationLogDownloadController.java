package srdm.cloud.commonService.app.api.log;

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
import srdm.cloud.commonService.app.bean.log.MakeDownloadLogFileReqBean;
import srdm.cloud.commonService.app.bean.log.MakeDownloadLogFileResBean;
import srdm.cloud.commonService.domain.service.log.AppLogService;
import srdm.common.constant.SrdmConstants;
import srdm.common.exception.SrdmBaseException;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;
import srdm.common.exception.SrdmGeneralException;

@RestController
@RequestMapping(value="/log")
public class ApplicationLogDownloadController {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationLogDownloadController.class);

	private static final String CONTENT_DISPOSITION_FORMAT = "attachment; filename=\"%s\"; filename*=UTF-8''%s";

	@Autowired
	AppLogService appLogService;

	/**
	 * システムログ（AppLog）ダウンロードリクエスト
	 */
	@RequestMapping(value="/makeDownloadLogFile/", method=RequestMethod.POST)
	public MakeDownloadLogFileResBean makeDownloadLogFile(
			@Validated @RequestBody MakeDownloadLogFileReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		MakeDownloadLogFileResBean resBean = new MakeDownloadLogFileResBean();

		long requestId = appLogService.makeDownloadLogFile(commonRequestData.getSessionId(), reqBean.getLogType());
		resBean.setRequestId(requestId);
		return resBean;
	}

	/**
	 * システムログ（AppLog）ダウンロード
	 */
	@RequestMapping(value="/downloadLogFile/", method=RequestMethod.GET)
	public void downloadLogFile(@RequestParam(required=true) long requestId,
			HttpServletResponse response,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		logger.info("requestId:[{}]",requestId);

		String fileName = appLogService.getDownloadFileName(commonRequestData.getSessionId(), requestId);

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
			appLogService.downloadAppLog(commonRequestData.getSessionId(), requestId, response.getOutputStream());
		} catch(SrdmGeneralException | SrdmDataNotFoundException e) {
			logger.error("Application log download error.", e);
			response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "");
			throw e;
		} catch (IOException e) {
			logger.error("Application log download error(I/O error).", e);
			response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "");
			throw new SrdmDataAccessException("Application log download error.", e);
		}
	}
}
