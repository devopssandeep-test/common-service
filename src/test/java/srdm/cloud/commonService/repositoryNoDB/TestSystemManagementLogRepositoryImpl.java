package srdm.cloud.commonService.repositoryNoDB;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.LogForView;
import srdm.cloud.commonService.domain.model.LogItem;
import srdm.cloud.commonService.domain.model.SystemManagementLog;
import srdm.cloud.commonService.domain.repository.log.SystemManagementLogRepository;
import srdm.cloud.commonService.util.OxmProcessor;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

@Repository
public class TestSystemManagementLogRepositoryImpl implements SystemManagementLogRepository {

	@Autowired
	OxmProcessor oxmProcessor;

	// アカウントロック時間（単位：分）
	@Value("${srdm.auth.lockedTime}")
	private long lockedTime;

	/**
	 * システム管理ログ取得（詳細項目用）
	 *
	 * @throws SrdmDataAccessException
	 * @throws SrdmDataNotFoundException
	 */
	@Override
	public SystemManagementLog finedOne(String logId) throws SrdmDataAccessException, SrdmDataNotFoundException {

		SystemManagementLog log = new SystemManagementLog();
		List<LogItem> itemList = new ArrayList<LogItem>();


		for(int i=0; i<4; i++){
			LogItem item = new LogItem();
			item.setName("test" + i);
			item.setValue("SystemManagementLogTest findOne NoDB");
			itemList.add(item);
		}

		log.setAccountId("A-d30a67bc-317a-4c60-b9b8-82446e839183");
		log.setAccountName("developer");
		log.setCode("0000");
		log.setDomainId("D-51a29222-8f56-499e-849c-4e66968c316c");
		log.setDomainName("Developer");
		log.setKind("test");
		log.setLogId("55457633-8153-4d0f-891d-af8a39a7816e");
		log.setOperation("test SystemManagementLog finedOne");
		log.setItemList(itemList);
		return log;
	}

	/**
	 * システム管理ログ取得（リスト表示用）
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
		logForView.setOperation("test SystemManagementLog findAllWithPagable");

		for(int i=0; i<5; i++){
			list.add(logForView);
		}
		return list;
	}

	/**
	 * システム管理ログ追加
	 */
	@Override
	public void add(SystemManagementLog log) throws SrdmDataAccessException {
	}

	/**
	 * システム管理ログ削除（全削除）
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public void deleteAll() throws SrdmDataAccessException {
	}

	/**
	 * システム管理ログ削除（timestamp指定）
	 *
	 * @throws SrdmDataAccessException
	 */
	@Override
	public void deleteByTimestamp(long timestamp) throws SrdmDataAccessException {
	}

	/**
	 * システム管理ログ件数
	 *
	 * @throws SrdmDataAccessException
	 * @throws SrdmDataNotFoundException
	 */
	@Override
	public Long count(GetListReq getListReq) throws SrdmDataAccessException, SrdmDataNotFoundException {
		Long count = 5l;
		return count;
	}

	/**
	 * システム管理ログ削除（domainId指定）
	 * （未使用）
	 */
	@Override
	public void deleteByDomainIds(List<String> domainIds) throws SrdmDataAccessException {
	}

	/**
	 * システム管理ログのexport（全てのログを出力する）
	 * （nowTimeは、実行時間。出力処理中にaccountStatusが変化しないように、呼出し元で現在時刻を指定）
	 */
	@Override
	public List<String> export(long nowTime, long startIndex, long endIndex) throws SrdmDataAccessException {
		List<String> listResult =new ArrayList<String>();
		listResult.add("test export");
		return listResult;
	}

}
