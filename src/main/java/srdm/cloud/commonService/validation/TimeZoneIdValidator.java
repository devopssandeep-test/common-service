package srdm.cloud.commonService.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import srdm.common.constant.SrdmConstants;

public class TimeZoneIdValidator implements ConstraintValidator<TimeZoneId, String> {

	@Override
	public void initialize(TimeZoneId constraintAnnotation) {

	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {

		boolean bRet;
		if (value != null && value.trim().length() > 0) {
			bRet = SrdmConstants.getListTimeZoneId().contains(value);
		} else {
			// null または、サイズ0の場合、OKを返す
			bRet = true;
		}
		return bRet;
	}

}
