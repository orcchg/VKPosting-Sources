package com.orcchg.vikstra.data.source.local.model.mapper;

import com.orcchg.vikstra.data.source.local.model.GroupBundleDBO;
import com.orcchg.vikstra.data.source.local.model.GroupDBO;
import com.orcchg.vikstra.data.source.local.model.populator.GroupBundleToDboPopulator;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupBundle;
import com.orcchg.vikstra.domain.model.mapper.DuplexMapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class GroupBundleToDboMapper implements DuplexMapper<GroupBundle, GroupBundleDBO> {

    private final GroupToDboMapper groupToDboMapper;
    private final GroupBundleToDboPopulator groupBundleToDboPopulator;

    @Inject
    public GroupBundleToDboMapper(GroupToDboMapper groupToDboMapper,
                                  GroupBundleToDboPopulator groupBundleToDboPopulator) {
        this.groupToDboMapper = groupToDboMapper;
        this.groupBundleToDboPopulator = groupBundleToDboPopulator;
    }

    /* Direct mapping */
    // ------------------------------------------
    @Override
    public GroupBundleDBO map(GroupBundle object) {
        GroupBundleDBO dbo = new GroupBundleDBO();
        groupBundleToDboPopulator.populate(object, dbo);
        return dbo;
    }

    @Override
    public List<GroupBundleDBO> map(List<GroupBundle> list) {
        List<GroupBundleDBO> mapped = new ArrayList<>();
        for (GroupBundle item : list) {
            mapped.add(map(item));
        }
        return mapped;
    }

    /* Backward mapping */
    // ------------------------------------------
    @Override
    public GroupBundle mapBack(GroupBundleDBO object) {
        List<Group> groups = new ArrayList<>();
        for (GroupDBO dbo : object.groups) {
            groups.add(groupToDboMapper.mapBack(dbo));
        }
        return GroupBundle.builder()
                .setId(object.id)
                .setGroups(groups)
                .setKeywordBundleId(object.keywordBundleId)
                .setTimestamp(object.timestamp)
                .setTitle(object.title)
                .build();
    }

    @Override
    public List<GroupBundle> mapBack(List<GroupBundleDBO> list) {
        List<GroupBundle> mapped = new ArrayList<>();
        for (GroupBundleDBO item : list) {
            mapped.add(mapBack(item));
        }
        return mapped;
    }
}
