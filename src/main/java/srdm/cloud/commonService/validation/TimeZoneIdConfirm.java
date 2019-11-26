package srdm.cloud.commonService.validation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * TimeZone Spesifing TypeとTimeZone Idの相関チェック
 * @author LX12040003
 *
 */
@Documented
@Constraint(validatedBy = {TimeZoneIdConfirmValidator.class})
@Retention(RUNTIME)
@Target({ TYPE, TYPE_PARAMETER })
public @interface TimeZoneIdConfirm {

	String message() default "E0014";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};

	@Documented
	@Retention(RUNTIME)
	@Target({ FIELD, METHOD, CONSTRUCTOR, ANNOTATION_TYPE, PARAMETER })
	public @interface List {
		TimeZoneIdConfirm[] value();
	}
}
