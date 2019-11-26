package srdm.cloud.commonService.app.api.setting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import srdm.cloud.commonService.app.bean.setting.SetSmtpSettingReqBean;
import srdm.cloud.commonService.validation.Validation;
import srdm.common.bean.ErrorInfoStore;

@Component
public class SetSMTPSettingValidator implements Validator {

	@Autowired
	Validation validation;

	@Override
	public boolean supports(Class<?> clazz) {

		// 対象のクラスであることをチェック
		return SetSmtpSettingReqBean.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		//既にエラーがある場合は、チェックしない
		if(errors.hasErrors() == true) {
			return;
		}

		SetSmtpSettingReqBean reqBean = (SetSmtpSettingReqBean) target;

		ErrorInfoStore errorStore = new ErrorInfoStore();

		// smtpHostが指定されている場合
		if(reqBean.getSmtpHost().isEmpty() == false) {
			validation.checkRequiredString(reqBean.getSmtpPort(), "smtpPort", errorStore);
			validation.checkRequiredString(reqBean.getFromAddress(), "fromAddress", errorStore);
		}

		// useAuthがtrueの場合
		if(Boolean.parseBoolean(reqBean.getUseAuth()) == true) {
			validation.checkRequiredString(reqBean.getUserName(), "userName", errorStore);
		}

		if (errorStore.hasError() == true) {
			// エラー情報をセット
			errorStore.getErrorList().stream()
			.forEach(e -> errors.rejectValue(e.getErrorField(), e.getErrorValue(), e.getErrorCode()));
			return;
		}

	}
}
