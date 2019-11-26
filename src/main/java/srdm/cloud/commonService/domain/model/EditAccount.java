package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * アカウント情報編集時のソースデータ用クラス
 * クラス内容を見直し、通常のAccountクラスを継承した（もしくは、内部に持った）形で作成し、
 * changePasswordFlag等追加しないといけないものだけを追加するとか。
 */
@Data
public class EditAccount implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 2304834328985610825L;

	private String accountId;
	private String accountName;
	private boolean changePasswordFlag;
	private String password;
	private String loginAccountPassword;
	private String roleId;
	private String language;
	private String dateTimeFormat;
	private String timeZoneSpecifingType;
	private String timeZoneId;	
	private boolean isPermanentAccount;
	private String homeGroupId;
}
