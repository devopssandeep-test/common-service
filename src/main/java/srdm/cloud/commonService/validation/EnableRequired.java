package srdm.cloud.commonService.validation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * 対応するEnableがtrueの場合の必須チェック
 *
 */
@Documented
@Constraint(validatedBy = {EnableRequiredValidator.class})
@Retention(RUNTIME)
@Target({ TYPE, ANNOTATION_TYPE })
@Repeatable(EnableRequiredHolderAnnotation.class)
public @interface EnableRequired {

	String message() default "E0014";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	// Filed name
	String field();

	@Documented
	@Retention(RUNTIME)
	@Target({ FIELD, METHOD, CONSTRUCTOR, ANNOTATION_TYPE, PARAMETER })
	public @interface List {
		EnableRequired[] value();
	}
}
