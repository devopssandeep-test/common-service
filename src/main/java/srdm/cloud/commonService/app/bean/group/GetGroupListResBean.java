package srdm.cloud.commonService.app.bean.group;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import srdm.common.bean.BaseBean;
import srdm.common.bean.JsonBaseResBean;

@Data
@EqualsAndHashCode(callSuper=false)
public class GetGroupListResBean extends JsonBaseResBean {

	/**
	 *
	 */
	private static final long serialVersionUID = -8901106154692935985L;

	@ToString
	@Getter
	@Setter
	private static class Group extends BaseBean {
		/**
		 *
		 */
		private static final long serialVersionUID = -4776450856211223538L;

		private String groupId;
		private String groupName;
		private String parentGroupId;

		protected Group(String groupId, String groupName, String parentGroupId) {
			this.groupId = groupId;
			this.groupName = groupName;
			this.parentGroupId = parentGroupId;
		}
	}

	private long startIndex;
	private long count;
	private long resultCount;
	private long totalCount;
	private String groupId;

	private List<Group> groupList = new ArrayList<Group>();

	public void addGroup(String groupId, String groupName, String parentGroupId) {
		groupList.add(new Group(groupId, groupName, parentGroupId));
	}
}
