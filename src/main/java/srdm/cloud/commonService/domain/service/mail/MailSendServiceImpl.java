package srdm.cloud.commonService.domain.service.mail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import srdm.cloud.commonService.domain.model.AccountNortificationInfo;
import srdm.cloud.commonService.domain.model.LogItem;
import srdm.cloud.commonService.domain.service.domain.DomainServiceImpl;
import srdm.cloud.commonService.domain.service.log.OpeLogWriteService;
import srdm.cloud.commonService.util.MailAlert.MailDestinationInfo;
import srdm.cloud.commonService.util.MailAlert.mail.accountInfo.AccountInfoEmailContent;
import srdm.cloud.commonService.util.MailAlert.mail.accountInfo.AccountInfoEmailNortify;
import srdm.common.constant.SrdmConstants;
import srdm.common.constant.SrdmLogConstants;
import srdm.common.exception.SrdmGeneralException;

@Service
public class MailSendServiceImpl implements MailSendService {

	private static final Logger logger = LoggerFactory.getLogger(DomainServiceImpl.class);

	@Autowired
	OpeLogWriteService opelogWriteService;

	@Override
	public void sendEmailNortify(String sessionId, AccountNortificationInfo accountNortificationInfo) throws SrdmGeneralException {

		// 送信先情報作成
		MailDestinationInfo mailDestinationInfo = new MailDestinationInfo();
		mailDestinationInfo.setToAddress(accountNortificationInfo.getToAddress());
		mailDestinationInfo.setCcAddress(accountNortificationInfo.getCcAddress());
		mailDestinationInfo.setBccAddress(accountNortificationInfo.getBccAddress());

		AccountInfoEmailNortify accountInfoEmailNortify = new AccountInfoEmailNortify(mailDestinationInfo);

		AccountInfoEmailContent content = new AccountInfoEmailContent();
		content.setSubject(accountNortificationInfo.getSubject());
		content.setBody(accountNortificationInfo.getBody());

		// 操作ログ情報編集
		List<LogItem> itemList = new ArrayList<LogItem>();
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_TO_ADDRESS, accountNortificationInfo.getToAddress()));
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_CC_ADDRESS, accountNortificationInfo.getCcAddress()));
		itemList.add(new LogItem(SrdmLogConstants.OPELOG_ITEM_NAME_BCC_ADDRESS, accountNortificationInfo.getBccAddress()));

		try {
			logger.info("Account Information Mail Notification: Start.");
			boolean blRet = accountInfoEmailNortify.execute(content);
			if(blRet == true) {
				logger.info("Account Information Mail Notification: Success.");
				// 操作ログ記録（成功）
				opelogWriteService.writeOperationLog(sessionId,
						SrdmLogConstants.OPELOG_OPERATION_EMAIL_NOTIFICATION,
						SrdmLogConstants.OPELOG_CODE_NORMAL,
						itemList);
			} else {
				logger.error("Account Information Mail Notification: Faild.");
				// 操作ログ記録（失敗：送信失敗）
				opelogWriteService.writeOperationLog(sessionId,
						SrdmLogConstants.OPELOG_OPERATION_EMAIL_NOTIFICATION,
						SrdmLogConstants.OPELOG_CODE_EMAIL_NORTIFY_FAILD,
						itemList);
				throw new SrdmGeneralException(SrdmConstants.ERROR_E0059, SrdmConstants.ERROR_MESSAGE_E0059);
			}
		} catch (IOException e) {
			logger.error("Account Information Mail Notification: error",e);
			// 操作ログ記録（失敗：送信失敗）
			opelogWriteService.writeOperationLog(sessionId,
					SrdmLogConstants.OPELOG_OPERATION_EMAIL_NOTIFICATION,
					SrdmLogConstants.OPELOG_CODE_EMAIL_NORTIFY_FAILD,
					itemList);
			throw new SrdmGeneralException(SrdmConstants.ERROR_E0059, SrdmConstants.ERROR_MESSAGE_E0059, e);
		}
	}

}
