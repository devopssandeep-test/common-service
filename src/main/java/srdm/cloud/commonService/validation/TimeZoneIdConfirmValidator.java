package srdm.cloud.commonService.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class TimeZoneIdConfirmValidator implements ConstraintValidator<TimeZoneIdConfirm, Object> {

	String message;

	@Override
	public void initialize(TimeZoneIdConfirm constraintAnnotation) {

		message = constraintAnnotation.message();
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {

		boolean bRet;
		BeanWrapper beanWrapper = new BeanWrapperImpl(value);
		String timeZoneId = (String) beanWrapper.getPropertyValue("timeZoneId");
		String timeZoneSpecifingType = (String) beanWrapper.getPropertyValue("timeZoneSpecifingType");

		if(timeZoneSpecifingType != null && timeZoneSpecifingType.trim().length() > 0) {
			if(timeZoneSpecifingType.equals("auto") == true ) {
				// timeZoneSpecifingType が autoの場合、timeZoneIdは、null or ""ならOK
				if(timeZoneId == null || timeZoneId.equals("") == true) {
						bRet = true;
				} else {
					context.disableDefaultConstraintViolation();
					context.buildConstraintViolationWithTemplate(message)
							.addPropertyNode("timeZoneId").addConstraintViolation();
					bRet = false;
				}
			} else if(timeZoneSpecifingType.equals("manual") == true ) {
				// timeZoneSpecifingType が manualの場合、timeZoneIdは、null or ""以外ならOK
				if(timeZoneId != null && timeZoneId.equals("") == false) {
					bRet = true;
				} else {
					context.disableDefaultConstraintViolation();
					context.buildConstraintViolationWithTemplate(message)
							.addPropertyNode("timeZoneId").addConstraintViolation();
					bRet = false;
				}
			} else {
				// timeZoneSpecifingTypeは、事前に@Patternで取りえる値をチェックしている為、ここにくることはないのでtrueを返す。
				bRet = true;
			}
		} else {
			// timeZoneSpecifingTypeは、事前に@NotBlankでチェック済みのため、ここにくることはないのでtrueを返す。
			bRet = true;
		}
		return bRet;
	}

}
