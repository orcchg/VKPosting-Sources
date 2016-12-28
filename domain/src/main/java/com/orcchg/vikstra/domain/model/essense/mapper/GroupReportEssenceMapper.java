package com.orcchg.vikstra.domain.model.essense.mapper;

import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;
import com.orcchg.vikstra.domain.model.mapper.Mapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class GroupReportEssenceMapper implements Mapper<GroupReportEssence, GroupReport> {

    private long groupReportId;
    private long timestamp;

    @Inject
    public GroupReportEssenceMapper(long groupReportId, long timestamp) {
        this.groupReportId = groupReportId;
        this.timestamp = timestamp;
    }

    public void setGroupReportId(long groupReportId) {
        this.groupReportId = groupReportId;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public GroupReport map(GroupReportEssence object) {
        return GroupReport.builder()
                .setId(groupReportId)
                .setErrorCode(object.errorCode())
                .setGroup(object.group())
                .setTimestamp(timestamp)
                .setWallPostId(object.wallPostId())
                .build();
    }

    @Override
    public List<GroupReport> map(List<GroupReportEssence> list) {
        List<GroupReport> mapped = new ArrayList<>();
        for (GroupReportEssence item : list) {
            mapped.add(map(item));
        }
        return mapped;
    }
}
