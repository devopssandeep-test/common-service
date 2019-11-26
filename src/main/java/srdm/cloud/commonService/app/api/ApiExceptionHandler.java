package srdm.cloud.commonService.app.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.core.JsonParseException;

import srdm.common.bean.JsonBaseResBean;
import srdm.common.constant.SrdmConstants;
import srdm.common.exception.SrdmBaseException;

@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	/**
	 * 例外ハンドル共通処理
	 */
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
			HttpStatus status, WebRequest request) {

		JsonBaseResBean errorBean = new JsonBaseResBean();

		// 想定外の例外を検知した場合は、例外クラスのクラス名をメッセージに出力する
		String message = SrdmConstants.ERROR_MESSAGE_MAP.get(SrdmConstants.ERROR_E9999)
				+ "[" + ex.getClass().getSimpleName().substring(0, ex.getClass().getSimpleName().indexOf("Exception")) + "]";
		errorBean.addError(SrdmConstants.ERROR_E9999, "", "", message);
		return super.handleExceptionInternal(ex, errorBean, headers, HttpStatus.OK, request);
	}

	/**
	 * FrameworkでのQueryStringから必須パラメータが取得できなかった場合の例外
	 * エラーコードは、固定でE0011を返す。
	 * SRDM2.4.0の時点でQueryStringからパラメータを取得しているのは、以下のAPI
	 * downloadLogFile, downloadExportData
	 */
	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		JsonBaseResBean errorBean = new JsonBaseResBean();

		errorBean.addError(SrdmConstants.ERROR_E0011, ex.getParameterName(), "", SrdmConstants.ERROR_MESSAGE_MAP.get(SrdmConstants.ERROR_E0011));
		return super.handleExceptionInternal(ex, errorBean, headers, HttpStatus.OK, request);
	}

	/**
	 * FrameworkでBody部のRequest Parameter読み取り時の例外
	 * JSON Parseエラーは、このメソッドでハンドリングする。
	 * 本メソッドで返すエラーコードは、E0004に統一。但し、JsonPerseExceptionとそれ以外でメッセージを分ける。
	 * JsonPerseException：E0004のエラーメッセージ
	 * 以外：E0005のメッセージ
	 */
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		JsonBaseResBean errorBean = new JsonBaseResBean();
		Throwable getCause = ex.getCause();
		if(getCause != null) {
			if(getCause instanceof JsonParseException ) {
				errorBean.addError(SrdmConstants.ERROR_E0004, "", "", SrdmConstants.ERROR_MESSAGE_MAP.get(SrdmConstants.ERROR_E0004));
			} else {
				// JsonParseException以外は、例外クラスのクラス名をエラーメッセージに出力する
				String message = SrdmConstants.ERROR_MESSAGE_MAP.get(SrdmConstants.ERROR_E0005)
						+ "[" + getCause.getClass().getSimpleName().substring(0, getCause.getClass().getSimpleName().indexOf("Exception")) + "]";
				errorBean.addError(SrdmConstants.ERROR_E0004, "", "", message);
			}
		} else {
			errorBean.addError(SrdmConstants.ERROR_E0004, "", "", SrdmConstants.ERROR_MESSAGE_MAP.get(SrdmConstants.ERROR_E0005));
		}
		return super.handleExceptionInternal(ex, errorBean, headers, HttpStatus.OK, request);
	}

	/**
	 * FrameworkでのBody部のRequest Parameterに対するValidation チェックエラー時の例外
	 * Bean Validationの"message"属性に指定された値をエラーコードとして用いる。
	 */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		JsonBaseResBean errorBean = new JsonBaseResBean();

		for(FieldError error : ex.getBindingResult().getFieldErrors()) {
			String errorCode;
			String message;
			if(error.getDefaultMessage().contains("/")) {
				errorCode = error.getDefaultMessage().substring(0, error.getDefaultMessage().indexOf("/"));
				message = error.getDefaultMessage().substring(error.getDefaultMessage().indexOf("/") + 1);
			} else {
				errorCode = error.getDefaultMessage();
				if(SrdmConstants.ERROR_MESSAGE_MAP.containsKey(errorCode)) {
					message = SrdmConstants.ERROR_MESSAGE_MAP.get(errorCode);
				} else {
					message = "";
				}
			}
			errorBean.addError(errorCode, error.getField(), "", message);
		}

		return super.handleExceptionInternal(ex, errorBean, headers, HttpStatus.OK, request);
	}

	/**
	 * SRDMアプリケーションの例外
	 * SRDMのアプリケーション内の例外をハンドリングする。
	 * @param ex
	 * @param request
	 * @return
	 */
	@ExceptionHandler
	public ResponseEntity<Object> handleSrdmBaseException(
			SrdmBaseException ex, WebRequest request) {

		JsonBaseResBean errorBean = new JsonBaseResBean();
		ex.getErrorStore().getErrorList().stream()
		.forEach(e -> errorBean.addError(e.getErrorCode(), e.getErrorField(), e.getErrorValue(), e.getErrorMessage()));
		return super.handleExceptionInternal(ex, errorBean, null, HttpStatus.OK, request);
	}

}
