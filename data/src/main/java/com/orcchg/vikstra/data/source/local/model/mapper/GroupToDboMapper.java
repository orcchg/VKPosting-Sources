package com.orcchg.vikstra.data.source.local.model.mapper;

import com.orcchg.vikstra.data.source.local.model.GroupDBO;
import com.orcchg.vikstra.data.source.local.model.populator.GroupToDboPopulator;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.mapper.DuplexMapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class GroupToDboMapper implements DuplexMapper<Group, GroupDBO> {

    private final KeywordToDboMapper keywordToDboMapper;
    private final GroupToDboPopulator groupToDboPopulator;

    @Inject
    public GroupToDboMapper(KeywordToDboMapper keywordToDboMapper, GroupToDboPopulator groupToDboPopulator) {
        this.keywordToDboMapper = keywordToDboMapper;
        this.groupToDboPopulator = groupToDboPopulator;
    }

    /* Direct mapping */
    // ------------------------------------------
    @Override
    public GroupDBO map(Group object) {
        GroupDBO dbo = new GroupDBO();
        groupToDboPopulator.populate(object, dbo);
        return dbo;
    }

    @Override
    public List<GroupDBO> map(List<Group> list) {
        List<GroupDBO> mapped = new ArrayList<>();
        for (Group item : list) {
            mapped.add(map(item));
        }
        return mapped;
    }

    /* Backward mapping */
    // ------------------------------------------
    @Override
    public Group mapBack(GroupDBO object) {
        return Group.builder()
                .setId(object.id)
                .setCanPost(object.canPost)
                .setKeyword(keywordToDboMapper.mapBack(object.keyword))
                .setMembersCount(object.membersCount)
                .setName(object.name)
                .build();
    }
}
