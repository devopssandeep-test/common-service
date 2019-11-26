package srdm.cloud.commonService.validation;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MailServerValidator implements ConstraintValidator<MailServer, String> {

	private static final Pattern PatternCommonANS = Pattern.compile("^[\\p{Print}]+$"); // 半角英数記号(共通)
	private static final Pattern PatternMailServerInvalidChars = Pattern.compile("[\\\\/:*?\"<>|\\[\\]+=;,@]");

	@Override
	public void initialize(MailServer constraintAnnotation) {

	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {

		boolean bRet = false;
		if (value != null && value.length() > 0) {
			if(value.trim().length() > 0) {
				int len = value.length();
				if (len >= 1 && len <= 255) {
					bRet = PatternCommonANS.matcher(value).find();
					if (bRet == true) {
						if (PatternMailServerInvalidChars.matcher(value).find() == true) {
							bRet = false;
						}
					}
				}
			}
		} else {
			// null or length=0は、OKとする
			bRet = true;
		}
		return bRet;
	}

}
