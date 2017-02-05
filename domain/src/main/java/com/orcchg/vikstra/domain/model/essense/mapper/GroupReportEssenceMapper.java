package com.orcchg.vikstra.domain.model.essense.mapper;

import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;
import com.orcchg.vikstra.domain.model.mapper.DuplexMapper;
import com.orcchg.vikstra.domain.model.mapper.Mapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class GroupReportEssenceMapper implements DuplexMapper<GroupReportEssence, GroupReport> {

    private long groupReportId;
    private long timestamp;

    @Inject
    public GroupReportEssenceMapper() {
    }

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

    /* Direct mapping */
    // ------------------------------------------
    @Override
    public GroupReport map(GroupReportEssence object) {
        return GroupReport.builder()
                .setId(groupReportId)
                .setCancelled(object.cancelled())
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

    /* Backward mapping */
    // ------------------------------------------
    /**
     * Maps {@link GroupReport} back to {@link GroupReportEssence} discarding
     * {@link GroupReport#id()} and {@link GroupReport#timestamp()} fields in {@param object}.
     */
    @Override
    public GroupReportEssence mapBack(GroupReport object) {
        return GroupReportEssence.builder()
                .setCancelled(object.cancelled())
                .setErrorCode(object.errorCode())
                .setGroup(object.group())
                .setWallPostId(object.wallPostId())
                .build();
    }

    @Override
    public List<GroupReportEssence> mapBack(List<GroupReport> list) {
        List<GroupReportEssence> mapped = new ArrayList<>();
        for (GroupReport item : list) {
            mapped.add(mapBack(item));
        }
        return mapped;
    }
}
