package srdm.cloud.commonService.app.api.log;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import srdm.cloud.commonService.app.bean.CommonRequestData;
import srdm.cloud.commonService.app.bean.log.DeleteOperationLogReqBean;
import srdm.cloud.commonService.app.bean.log.DeleteOperationLogResBean;
import srdm.cloud.commonService.app.bean.log.DeleteSystemLogResBean;
import srdm.cloud.commonService.app.bean.log.GetOperationLogDetailReqBean;
import srdm.cloud.commonService.app.bean.log.GetOperationLogDetailResBean;
import srdm.cloud.commonService.app.bean.log.GetOperationLogReqBean;
import srdm.cloud.commonService.app.bean.log.GetOperationLogReqBean.GetOperationLog;
import srdm.cloud.commonService.app.bean.log.GetOperationLogResBean;
import srdm.cloud.commonService.app.bean.log.GetSystemLogDetailReqBean;
import srdm.cloud.commonService.app.bean.log.GetSystemLogDetailResBean;
import srdm.cloud.commonService.app.bean.log.GetSystemLogReqBean;
import srdm.cloud.commonService.app.bean.log.GetSystemLogReqBean.GetSystemLog;
import srdm.cloud.commonService.app.bean.log.GetSystemLogResBean;
import srdm.cloud.commonService.app.bean.log.Item;
import srdm.cloud.commonService.app.bean.log.Log;
import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.GetListRes;
import srdm.cloud.commonService.domain.model.LogForView;
import srdm.cloud.commonService.domain.model.LogItem;
import srdm.cloud.commonService.domain.model.OperationLog;
import srdm.cloud.commonService.domain.model.SystemManagementLog;
import srdm.cloud.commonService.domain.service.log.LogService;
import srdm.common.exception.SrdmBaseException;

@RestController
@RequestMapping(value="/log")
public class OperationLogContoroller {

//	private static final Logger logger = LoggerFactory.getLogger(OperationLogContoroller.class);

	@Autowired
	LogService logService;

	/**
	 * 操作ログ取得
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value="/getOperationLog/", method=RequestMethod.POST)
	public GetOperationLogResBean getOperationLog(
			@Validated(GetOperationLog.class) @RequestBody GetOperationLogReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		GetOperationLogResBean resBean = new GetOperationLogResBean();

		GetListReq getListReq = new GetListReq();
		if(reqBean.getStartIndex() != 0) {
			getListReq.setStartIndex(reqBean.getStartIndex());
		}
		if(reqBean.getCount() != 0) {
			getListReq.setCount(reqBean.getCount());
		}
		if(reqBean.getSimpleFilter() != null && reqBean.getSimpleFilter().isEmpty() == false) {
			getListReq.setSimpleFilter(reqBean.getSimpleFilter());
		}
		if(reqBean.getOrderBy() != null && reqBean.getOrderBy().isEmpty() == false) {
			getListReq.setOrderBy(reqBean.getOrderBy());
		}
		getListReq.getKeyMap().put("domainId", reqBean.getDomainId());

		GetListRes getListRes = logService.getOperationLogs(commonRequestData.getSessionId(), getListReq);
		resBean.setStartIndex(getListReq.getStartIndex());
		resBean.setCount(getListReq.getCount());
		resBean.setDomainId(reqBean.getDomainId());
		resBean.setTotalCount(getListRes.getTotalCount());
		resBean.setResultCount(getListRes.getList().size());
		List<Log> logList = new ArrayList<Log>();
		for(Object obj : getListRes.getList()) {
			LogForView srcLog = (LogForView)obj;
			Log destLog = new Log();
			BeanUtils.copyProperties(srcLog, destLog);
			logList.add(destLog);
		}
		resBean.setLogList(logList);
		return resBean;
	}

	/**
	 * システム管理ログ取得
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value="/getSystemLog/", method=RequestMethod.POST)
	public GetSystemLogResBean getSytemLog(
			@Validated(GetSystemLog.class) @RequestBody GetSystemLogReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		GetSystemLogResBean resBean = new GetSystemLogResBean();

		GetListReq getListReq = new GetListReq();
		if(reqBean.getStartIndex() != 0) {
			getListReq.setStartIndex(reqBean.getStartIndex());
		}
		if(reqBean.getCount() != 0) {
			getListReq.setCount(reqBean.getCount());
		}
		if(reqBean.getSimpleFilter() != null && reqBean.getSimpleFilter().isEmpty() == false) {
			getListReq.setSimpleFilter(reqBean.getSimpleFilter());
		}
		if(reqBean.getOrderBy() != null && reqBean.getOrderBy().isEmpty() == false) {
			getListReq.setOrderBy(reqBean.getOrderBy());
		}

		GetListRes getListRes = logService.getSystemLogs(commonRequestData.getSessionId(), getListReq);
		resBean.setStartIndex(getListReq.getStartIndex());
		resBean.setCount(getListReq.getCount());
		resBean.setTotalCount(getListRes.getTotalCount());
		resBean.setResultCount(getListRes.getList().size());
		List<Log> logList = new ArrayList<Log>();
		for(Object obj : getListRes.getList()) {
			LogForView srcLog = (LogForView)obj;
			Log destLog = new Log();
			BeanUtils.copyProperties(srcLog, destLog);
			logList.add(destLog);
		}
		resBean.setLogList(logList);

		return resBean;
	}

	/**
	 * 操作ログ詳細取得
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value="/getOperationLogDetail/", method=RequestMethod.POST)
	public GetOperationLogDetailResBean getOperationLogDetail(
			@Validated @RequestBody GetOperationLogDetailReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		GetOperationLogDetailResBean resBean = new GetOperationLogDetailResBean();

		OperationLog log = logService.getOperationLog(commonRequestData.getSessionId(), reqBean.getDomainId(), reqBean.getLogId());
		resBean.setDomainId(reqBean.getDomainId());
		resBean.setLogId(reqBean.getLogId());
		resBean.setResultCount(log.getItemList().size());
		List<Item> logList = new ArrayList<Item>();
		for(Object obj : log.getItemList()) {
			LogItem srcLogItem = (LogItem)obj;
			Item destLogItem = new Item();
			BeanUtils.copyProperties(srcLogItem, destLogItem);
			logList.add(destLogItem);
		}
		resBean.setItemList(logList);

		return resBean;
	}

	/**
	 * システム管理ログ詳細取得
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value="/getSystemLogDetail/", method=RequestMethod.POST)
	public GetSystemLogDetailResBean getSystemLogDetail(
			@Validated @RequestBody GetSystemLogDetailReqBean reqBean,
			CommonRequestData commonRequestData) throws  SrdmBaseException {

		GetSystemLogDetailResBean resBean = new GetSystemLogDetailResBean();

		SystemManagementLog log = logService.getSystemLog(commonRequestData.getSessionId(), reqBean.getLogId());
		resBean.setLogId(reqBean.getLogId());
		resBean.setResultCount(log.getItemList().size());
		List<Item> logList = new ArrayList<Item>();
		for(Object obj : log.getItemList()) {
			LogItem srcLogItem = (LogItem)obj;
			Item destLogItem = new Item();
			BeanUtils.copyProperties(srcLogItem, destLogItem);
			logList.add(destLogItem);
		}
		resBean.setItemList(logList);

		return resBean;
	}

	/**
	 * 操作ログ削除
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value="/deleteOperationLog/", method=RequestMethod.POST)
	public DeleteOperationLogResBean deleteOperationLog(
			@Validated @RequestBody DeleteOperationLogReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		logService.deleteOperationLog(commonRequestData.getSessionId(), reqBean.getDomainId());

		DeleteOperationLogResBean resBean = new DeleteOperationLogResBean();
		return resBean;
	}

	/**
	 * システム管理ログ削除
	 * @throws SrdmBaseException
	 */
	@RequestMapping(value="/deleteSystemLog/", method=RequestMethod.POST)
	public DeleteSystemLogResBean deleteSystemLog(
			CommonRequestData commonRequestData) throws SrdmBaseException {

		logService.deleteSystemLog(commonRequestData.getSessionId());

		DeleteSystemLogResBean resBean = new DeleteSystemLogResBean();
		return resBean;
	}
}
