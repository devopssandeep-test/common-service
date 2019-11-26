package srdm.cloud.commonService.repositoryNoDB;

import org.springframework.stereotype.Repository;

import srdm.cloud.commonService.domain.model.DeleteLogSetting;
import srdm.cloud.commonService.domain.repository.schedule.ScheduleRepository;
import srdm.common.constant.SrdmLogConstants.LogType;
import srdm.common.exception.SrdmDataAccessException;

@Repository
public class TestScheduleRepositoryImpl implements ScheduleRepository{

	@Override
	public boolean isExistMibSchedule(String groupId) throws SrdmDataAccessException {
		// TODO 自動生成されたメソッド・スタブ
		System.out.println("called Schedule1 : isExistMibSchedule");
		return true;
	}

	@Override
	public void insertMibSchedule(String groupId, String domainId, String accountId) throws SrdmDataAccessException {
		// TODO 自動生成されたメソッド・スタブ
		System.out.println("called Schedule2 : insertMibSchedule");

	}

	@Override
	public DeleteLogSetting findDeleteLogSchedule(LogType logType) throws SrdmDataAccessException {
		// TODO 自動生成されたメソッド・スタブ
		System.out.println("called Schedule3 : findDeleteLogSchedule");
		DeleteLogSetting deleteLogSetting = new DeleteLogSetting();
		deleteLogSetting.setAccountId("A-Test");
		deleteLogSetting.setDomainId("D-Test");
		deleteLogSetting.setPeriod(0);
		deleteLogSetting.setStartHour(0);
		deleteLogSetting.setStartMinute(0);
		deleteLogSetting.setExecFlag(true);
		return deleteLogSetting;
	}

	@Override
	public void updateDeleteLogSchedule(LogType logType, DeleteLogSetting deleteLogSetting)
			throws SrdmDataAccessException {
		// TODO 自動生成されたメソッド・スタブ
		System.out.println("called Schedule4 : updateDeleteLogSchedule");

	}

	@Override
	public void shutdownScheduleService() throws SrdmDataAccessException {
		// TODO 自動生成されたメソッド・スタブ
		System.out.println("called Schedule5 : shutdownScheduleService");

	}

}
