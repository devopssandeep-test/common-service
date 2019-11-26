package srdm.cloud.commonService.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class EnableRequiredValidator implements ConstraintValidator<EnableRequired, Object> {

	String message;
	String field;
	String enableField;

	@Override
	public void initialize(EnableRequired constraintAnnotation) {

		field = constraintAnnotation.field();
		enableField = constraintAnnotation.field() + "Enable";
		message = constraintAnnotation.message();
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {

		boolean bRet = false;
		BeanWrapper beanWrapper = new BeanWrapperImpl(value);
		String fieldValue = (String) beanWrapper.getPropertyValue(field);
		String isEnable = (String) beanWrapper.getPropertyValue(enableField);

		if(Boolean.parseBoolean(isEnable) == true) {
			if(fieldValue != null && fieldValue.isEmpty() == false) {
				bRet = true;
			}
		} else {
			// enableFieldの値が、falseの場合は、チェックしない
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
