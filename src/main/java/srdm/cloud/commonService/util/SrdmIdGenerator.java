package srdm.cloud.commonService.util;

import java.util.UUID;

import org.springframework.stereotype.Component;

import srdm.common.constant.SrdmConstants;

@Component
public class SrdmIdGenerator {

	/**
	 * ドメインID生成
	 * @return
	 */
	public String generateDomainId() {
		return SrdmConstants.ID_PREFIX_DOMAIN + UUID.randomUUID().toString();
	}

	/**
	 * ロールID生成
	 * @return
	 */
	public String generateRoleId() {
		return SrdmConstants.ID_PREFIX_ROLE + UUID.randomUUID().toString();
	}

	/**
	 * アカウントID生成
	 * @return
	 */
	public String generateAccountId() {
		return SrdmConstants.ID_PREFIX_ACCOUNT + UUID.randomUUID().toString();
	}
}
