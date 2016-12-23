package com.orcchg.vikstra.data.source.local.model.populator;

import com.orcchg.vikstra.data.source.local.model.GroupBundleDBO;
import com.orcchg.vikstra.data.source.local.model.mapper.GroupToDboMapper;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupBundle;
import com.orcchg.vikstra.domain.model.mapper.Populator;

import javax.inject.Inject;

import io.realm.RealmList;

public class GroupBundleToDboPopulator implements Populator<GroupBundle, GroupBundleDBO> {

    private final GroupToDboMapper groupToDboMapper;

    @Inject
    public GroupBundleToDboPopulator(GroupToDboMapper groupToDboMapper) {
        this.groupToDboMapper = groupToDboMapper;
    }

    @Override
    public void populate(GroupBundle object, GroupBundleDBO dbo) {
        dbo.id = object.id();
        dbo.groups = new RealmList<>();
        dbo.keywordBundleId = object.keywordBundleId();
        dbo.timestamp = object.timestamp();
        dbo.title = object.title();
        for (Group group : object.groups()) {
            dbo.groups.add(groupToDboMapper.map(group));
        }
    }
}
