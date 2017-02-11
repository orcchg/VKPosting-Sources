package com.orcchg.vikstra.data.source.local.model.mapper;

import com.orcchg.vikstra.data.source.local.model.GroupReportDBO;
import com.orcchg.vikstra.data.source.local.model.populator.GroupReportToDboPopulator;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.mapper.DuplexMapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class GroupReportToDboMapper implements DuplexMapper<GroupReport, GroupReportDBO> {

    private final GroupToDboMapper groupToDboMapper;
    private final GroupReportToDboPopulator groupReportToDboPopulator;

    @Inject
    public GroupReportToDboMapper(GroupToDboMapper groupToDboMapper,
            GroupReportToDboPopulator groupReportToDboPopulator) {
        this.groupToDboMapper = groupToDboMapper;
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
        GroupReport report = GroupReport.builder()
                .setId(object.id)
                .setCancelled(object.cancelled)
                .setErrorCode(object.errorCode)
                .setGroup(groupToDboMapper.mapBack(object.group))
                .setTimestamp(object.timestamp)
                .setWallPostId(object.wallPostId)
                .build();
        report.setReverted(object.reverted);
        return report;
    }

    @Override
    public List<GroupReport> mapBack(List<GroupReportDBO> list) {
        List<GroupReport> mapped = new ArrayList<>();
        for (GroupReportDBO item : list) {
            mapped.add(mapBack(item));
        }
        return mapped;
    }
}
