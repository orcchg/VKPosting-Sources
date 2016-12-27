package com.orcchg.vikstra.data.source.local.report;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.data.source.repository.report.IReportStorage;
import com.orcchg.vikstra.domain.model.GroupReport;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ReportDatabase implements IReportStorage {

    @Inject
    ReportDatabase() {
    }

    /* Create */
    // ------------------------------------------
    @Override
    public GroupReport addGroupReport(GroupReport report) {
        return null;
    }

    @Override
    public List<GroupReport> addGroupReports(List<GroupReport> many) {
        return null;
    }

    /* Read */
    // ------------------------------------------
    @Override
    public long getLastId() {
        return 0;
    }

    @Nullable @Override
    public GroupReport groupReport(long id) {
        return null;
    }

    @Nullable @Override
    public GroupReport pollGroupReport(long id) {
        return null;
    }

    /* Delete */
    // ------------------------------------------
    @Override
    public boolean clear() {
        return false;
    }

    @Override
    public boolean deleteGroupReport(long id) {
        return false;
    }
}
