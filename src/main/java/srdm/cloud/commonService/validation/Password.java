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
import javax.validation.constraints.Size;

/**
 * パスワードチェック
 *
 */
@Documented
@Constraint(validatedBy = {})
@Retention(RUNTIME)
@Target({ FIELD, METHOD, CONSTRUCTOR, ANNOTATION_TYPE, PARAMETER })
@ReportAsSingleViolation
// 桁数(8～128)、マルチバイト不可
@Size(min=8, max=128, message="E0014")
@Pattern(regexp="^[\\p{ASCII}]+$", message="E0014")
public @interface Password {

	String message() default "E0014";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	@Documented
	@Retention(RUNTIME)
	@Target({ FIELD, METHOD, CONSTRUCTOR, ANNOTATION_TYPE, PARAMETER })
	public @interface List {
		Password[] value();
	}
}
