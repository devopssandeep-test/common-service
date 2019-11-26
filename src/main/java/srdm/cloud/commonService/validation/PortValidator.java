package srdm.cloud.commonService.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.validator.GenericValidator;

public class PortValidator implements ConstraintValidator<Port, String> {

	private static long MIN_VALUE = 1;
	private static long MAX_VALUE = 65535;

	@Override
	public void initialize(Port constraintAnnotation) {

	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {

		boolean bRet = false;
		if (value != null && value.trim().length() > 0) {
			if (GenericValidator.isLong(value)) {
				if (GenericValidator.isInRange(Long.parseLong(value), MIN_VALUE, MAX_VALUE)) {
					bRet = true;
				}
			}
		} else {
			// null or length=0は、OKとする
			bRet = true;
		}
		return bRet;
	}

}
