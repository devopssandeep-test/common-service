package srdm.cloud.commonService.repositoryNoDB;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.LogForView;
import srdm.cloud.commonService.domain.model.LogItem;
import srdm.cloud.commonService.domain.model.OperationLog;
import srdm.cloud.commonService.domain.repository.log.OperationLogRepository;
import srdm.cloud.commonService.util.OxmProcessor;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

@Repository
public class TestOperationLogRepositoryImpl implements OperationLogRepository {

	@Autowired
	OxmProcessor oxmProcessor;

	// アカウントロック時間（単位：分）
	@Value("${srdm.auth.lockedTime}")
	private long lockedTime;

	/**
	 * 操作ログ取得（単一）
	 *
	 * @throws SrdmDataAccessException
	 * @throws SrdmDataNotFoundException
	 */
	@Override
	public OperationLog finedOne(List<String> domainIdList, String logId) throws SrdmDataAccessException, SrdmDataNotFoundException {

		OperationLog log = new OperationLog();
		List<LogItem> itemList = new ArrayList<LogItem>();


		for(int i=0; i<4; i++){
			LogItem item = new LogItem();
			item.setName("test" + i);
			item.setValue("OperationLogTest finedOne NoDB");
			itemList.add(item);
		}

		log.setAccountId("A-d30a67bc-317a-4c60-b9b8-82446e839183");
		log.setAccountName("developer");
		log.setCode("0000");
		log.setDomainId("D-51a29222-8f56-499e-849c-4e66968c316c");
		log.setDomainName("Developer");
		log.setKind("test");
		log.setLogId("a486588e-49e3-4468-8fb1-938d80b24c95");
		log.setOperation("test");
		log.setItemList(itemList);
		return log;
	}

	/**
	 * 操作ログ取得（リスト表示用）
	 * （複数ドメインの操作ログを取得）
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public List<LogForView> findAllWithPagable(GetListReq getListReq) throws SrdmDataAccessException {

		List<LogForView> list = new ArrayList<LogForView>();
		LogForView logForView = new LogForView();
		logForView.setAccountId("A-d30a67bc-317a-4c60-b9b8-82446e839183");
		logForView.setAccountName("developer");
		logForView.setCode("0000");
		logForView.setDomainId("D-51a29222-8f56-499e-849c-4e66968c316c");
		logForView.setDomainName("Developer");
		logForView.setKind("test");
		logForView.setLogId("a486588e-49e3-4468-8fb1-938d80b24c95");
		logForView.setOperation("test");
		list.add(logForView);
		return list;
	}

	/**
	 * 操作ログ追加
	 */
	@Override
	public void add(OperationLog log) throws SrdmDataAccessException {

	}

	/**
	 * 操作ログ削除（domainId指定）
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public void deleteByDomainIds(List<String> domainIds) throws SrdmDataAccessException {


	}

	/**
	 * 操作ログ削除（timestamp指定）
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public void deleteByTimestamp(long timestamp) throws SrdmDataAccessException {

	}

	/**
	 * 操作ログ件数
	 *
	 * @throws SrdmDataAccessException
	 * @throws SrdmDataNotFoundException
	 */
	@Override
	public Long count(GetListReq getListReq) throws SrdmDataAccessException, SrdmDataNotFoundException {

		Long count = 1l;
		return count;
	}

	/**
	 * 操作ログのエクスポート（全てのログを出力）
	 * （nowTimeは、実行時間。出力処理中にaccountStatusが変化しないように、呼出し元で現在時刻を指定）
	 */
	@Override
	public List<String> export(List<String> domainIdList, long nowTime, long startIndex, long endIndex)
			throws SrdmDataAccessException {

		final int end = 10;
		List<String> listResult =new ArrayList<String>();

		if(startIndex < end) {
			listResult.add("<log>data-0</log>");
			listResult.add("<log>data-1</log>");
			listResult.add("<log>data-2</log>");
			listResult.add("<log>data-3</log>");
			listResult.add("<log>data-4</log>");
			listResult.add("<log>data-5</log>");
			listResult.add("<log>data-6</log>");
			listResult.add("<log>data-7</log>");
			listResult.add("<log>data-8</log>");
			listResult.add("<log>data-9</log>");
		}
		return listResult;
	}
}
