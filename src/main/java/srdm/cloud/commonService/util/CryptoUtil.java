package srdm.cloud.commonService.util;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import srdm.common.constant.SrdmConstants;

@Component
public class CryptoUtil {

	private static final Logger logger = LoggerFactory.getLogger(CryptoUtil.class);

	private static final String CIPHER_ALG_NAME = "AES";
	private static final String SECRETKEY_ALG_NAME = "AES";

	/**
	 * 文字列を暗号化します。
	 *
	 * @param value 値
	 * @return 暗号化した文字列
	 * @throws GeneralSecurityException
	 */
	public String encrypt(String value) throws GeneralSecurityException {

		try {

			SecretKeySpec keySpec = new SecretKeySpec((getKey().substring(4, 16) + getKey().substring(0, 4)).getBytes(SrdmConstants.SYSTEM_CHARSET_NAME), SECRETKEY_ALG_NAME);
			Cipher cipher = Cipher.getInstance(CIPHER_ALG_NAME);
			cipher.init(Cipher.ENCRYPT_MODE, keySpec);

			return (DatatypeConverter.printHexBinary(cipher.doFinal(value.getBytes(SrdmConstants.SYSTEM_CHARSET_NAME))));
		} catch (UnsupportedEncodingException e) {

			// Encodingは、UTF-8固定で指定を行っている為、ここでExceptionは、発生しない。
			logger.warn("[CryptUtil]: encrypt failed.",e);
			return "";
		}
	}

	/**
	 * 文字列を復号化します。
	 *
	 * @param value 値
	 * @return 復号化した文字列
	 * @throws GeneralSecurityException
	 */
	public String decrypt(String value) throws GeneralSecurityException {

		if (value == null) {
			return null;
		}

		try {

			SecretKeySpec keySpec = new SecretKeySpec((getKey().substring(4, 16) + getKey().substring(0, 4)).getBytes(SrdmConstants.SYSTEM_CHARSET_NAME), SECRETKEY_ALG_NAME);
			Cipher cipher = Cipher.getInstance(CIPHER_ALG_NAME);
			cipher.init(Cipher.DECRYPT_MODE, keySpec);

			return (new String(cipher.doFinal(DatatypeConverter.parseHexBinary(value)), SrdmConstants.SYSTEM_CHARSET_NAME));
		} catch (UnsupportedEncodingException e) {

			// Encodingは、UTF-8固定で指定を行っている為、ここでExceptionは、発生しない。
			logger.warn("[CryptUtil]: decrypt failed. ",e);
			return "";
		}
	}

	private String getKey() {

		return ("4144fc1919d3ed4a");
	}
}
