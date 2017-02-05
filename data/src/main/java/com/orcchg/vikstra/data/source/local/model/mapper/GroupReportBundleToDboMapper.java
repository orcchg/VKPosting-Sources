package com.orcchg.vikstra.data.source.local.model.mapper;

import com.orcchg.vikstra.data.source.local.model.GroupReportBundleDBO;
import com.orcchg.vikstra.data.source.local.model.GroupReportDBO;
import com.orcchg.vikstra.data.source.local.model.populator.GroupReportBundleToDboPopulator;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.model.mapper.DuplexMapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class GroupReportBundleToDboMapper implements DuplexMapper<GroupReportBundle, GroupReportBundleDBO> {

    private final GroupReportToDboMapper groupReportToDboMapper;
    private final GroupReportBundleToDboPopulator groupReportBundleToDboPopulator;

    @Inject
    public GroupReportBundleToDboMapper(GroupReportToDboMapper groupReportToDboMapper,
                                        GroupReportBundleToDboPopulator groupReportBundleToDboPopulator) {
        this.groupReportToDboMapper = groupReportToDboMapper;
        this.groupReportBundleToDboPopulator = groupReportBundleToDboPopulator;
    }

    /* Direct mapping */
    // ------------------------------------------
    @Override
    public GroupReportBundleDBO map(GroupReportBundle object) {
        GroupReportBundleDBO dbo = new GroupReportBundleDBO();
        groupReportBundleToDboPopulator.populate(object, dbo);
        return dbo;
    }

    @Override
    public List<GroupReportBundleDBO> map(List<GroupReportBundle> list) {
        List<GroupReportBundleDBO> mapped = new ArrayList<>();
        for (GroupReportBundle item : list) {
            mapped.add(map(item));
        }
        return mapped;
    }

    /* Backward mapping */
    // ------------------------------------------
    @Override
    public GroupReportBundle mapBack(GroupReportBundleDBO object) {
        List<GroupReport> reports = new ArrayList<>();
        for (GroupReportDBO dbo : object.groupReports) {
            reports.add(groupReportToDboMapper.mapBack(dbo));
        }
        return GroupReportBundle.builder()
                .setId(object.id)
                .setGroupReports(reports)
                .setTimestamp(object.timestamp)
                .build();
    }

    @Override
    public List<GroupReportBundle> mapBack(List<GroupReportBundleDBO> list) {
        List<GroupReportBundle> mapped = new ArrayList<>();
        for (GroupReportBundleDBO item : list) {
            mapped.add(mapBack(item));
        }
        return mapped;
    }
}
