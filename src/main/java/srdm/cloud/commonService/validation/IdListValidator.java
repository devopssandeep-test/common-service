package srdm.cloud.commonService.validation;

import java.util.List;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IdListValidator implements ConstraintValidator<IdList, List<String>> {

	private static final Pattern PatternDomainId = Pattern.compile("^D-[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$");
	private static final Pattern PatternAccountId = Pattern.compile("^A-[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$");
	private static final Pattern PatternRoleId = Pattern.compile("^R-[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$");
	private static final Pattern PatternDefaultId = Pattern.compile("^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$");

	String type;

	@Override
	public void initialize(IdList constraintAnnotation) {

		type = constraintAnnotation.field();
	}

	@Override
	public boolean isValid(List<String> value, ConstraintValidatorContext context) {

		boolean bRet = true;

		if(value == null || value.isEmpty() == true) {
			// null or size0の場合、OKを返す。
			return true;
		}

		Pattern pattern;
		if(type.equalsIgnoreCase("domainid") == true) {
			pattern = PatternDomainId;
		} else if(type.equalsIgnoreCase("accountid") == true) {
			pattern = PatternAccountId;
		} else if(type.equalsIgnoreCase("roleid") == true) {
			pattern = PatternRoleId;
		} else {
			// 存在しないパターンが指定された場合は、デフォルトチのチェック(UUIDフォーマットのチェック)を行う
			pattern = PatternDefaultId;
		}
		for(String id : value) {
			boolean valid;
			valid = pattern.matcher(id).find();
			if(valid == false) {
				bRet = false;
				break;
			}
		}
		return bRet;
	}

}
