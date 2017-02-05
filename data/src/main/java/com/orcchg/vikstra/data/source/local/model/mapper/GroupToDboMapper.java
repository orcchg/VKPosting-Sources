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
        Group group = Group.builder()
                .setId(object.id)
                .setCanPost(object.canPost)
                .setKeyword(keywordToDboMapper.mapBack(object.keyword))
                .setLink(object.link)
                .setMembersCount(object.membersCount)
                .setName(object.name)
                .setScreenName(object.screenName)
                .setWebSite(object.webSite)
                .build();
        group.setSelected(object.isSelected);
        return group;
    }

    @Override
    public List<Group> mapBack(List<GroupDBO> list) {
        List<Group> mapped = new ArrayList<>();
        for (GroupDBO item : list) {
            mapped.add(mapBack(item));
        }
        return mapped;
    }
}
