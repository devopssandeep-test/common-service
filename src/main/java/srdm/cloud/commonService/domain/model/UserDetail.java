package srdm.cloud.commonService.domain.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class UserDetail implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3481165405960840932L;

	private String sessionId;
	private String groupId;
	private String domainId;
	private String accountId;
	private List<String> permissionList;
	private long sessionTimeout;
}
