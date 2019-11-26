package srdm.cloud.commonService.validation;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EmailAddressValidator implements ConstraintValidator<EmailAddress, String> {

	private static final Pattern PatternCommonANS = Pattern.compile("^[\\p{Print}]+$"); // 半角英数記号(共通)

	@Override
	public void initialize(EmailAddress constraintAnnotation) {

	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {

		boolean bRet = false;
		if (value != null && value.trim().length() > 0) {
			int len = value.length();
			if (len >= 1 && len <= 256) {
				bRet = PatternCommonANS.matcher(value).find();
				if (bRet == true) {
					if (count(value, "@") != 1) { // @ は1つ
						bRet = false;
					} else if (count(value, ".") == 0) { // . は1つ以上
						bRet = false;
					}
				}
			}
		} else {
			// null or length=0は、OKとする
			bRet = true;
		}
		return bRet;
	}

	private int count(String buf, String str) {
		int n = 0;
		if (buf != null && str != null) {
			int pos = 0;
			int len = buf.length();
			while (pos < len) {
				int idx = buf.indexOf(str, pos);
				if (idx < 0) {
					break;
				}
				n ++;
				pos += idx + 1;
			}
		}
		return n;
	}
}
