package srdm.cloud.commonService.app.api.maintenance;

import java.util.Calendar;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import srdm.cloud.commonService.app.bean.maintenance.SetScheduledMaintenanceSettingsReqBean;
import srdm.cloud.commonService.validation.Validation;
import srdm.common.bean.ErrorInfoStore;
import srdm.common.constant.SrdmConstants;

@Component
public class SetScheduledMaintenanceSettingsValidator implements Validator {

	@Autowired
	Validation validation;

	@Override
	public boolean supports(Class<?> clazz) {
		// 対象クラスかをチェック
		return SetScheduledMaintenanceSettingsReqBean.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		// 既にエラーがあれば、チェックしない。
		if(errors.hasErrors()) {
			return;
		}

		/**
		 * 独自の入力チェック処理の実装について
		 * チェック結果をErrorInfoStoreクラスにセットし、
		 * APIを抜けるときに引数のErrorsクラスにセットしてAPIを抜ける。
		 * この時、ErrorsクラスのrejectValueメソッドの引数にあるdefaultMessageにエラーコードをセットする事。
		 * ApiExceptionHandlerクラスは、defaultMessageからAPIのエラーコードとメッセージを設定する。
		 */
		ErrorInfoStore errorStore = new ErrorInfoStore();
		/*
		 * 必須チェック
		 */
		SetScheduledMaintenanceSettingsReqBean reqBean = (SetScheduledMaintenanceSettingsReqBean) target;
		final boolean execFlag = Boolean.parseBoolean(reqBean.getExecFlag());
		final boolean sendFlag = Boolean.parseBoolean(reqBean.getSendFlag());
		// 定期実行
		if (execFlag == true) {
			// 実行種別(周期)
			validation.checkRequiredString(reqBean.getExecType(), "execType", errorStore);
			// 日時
			validation.checkRequiredLong(reqBean.getExecDateTimestamp(), "execDateTimestamp", errorStore);
			validation.checkRequiredLong(reqBean.getExecTimeHour(), "execTimeHour", errorStore);
			validation.checkRequiredLong(reqBean.getExecTimeMinute(), "execTimeMinute", errorStore);
		}
		// 結果通知メール
		if (sendFlag == true) {
			// 宛先
			validation.checkRequiredString(reqBean.getToAddress(), "toAddress", errorStore);
		}
		if (errorStore.hasError() == true) {
			// エラー情報をセット
			errorStore.getErrorList().stream()
			.forEach(e -> errors.rejectValue(e.getErrorField(), e.getErrorValue(), e.getErrorCode()));
			return;
		}

		/*
		 * 入力値チェック
		 * 相関チェック
		 */
		// 実行種別
		if (reqBean.getExecType() != null) {
			if (SrdmConstants.getListScheduledMaintenanceExecType().contains(reqBean.getExecType()) == false) {
				errorStore.addError(SrdmConstants.ERROR_E0014, "execType", reqBean.getExecType(), "Out of Range.");
			} else 	if ("weekDay".equals(reqBean.getExecType()) == true) {
				// 曜日指定
				validation.checkRequiredList(reqBean.getWeekDay(), "weekDay", errorStore);
			} else if ("monthDate".equals(reqBean.getExecType()) == true) {
				// 日指定
				validation.checkRequiredList(reqBean.getMonthDate(), "monthDate", errorStore);
			} else {
				// "everyday"(毎日) or "once"(1回)
			}
		}
		if (errorStore.hasError() == true) {
			// エラー情報をセット
			errorStore.getErrorList().stream()
			.forEach(e -> errors.rejectValue(e.getErrorField(), e.getErrorValue(), e.getErrorCode()));
			return;
		}
		// 曜日指定
		if (reqBean.getWeekDay() != null) {
			for (String wday: reqBean.getWeekDay()) {
				if (wday == null) {
					if (execFlag == true) {
						errorStore.addError(SrdmConstants.ERROR_E0011, "weekDay", null, "This field is required.");
					}
				} else {
					if (SrdmConstants.getListWeekDay().contains(wday) == false) {
						errorStore.addError(SrdmConstants.ERROR_E0014, "weekDay", wday, "Out of Range.");
					}
				}
			}
		}
		// 日指定
		if (reqBean.getMonthDate() != null) {
			for (Long mday: reqBean.getMonthDate()) {
				if (mday == null) {
					if (execFlag == true) {
						errorStore.addError(SrdmConstants.ERROR_E0011, "monthDate", null, "This field is required.");
					}
				} else {
					validation.checkIsInRangeLong(mday.toString(), 1, 31, "monthDate", errorStore);
				}
			}
		}
		// 日付
		if (reqBean.getExecDateTimestamp() != null) {
			validation.checkIsInRangeLong(reqBean.getExecDateTimestamp().toString(), 0, Long.MAX_VALUE, "execDateTimestamp", errorStore);
		}
		// 時
		int hour = 0;
		if (reqBean.getExecTimeHour() != null) {
			if (validation.checkIsInRangeLong(reqBean.getExecTimeHour().toString(), 0, 23, "execTimeHour", errorStore) == true) {
				hour = (int)((long)reqBean.getExecTimeHour());
			}
		}
		// 分
		int minute = 0;
		if (reqBean.getExecTimeMinute() != null) {
			if (validation.checkIsInRangeLong(reqBean.getExecTimeMinute().toString(), 0, 59, "execTimeMinute", errorStore) == true) {
				minute = (int)((long)reqBean.getExecTimeMinute());
			}
		}
		// タイムゾーンID（バリデーションチェックは、reqBeanクラスにアノテーションで指定）

		// 日時書式（バリデーションチェックは、reqBeanクラスにアノテーションで指定）

		// 言語（バリデーションチェックは、reqBeanクラスにアノテーションで指定）

		// 宛先（バリデーションチェックは、reqBeanクラスにアノテーションで指定）

		if (errorStore.hasError() == true) {
			// エラー情報をセット
			errorStore.getErrorList().stream()
			.forEach(e -> errors.rejectValue(e.getErrorField(), e.getErrorValue(), e.getErrorCode()));
			return;
		}

		// 日時チェック("once"の場合は過去の日時は指定不可)
		if (execFlag == true && "once".equals(reqBean.getExecType()) == true) {
			String timeZoneId;
			if (reqBean.getTimeZoneId() != null && reqBean.getTimeZoneId().length() > 0) {
				timeZoneId = reqBean.getTimeZoneId();
			} else {
				timeZoneId = SrdmConstants.getDefaultTimeZoneId();
			}
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+0")); // execDateTimestampはGMT+00:00で日付が入っている(timeZoneIdによる変換はなし)
			cal.setTimeInMillis(reqBean.getExecDateTimestamp()); // UIでy年m月d日を指定した場合、y年m月d日0時0分0秒GMT+00:00がセットされている
			cal.setTimeZone(TimeZone.getTimeZone(timeZoneId)); // 時刻はtimeZoneIdに依存するため、ここでタイムゾーンをセットする
			cal.set(Calendar.HOUR_OF_DAY, hour);
			cal.set(Calendar.MINUTE, minute);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			long t = cal.getTimeInMillis();
			if (t < System.currentTimeMillis()) {
				errorStore.addError(SrdmConstants.ERROR_E0014, "execDateTimestamp", String.valueOf(reqBean.getExecDateTimestamp()), "Out of Range: execDateTimestamp/execTimeHour/execTimeMinute/timeZoneId"); // Expired
				errorStore.addError(SrdmConstants.ERROR_E0014, "execTimeHour", String.valueOf(hour), "Out of Range: execDateTimestamp/execTimeHour/execTimeMinute/timeZoneId"); // Expired
				errorStore.addError(SrdmConstants.ERROR_E0014, "execTimeMinute", String.valueOf(minute), "Out of Range: execDateTimestamp/execTimeHour/execTimeMinute/timeZoneId"); // Expired
				errorStore.addError(SrdmConstants.ERROR_E0014, "timeZoneId", reqBean.getTimeZoneId(), "Out of Range: execDateTimestamp/execTimeHour/execTimeMinute/timeZoneId"); // Expired
			}
		}
		// 相関チェック(時刻)
		if ((reqBean.getExecTimeHour() != null && reqBean.getExecTimeMinute() == null) || (reqBean.getExecTimeHour() == null && reqBean.getExecTimeMinute() != null)) {
			errorStore.addError(SrdmConstants.ERROR_E0018, "execTimeHour", String.valueOf(reqBean.getExecTimeHour()), "Both execTimeHour/execTimeMinute fields must be set.");
			errorStore.addError(SrdmConstants.ERROR_E0018, "execTimeMinute", String.valueOf(reqBean.getExecTimeMinute()), "Both execTimeHour/execTimeMinute fields must be set.");
		}
		if (errorStore.hasError() == true) {
			// エラー情報をセット
			errorStore.getErrorList().stream()
			.forEach(e -> errors.rejectValue(e.getErrorField(), e.getErrorValue(), e.getErrorCode()));
			return;
		}
	}

}
