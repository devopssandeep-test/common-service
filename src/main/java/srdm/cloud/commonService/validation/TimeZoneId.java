package srdm.cloud.commonService.validation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * TimeZone Idチェック
 *
 */
@Documented
@Constraint(validatedBy={TimeZoneIdValidator.class})
@Retention(RUNTIME)
@Target({ FIELD, METHOD, PARAMETER, CONSTRUCTOR, ANNOTATION_TYPE })
public @interface TimeZoneId {

	String message() default "E0014";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	@Documented
	@Retention(RUNTIME)
	@Target({ FIELD, METHOD, CONSTRUCTOR, ANNOTATION_TYPE, PARAMETER })
	public @interface List {
		TimeZoneId[] value();
	}
}
