package srdm.cloud.commonService.domain.service.group;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.GetListRes;
import srdm.cloud.commonService.domain.model.SimpleGroup;
import srdm.cloud.commonService.domain.repository.group.GroupRepository;
import srdm.cloud.commonService.domain.repository.session.SrdmSessionRepository;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

@Service
public class GroupServiceImple implements GroupService {

	private static final Logger logger = LoggerFactory.getLogger(GroupServiceImple.class);

	private static final String EXPAND_VALUE_CHILDREN_ONLY = "childrenOnly";

	@Autowired
	SrdmSessionRepository srdmSessionRepository;

	@Autowired
	GroupRepository groupRepository;

	@Override
	public GetListRes getGroups(String sessionId, GetListReq getListReq)
			throws SrdmDataAccessException, SrdmDataNotFoundException {

		GetListRes getListRes = new GetListRes();

		// 指定グループがログインアカウントの管理対象グループ配下にあるかをチェック
		String homeGroupId = srdmSessionRepository.getGroupId(sessionId);
		if(homeGroupId.isEmpty()) {
			// sessionIdチェックを行っているので基本的にここでのエラーはない。
			logger.warn("login domainId not found. sessionId[{}]", sessionId);
			throw new SrdmDataNotFoundException("sessionId", "", "Unable to get the account list.");
		}
		boolean isUnderGroup = groupRepository.isUnderGroup(homeGroupId, getListReq.getKeyMap().get("groupId"));
		if(isUnderGroup == false) {
			logger.warn("group is not included. groupId[{}]", getListReq.getKeyMap().get("groupId"));
			throw new SrdmDataNotFoundException("groupId", "", "Unable to get the group list.");
		}

		// 指定グループのデータを取得
		String expand = getListReq.getKeyMap().get("expand");
		List<SimpleGroup> groupList;
		if(expand.equals(EXPAND_VALUE_CHILDREN_ONLY) == true) {

			// 子グループを取得
			groupList = groupRepository.findAllByParentGroupIdWithPagable(getListReq);
			getListRes.setList(groupList);
			getListRes.setResultCount(groupList.size());

			// Total件数取得
			long total;
			try {
				total = groupRepository.count(getListReq.getKeyMap().get("groupId"));
				if (total == 0) {
					// 指定ドメイン無し
					logger.warn("Grrrroup not found. groupId[{}]");
					throw new SrdmDataNotFoundException("Group Not found!!.");
				}
			} catch (SrdmDataNotFoundException e) {
				// Total件数 0件
				total = 0;
                throw new SrdmDataNotFoundException("Group Not found!!.");
			}
			getListRes.setTotalCount(total);
		} else {

			// 指定グループを取得
			groupList = new ArrayList<SimpleGroup>();
			
			try {
				SimpleGroup group = groupRepository.findOne(getListReq.getKeyMap().get("groupId"));
				if (group.getGroupId().isEmpty() ) {
					// 指定ドメイン無し
					logger.warn("Group not found. groupId[{}]", getListReq.getKeyMap().get("groupId"));
					throw new SrdmDataNotFoundException("Group Not found!!.");
				} else {
					groupList.add(group);
				}
			} catch (SrdmDataNotFoundException e) {
				logger.warn("group not found. groupId[{}]", getListReq.getKeyMap().get("groupId"));
                throw new SrdmDataNotFoundException("Group Not found!!.");
			}
			getListRes.setList(groupList);
			getListRes.setResultCount(groupList.size());
			getListRes.setTotalCount(groupList.size());
		}

		return getListRes;
	}

}
