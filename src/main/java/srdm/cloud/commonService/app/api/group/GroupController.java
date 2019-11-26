package srdm.cloud.commonService.app.api.group;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import srdm.cloud.commonService.app.bean.CommonRequestData;
import srdm.cloud.commonService.app.bean.group.GetGroupListReqBean;
import srdm.cloud.commonService.app.bean.group.GetGroupListResBean;
import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.GetListRes;
import srdm.cloud.commonService.domain.model.SimpleGroup;
import srdm.cloud.commonService.domain.service.group.GroupService;
import srdm.common.exception.SrdmBaseException;

@RestController
@RequestMapping(value="/group")
public class GroupController {

//	private static final Logger logger = LoggerFactory.getLogger(GroupController.class);

	@Autowired
	GroupService groupService;

	@RequestMapping(value="/getGroupList/", method=RequestMethod.POST)
	public GetGroupListResBean getGroupList(
			@Validated @RequestBody GetGroupListReqBean reqBean,
			CommonRequestData commonRequestData) throws SrdmBaseException {

		GetGroupListResBean resBean = new GetGroupListResBean();

		GetListReq getListReq = new GetListReq();
		if(reqBean.getStartIndex() != 0) {
			getListReq.setStartIndex(reqBean.getStartIndex());
		}
		if(reqBean.getCount() != 0) {
			getListReq.setCount(reqBean.getCount());
		}
		getListReq.getKeyMap().put("groupId", reqBean.getGroupId());
		if(reqBean.getExpand() != null && reqBean.getExpand().isEmpty() == false) {
			getListReq.getKeyMap().put("expand", reqBean.getExpand());
		} else {
			// expandの指定が無い場合は、"no"を設定
			getListReq.getKeyMap().put("expand", "no");
		}

		GetListRes getListRes = groupService.getGroups(commonRequestData.getSessionId(), getListReq);

		resBean.setStartIndex(getListReq.getStartIndex());
		resBean.setCount(getListReq.getCount());
		resBean.setGroupId(reqBean.getGroupId());
		resBean.setTotalCount(getListRes.getTotalCount());
		resBean.setResultCount(getListRes.getList().size());
		getListRes.getList().stream()
		.forEach(group -> resBean.addGroup(
				((SimpleGroup)group).getGroupId(),
				((SimpleGroup)group).getGroupName(),
				((SimpleGroup)group).getParentGroupId()));

		return resBean;
	}
}
