package srdm.cloud.commonService.repositoryNoDB;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import srdm.cloud.commonService.domain.model.GetListReq;
import srdm.cloud.commonService.domain.model.SimpleGroup;
import srdm.cloud.commonService.domain.repository.group.GroupRepository;
import srdm.cloud.commonService.util.OxmProcessor;
import srdm.common.exception.SrdmDataAccessException;
import srdm.common.exception.SrdmDataNotFoundException;

@Repository
public class TestGroupRepositoryImpl implements GroupRepository {

	@Autowired
	OxmProcessor oxmProcessor;

	@Override
	public List<SimpleGroup> findAllByParentGroupIdWithPagable(GetListReq getListReq) throws SrdmDataAccessException {

		List<SimpleGroup> list = new ArrayList<SimpleGroup>();
		SimpleGroup group = new SimpleGroup();

		group.setGroupId("100100");
		group.setGroupName("TestGroup");
		group.setParentGroupId("0");

		list.add(group);
		return list;
	}

	@Override
	public long count(String groupId) throws SrdmDataNotFoundException, SrdmDataAccessException {

		long count = 1l;
		return count;
	}

	@Override
	public SimpleGroup findOne(String groupId) throws SrdmDataNotFoundException, SrdmDataAccessException {

		SimpleGroup group = new SimpleGroup();
		group.setGroupId("100100");
		group.setGroupName("TestGroup");
		group.setParentGroupId("0");
		return group;
	}

	@Override
	public boolean isUnderGroup(String srcGroupId, String targetGroupId) throws SrdmDataAccessException {
		boolean bRet = true;
		return bRet;
	}

}
