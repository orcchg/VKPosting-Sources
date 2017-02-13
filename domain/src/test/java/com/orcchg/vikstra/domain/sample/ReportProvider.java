package com.orcchg.vikstra.domain.sample;

import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.GroupReportBundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

public class ReportProvider {
    private static long BASE_ID = 1000;

    private final GroupBundleProvider groupsProvider;

    private Random rng = new Random();

    @Inject
    public ReportProvider(GroupBundleProvider groupsProvider) {
        this.groupsProvider = groupsProvider;
    }

    public GroupReportBundle reportBundle() {
        return GroupReportBundle.builder()
                .setId(1000)
                .setGroupReports(reports(rng.nextInt() % 10 + 1))
                .setTimestamp(1_456_789_101)
                .build();
    }

    // ------------------------------------------
    public GroupReport report() {
        return GroupReport.builder()
                .setId(BASE_ID++)
                .setCancelled(rng.nextBoolean())
                .setErrorCode(0)
                .setGroup(groupsProvider.group())
                .setTimestamp(1_456_789_101)
                .setWallPostId(rng.nextInt() + 1)
                .build();
    }

    // ------------------------------------------
    public List<GroupReport> reports(int size) {
        List<GroupReport> list = new ArrayList<>();
        for (int i = 0; i < size; ++i) list.add(report());
        return list;
    }
}
