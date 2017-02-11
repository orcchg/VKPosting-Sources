package com.orcchg.vikstra.app.ui.viewobject.mapper;

import com.orcchg.vikstra.app.ui.viewobject.ReportListItemVO;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;
import com.orcchg.vikstra.domain.model.mapper.Mapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class GroupReportEssenceToVoMapper implements Mapper<GroupReportEssence, ReportListItemVO> {

    @Inject
    public GroupReportEssenceToVoMapper() {
    }

    @Override
    public ReportListItemVO map(GroupReportEssence object) {
        Group group = object.group();
        ReportListItemVO viewObject = ReportListItemVO.builder()
                .setGroupId(object.group().id())
                .setGroupName(group.name())
                .setMembersCount(group.membersCount())
                .setReportStatus(object.status())
                .build();
        viewObject.setReverted(object.wasReverted());
        return viewObject;
    }

    @Override
    public List<ReportListItemVO> map(List<GroupReportEssence> list) {
        List<ReportListItemVO> mapped = new ArrayList<>();
        for (GroupReportEssence item : list) {
            mapped.add(map(item));
        }
        return mapped;
    }
}
