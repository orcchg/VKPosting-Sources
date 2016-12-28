package com.orcchg.vikstra.data.source.local.model.populator;

import com.orcchg.vikstra.data.source.local.model.GroupReportBundleDBO;
import com.orcchg.vikstra.data.source.local.model.mapper.GroupReportToDboMapper;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.model.mapper.Populator;

import javax.inject.Inject;

import io.realm.RealmList;

public class GroupReportBundleToDboPopulator implements Populator<GroupReportBundle, GroupReportBundleDBO> {

    private final GroupReportToDboMapper groupReportToDboMapper;

    @Inject
    public GroupReportBundleToDboPopulator(GroupReportToDboMapper groupReportToDboMapper) {
        this.groupReportToDboMapper = groupReportToDboMapper;
    }

    @Override
    public void populate(GroupReportBundle object, GroupReportBundleDBO dbo) {
        dbo.id = object.id();
        dbo.groupReports = new RealmList<>();
        dbo.timestamp = object.timestamp();
        for (GroupReport report : object.groupReports()) {
            dbo.groupReports.add(groupReportToDboMapper.map(report));
        }
    }
}
