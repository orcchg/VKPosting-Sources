package com.orcchg.vikstra.data.source.local.model.mapper;

import com.orcchg.vikstra.data.source.local.model.GroupReportDBO;
import com.orcchg.vikstra.data.source.local.model.populator.GroupReportToDboPopulator;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.mapper.DuplexMapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class GroupReportToDboMapper implements DuplexMapper<GroupReport, GroupReportDBO> {

    private final GroupReportToDboPopulator groupReportToDboPopulator;

    @Inject
    public GroupReportToDboMapper(GroupReportToDboPopulator groupReportToDboPopulator) {
        this.groupReportToDboPopulator = groupReportToDboPopulator;
    }

    /* Direct mapping */
    // ------------------------------------------
    @Override
    public GroupReportDBO map(GroupReport object) {
        GroupReportDBO dbo = new GroupReportDBO();
        groupReportToDboPopulator.populate(object, dbo);
        return dbo;
    }

    @Override
    public List<GroupReportDBO> map(List<GroupReport> list) {
        List<GroupReportDBO> mapped = new ArrayList<>();
        for (GroupReport item : list) {
            mapped.add(map(item));
        }
        return mapped;
    }

    /* Backward mapping */
    // ------------------------------------------
    @Override
    public GroupReport mapBack(GroupReportDBO object) {
        return GroupReport.builder()
                .setId(object.id)
                .setErrorCode(object.errorCode)
                .setGroupId(object.groupId)
                .setTimestamp(object.timestamp)
                .setWallPostId(object.wallPostId)
                .build();
    }
}
