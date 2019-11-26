package srdm.cloud.commonService.validation;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IpAddressValidator implements ConstraintValidator<IpAddress, String> {

	private static final Pattern PatternPublicAddress = Pattern.compile("^[-._A-Za-z0-9]+$");

	@Override
	public void initialize(IpAddress constraintAnnotation) {

	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {

		boolean bRet = false;
		if (value != null && value.trim().length() > 0) {
			int len = value.length();
			if (len >= 1 && len <= 255) {
				if(PatternPublicAddress.matcher(value).find() == true) {
					if (value.contains("..") == false											// ".."がないこと
							&& value.startsWith(".") == false && value.endsWith(".") == false	// "."で開始/終了しないこと
							&& value.startsWith("-") == false && value.endsWith("-") == false)	// "-"で開始/終了しないこと
						{
							bRet = true;
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
