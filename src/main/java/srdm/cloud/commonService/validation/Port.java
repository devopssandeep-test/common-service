package srdm.cloud.commonService.validation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;

/**
 * ポート番号チェック
 *
 */
@Documented
@Constraint(validatedBy = {PortValidator.class})
@Retention(RUNTIME)
@Target({ FIELD, METHOD, CONSTRUCTOR, ANNOTATION_TYPE, PARAMETER })
@ReportAsSingleViolation
public @interface Port {

	String message() default "E0014";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	@Documented
	@Retention(RUNTIME)
	@Target({ FIELD, METHOD, CONSTRUCTOR, ANNOTATION_TYPE, PARAMETER })
	public @interface List {
		Port[] value();
	}
}
