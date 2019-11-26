package srdm.cloud.commonService.validation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * 変更パスワードチェック
 *
 */
@Documented
@Constraint(validatedBy = {EditPasswordValidator.class})
@Retention(RUNTIME)
@Target({ TYPE, ANNOTATION_TYPE })
public @interface EditPassword {

	String message() default "E0014";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	// Filed name
	String field();

	@Documented
	@Retention(RUNTIME)
	@Target({ FIELD, METHOD, CONSTRUCTOR, ANNOTATION_TYPE, PARAMETER })
	public @interface List {
		EditPassword[] value();
	}
}
