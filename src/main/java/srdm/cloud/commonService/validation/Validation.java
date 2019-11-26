package srdm.cloud.commonService.validation;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.validator.GenericValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import srdm.common.bean.ErrorInfoStore;
import srdm.common.constant.SrdmConstants;

@Component
public class Validation {

	private static final Logger logger = LoggerFactory.getLogger(Validation.class);

	// Pattern: Invalid Format.
	// List: Out of Range.

	private static final Pattern PatternSessionId = Pattern.compile("^[_0-9A-Fa-f]+$");

	/**
	 *
	 * @param sessionId
	 * @return
	 */
	public boolean sessionId(String sessionId) {
		boolean bRet = false;
		if (sessionId != null && sessionId.length() > 0) {
			bRet = PatternSessionId.matcher(sessionId).find();
		}
		return bRet;
	}

	/**
	 * 以下、必須チェック
	 */
	/**
	 * Integer数値の必須チェック 主として必須項目の数値に使用することを想定しています。 空であればエラー情報を引数オブジェクトに追加します。
	 *
	 * @param obj
	 * @param fieldName
	 * @param errorStore
	 * @return
	 */
	public boolean checkRequiredInteger(Integer obj, String fieldName, ErrorInfoStore errorStore) {
		if (obj == null) {
			errorStore.addError(SrdmConstants.ERROR_E0011, fieldName, "", "This field is required.");
			logger.warn( "This field is required. " + fieldName +": null" );
			return false;
		}

		return true;
	}

	/**
	 * Long数値の必須チェック
	 * 主として必須項目の数値に使用することを想定しています。
	 * 空であればエラー情報を引数オブジェクトに追加します
	 * @param obj
	 * @param fieldName
	 * @param errorStore
	 * @return
	 */
	public boolean checkRequiredLong(Long obj, String fieldName, ErrorInfoStore errorStore) {
		if (obj == null) {
			errorStore.addError(SrdmConstants.ERROR_E0011, fieldName, "", "This field is required.");
			logger.warn( "This field is required. " + fieldName +": null" );
			return false;
		}

		return true;
	}

	/**
	 * 文字列の必須チェックを行います。
	 * 空であればエラー情報を引数オブジェクトに追加します。
	 * @param obj
	 * @param fieldName
	 * @param errorStore
	 * @return
	 */
	public boolean checkRequiredString(String obj, String fieldName, ErrorInfoStore errorStore) {
		if (StringUtils.hasText(obj) == false) {
			errorStore.addError(SrdmConstants.ERROR_E0011, fieldName, obj, "This field is required.");
			logger.warn( "This field is required. " + fieldName +":"+ obj );
			return false;
		}

		return true;
	}

	/**
	 * Booleanの必須チェックを行います。
	 * 空であればエラー情報を引数オブジェクトに追加します。
	 * @param obj
	 * @param fieldName
	 * @param errorStore
	 * @return
	 */
	public boolean checkRequiredBoolean(Object obj, String fieldName, ErrorInfoStore errorStore) {
		if ( obj == null ) {
			errorStore.addError(SrdmConstants.ERROR_E0011, fieldName, "", "This field is null.");
			logger.warn( "This field is null. " + fieldName );
			return false;
		}

		String str = obj.toString();
		if( !str.equalsIgnoreCase("true") && !str.equalsIgnoreCase("false") ){
			errorStore.addError(SrdmConstants.ERROR_E0011, fieldName, str, "This field is required.");
			logger.warn( "This field is required. " + fieldName +":"+ str );
			return false;
		}

		return true;
	}

	/**
	 * リストの必須チェックを行います。
	 * 空であればエラー情報を引数オブジェクトに追加します。
	 * @param obj
	 * @param fieldName
	 * @param errorStore
	 * @return
	 */
	public boolean checkRequiredList(List<?> obj, String fieldName, ErrorInfoStore errorStore) {
		if (obj == null || obj.isEmpty()) {
			errorStore.addError(SrdmConstants.ERROR_E0011, fieldName, "", "This field is required.");
			logger.warn( "This field is required. " + fieldName );
			return false;
		}

		return true;
	}

	/**
	 * 数値範囲チェック
	 *
	 * @param value 値
	 * @param min 最少桁
	 * @param max 最大桁
	 * @param fieldName フィールド名
	 * @param errorStore InputCheckErrorStore
	 * @return エラーの場合は、falseを返します。
	 */
	public boolean checkIsInRangeLong(String value, long min, long max, String fieldName, ErrorInfoStore errorStore) {

		if (GenericValidator.isLong(value)) {

			if (!GenericValidator.isInRange(Long.parseLong(value), min, max)) {

				// 入力値の範囲が正しくありません。
				errorStore.addError(
						SrdmConstants.ERROR_E0049,
						fieldName,
						String.valueOf(value),
						"Incorrect range of input values.");
				return (false);
			}
		}
		return (true);
	}
}
