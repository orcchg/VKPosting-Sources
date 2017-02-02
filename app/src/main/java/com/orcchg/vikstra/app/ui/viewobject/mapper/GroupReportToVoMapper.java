package com.orcchg.vikstra.app.ui.viewobject.mapper;

import com.orcchg.vikstra.app.ui.viewobject.ReportListItemVO;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.mapper.Mapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class GroupReportToVoMapper implements Mapper<GroupReport, ReportListItemVO> {

    @Inject
    public GroupReportToVoMapper() {
    }

    @Override
    public ReportListItemVO map(GroupReport object) {
        Group group = object.group();
        return ReportListItemVO.builder()
                .setGroupId(group.id())
                .setGroupName(group.name())
                .setMembersCount(group.membersCount())
                .setReportStatus(object.status())
                .build();
    }

    @Override
    public List<ReportListItemVO> map(List<GroupReport> list) {
        List<ReportListItemVO> mapped = new ArrayList<>();
        for (GroupReport item : list) {
            mapped.add(map(item));
        }
        return mapped;
    }
}
