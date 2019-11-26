package srdm.cloud.commonService.validation;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LanguageValidator implements ConstraintValidator<Language, String> {

	private static final Pattern PatternLanguage = Pattern.compile("^[a-z]{2}$");
	private static final Pattern PatternLanguageCountry = Pattern.compile("^[a-z]{2}\\-[A-Z]{2}$");

	@Override
	public void initialize(Language constraintAnnotation) {

	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {

		boolean bRet;
		if (value != null && value.trim().length() > 0) {
			if (value.contains("-") == true) {
				bRet = PatternLanguageCountry.matcher(value).find();
			} else {
				bRet = PatternLanguage.matcher(value).find();
			}
		} else {
			// null または、サイズ0の場合、OKを返す
			bRet = true;
		}
		return bRet;
	}
}
