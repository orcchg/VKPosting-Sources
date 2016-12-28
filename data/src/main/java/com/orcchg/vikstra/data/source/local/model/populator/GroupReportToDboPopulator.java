package com.orcchg.vikstra.data.source.local.model.populator;

import com.orcchg.vikstra.data.source.local.model.GroupReportDBO;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.mapper.Populator;

import javax.inject.Inject;

public class GroupReportToDboPopulator implements Populator<GroupReport, GroupReportDBO> {

    @Inject
    public GroupReportToDboPopulator() {
    }

    @Override
    public void populate(GroupReport object, GroupReportDBO dbo) {
        dbo.id = object.id();
        dbo.errorCode = object.errorCode();
        dbo.groupId = object.groupId();
        dbo.timestamp = object.timestamp();
        dbo.wallPostId = object.wallPostId();
    }
}
