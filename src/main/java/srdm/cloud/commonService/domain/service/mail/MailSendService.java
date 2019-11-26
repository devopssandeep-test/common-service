package srdm.cloud.commonService.domain.service.mail;

import srdm.cloud.commonService.domain.model.AccountNortificationInfo;
import srdm.common.exception.SrdmGeneralException;

public interface MailSendService {

	void sendEmailNortify(String sessionId, AccountNortificationInfo accountNortificationInfo) throws SrdmGeneralException;
}
