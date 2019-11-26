package srdm.cloud.commonService.validation;

import java.io.UnsupportedEncodingException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import srdm.common.constant.SrdmConstants;

public class EmailAddrListValidator implements ConstraintValidator<EmailAddrList, String> {

	@Override
	public void initialize(EmailAddrList constraintAnnotation) {

	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {

		/**
		 * 複数のアドレスを入力する場合は、セミコロン「;」で区切る。
		 *
		 * 制限：
		 * ・1000文字以下
		 * ・半角英数字および半角記号以外は禁止
		 * ・複数のアドレスを設定可
		 * ・一つの[@]または一つ以上の[.](ピリオド)がそれぞれのアドレスで必要です。
		 * ・各アドレスにおいて少なくとも2文字(文字のみ)が最後の [.](ピリオド) の後に必要
		 * ・以下の特別文字は使用禁止
		 * 		",", "\", " "(Space), "<", ">", ";", "[", "]"
		 * ・空欄（入力なし）のみは不可。
		 * 		※ただし空欄のチェックは呼出元で判定すること。(To/Cc/Bccによって制限が異なるため)
		 */
		if (value == null) {
			// 入力なし
			return true; // 必要であれば呼出元でチェックすること
		}
		if (value.length() > 1000) {
			// 1000文字超え
			return false;
		}
		if (value.trim().length() == 0) {
			// 入力なし or スペースのみ
			return true; // 必要であれば呼出元でチェックすること
		}
		try {
			if (value.length() != (value.getBytes(SrdmConstants.SYSTEM_CHARSET_NAME).length)) {
				// 半角英数以外
				return false;
			}
		} catch (UnsupportedEncodingException e) {
			// 半角英数以外？
			return false;
		}

		final String[] invalid = {
				",", "\\", " ", "<", ">", "[", "]"
		};
		for (int i = 0; i < value.length(); i ++) {
			String c = value.substring(i, i + 1);
			for (int j = 0; j < invalid.length; j ++) {
				if (c.equals(invalid[j]) == true) {
					return false;
				}
			}
			char ch = value.charAt(i);
			if (ch < 0x20 || ch > 0x7e) {
				return false;
			}
		}

		String wrk = value.replace(";", "");
		if (wrk.trim().length() == 0) {
			return false;
		}
		String[] addresses = value.split(";");

		for (String addr: addresses) {
			// userとhost.domain に分割
			String tmp[] = addr.split("@", -1);
			if (tmp.length != 2) {
				// "@"が1個ではない
				return false;
			} else if (tmp[0].trim().length() == 0) {
				// userがない
				return false;
			} else {
				if (tmp[1].contains(".") == false) {
					// domainがない
					return false;
				}
				String tmp2[] = tmp[1].split("\\.", -1);
				if (tmp2.length == 0) {
					// hostまたはdomainが長さ0
					return false;
				}
				int last = tmp2.length - 1;
				if (tmp2[last].length() < 2) {
					// gTLDが2文字未満
					return false;
				}
			}
		}
		return true;
	}

}
