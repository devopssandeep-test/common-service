package srdm.cloud.commonService.asynchronous;

public interface AsyncDeleteLog {

	void deleteOperationLog(String domainId, String accountId, long period);
	void deleteSystemLog(String domainId, String accountId, long period);
}
