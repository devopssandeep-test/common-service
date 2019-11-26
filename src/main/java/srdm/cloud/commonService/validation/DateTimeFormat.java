package srdm.cloud.commonService.validation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.Pattern;

/**
 * 日付時刻フォーマットチェック
 *
 */
@Documented
@Constraint(validatedBy = {})
@Retention(RUNTIME)
@Target({ FIELD, METHOD, CONSTRUCTOR, ANNOTATION_TYPE, PARAMETER })
@ReportAsSingleViolation
// 特定のパターンであることをチェック(空文字もOKとする為、追加しておく）
@Pattern(regexp="MM/dd/yyyy HH:mm:ss|yyyy/MM/dd HH:mm:ss|dd/MM/yyyy HH:mm:ss|")
public @interface DateTimeFormat {

	String message() default "E0014";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	@Documented
	@Retention(RUNTIME)
	@Target({ FIELD, METHOD, CONSTRUCTOR, ANNOTATION_TYPE, PARAMETER })
	public @interface List {
		DateTimeFormat[] value();
	}
}
