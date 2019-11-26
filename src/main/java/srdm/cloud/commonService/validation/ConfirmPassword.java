package srdm.cloud.commonService.validation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * パスワード編集時のログインパスワードチェック
 *
 */
@Documented
@Constraint(validatedBy = {ConfirmPasswordValidator.class})
@Retention(RUNTIME)
@Target({ TYPE, ANNOTATION_TYPE })
public @interface ConfirmPassword {

	String message() default "E0014";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	// Filed name
	String field();

	@Documented
	@Retention(RUNTIME)
	@Target({ FIELD, METHOD, CONSTRUCTOR, ANNOTATION_TYPE, PARAMETER })
	public @interface List {
		ConfirmPassword[] value();
	}
}
