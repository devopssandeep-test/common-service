package srdm.cloud.commonService.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.stereotype.Component;

import srdm.common.constant.SrdmConstants;

/**
 * パスワードエンコード用クラス
 * 【注意】
 * パスワードのエンコード処理が必要な箇所は、以下の通り。
 * ・commonServiceのlogin、createAccount、editAccount
 * ・fdManagerの"getOperationLog"、"requestOperationLog"、"deleteOperationLog"、"forceDeleteFile"
 * ・controlPanelの初期パスワード生成
 * 変更する場合は、これら全てのソースを変更すること。
 *
 */
@Component
public class SrdmPasswordEncoder {

	/**
	 * Javaの標準APIを使用して「ハッシュ」・「ソルト」・「ストレッチング」を用いたパスワードの暗号化。
	 * 信頼できるアルゴリズムとして"PBKDF2WithHmacSHA256"を使用。
	 */

	/** パスワードを安全にするためのアルゴリズム */
	private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
	/** ストレッチング回数 */
	private static final int ITERATION_COUNT = 10000;
	/** 生成される鍵の長さ */
	private static final int KEY_LENGTH = 256;

	/**
	 *　平文のパスワードとソルトからパスワードを生成。
	 *
	 * @param password 平文のパスワード
	 * @param salt ソルト
	 * @return 難読化レベル + 難読化パスワード
	 * @throws UnsupportedEncodingException
	 */
	public String getSafetyPassword(String password, String salt) throws UnsupportedEncodingException {

		char[] passCharAry = password.toCharArray();
		byte[] hashedSalt = getHashedSalt(salt);

		PBEKeySpec keySpec = new PBEKeySpec(passCharAry, hashedSalt, ITERATION_COUNT, KEY_LENGTH);

		SecretKeyFactory skf;
		try {
			skf = SecretKeyFactory.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		SecretKey secretKey;
		try {
			secretKey = skf.generateSecret(keySpec);
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
		byte[] passByteAry = secretKey.getEncoded();

		// 生成されたバイト配列を16進数の文字列に変換
		StringBuilder sb = new StringBuilder(64);
		for (byte b : passByteAry) {
			sb.append(String.format("%02x", b & 0xff));
		}

		return SrdmConstants.OBFUSCATION_LEVEL_NO1 + sb.toString();
	}

	/**
	 * ソルトをハッシュ化して返却します
	 * ※ハッシュアルゴリズムはSHA-256を使用
	 *
	 * @param salt ソルト
	 * @return ハッシュ化されたバイト配列のソルト
	 * @throws UnsupportedEncodingException
	 */
	private byte[] getHashedSalt(String salt) throws UnsupportedEncodingException {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		messageDigest.update(salt.getBytes(SrdmConstants.SYSTEM_CHARSET_NAME));
		return messageDigest.digest();
	}
}
