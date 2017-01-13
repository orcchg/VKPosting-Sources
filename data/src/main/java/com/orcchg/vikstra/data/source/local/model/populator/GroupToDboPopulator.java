package com.orcchg.vikstra.data.source.local.model.populator;

import com.orcchg.vikstra.data.source.local.model.GroupDBO;
import com.orcchg.vikstra.data.source.local.model.mapper.KeywordToDboMapper;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.mapper.Populator;

import javax.inject.Inject;

public class GroupToDboPopulator implements Populator<Group, GroupDBO> {

    private final KeywordToDboMapper keywordToDboMapper;

    @Inject
    public GroupToDboPopulator(KeywordToDboMapper keywordToDboMapper) {
        this.keywordToDboMapper = keywordToDboMapper;
    }

    @Override
    public void populate(Group object, GroupDBO dbo) {
        dbo.id = object.id();
        dbo.canPost = object.canPost();
        dbo.keyword = keywordToDboMapper.map(object.keyword());
        dbo.membersCount = object.membersCount();
        dbo.name = object.name();
    }
}
