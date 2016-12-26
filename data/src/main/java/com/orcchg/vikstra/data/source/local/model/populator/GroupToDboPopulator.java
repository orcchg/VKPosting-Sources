package com.orcchg.vikstra.data.source.local.model.populator;

import com.orcchg.vikstra.data.source.local.model.GroupDBO;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.mapper.Populator;

import javax.inject.Inject;

public class GroupToDboPopulator implements Populator<Group, GroupDBO> {

    @Inject
    public GroupToDboPopulator() {
    }

    @Override
    public void populate(Group object, GroupDBO dbo) {
        dbo.id = object.id();
        dbo.canPost = object.canPost();
        dbo.membersCount = object.membersCount();
        dbo.name = object.name();
    }
}
