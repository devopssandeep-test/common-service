package srdm.cloud.commonService.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class ConfirmPasswordValidator implements ConstraintValidator<ConfirmPassword, Object> {

	String message;

	String field;

	@Override
	public void initialize(ConfirmPassword constraintAnnotation) {

		field = constraintAnnotation.field();
		message = constraintAnnotation.message();
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {

		boolean bRet = false;
		BeanWrapper beanWrapper = new BeanWrapperImpl(value);
		String password = (String) beanWrapper.getPropertyValue(field);
		String changePasswordFlag = (String) beanWrapper.getPropertyValue("changePasswordFlag");

		if(Boolean.parseBoolean(changePasswordFlag) == true) {
			if (password != null && password.trim().length() > 0) {
				bRet = true;
			}
		} else {
			// changePasswordFlagの値は、事前にチェック済みのため、true以外は、全てOKとして返す
			bRet = true;
		}

		if(bRet == false) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(message)
					.addPropertyNode(field).addConstraintViolation();
		}
		return bRet;
	}

}
