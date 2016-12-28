package com.orcchg.vikstra.data.source.remote.report;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.data.source.repository.report.IReportStorage;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.GroupReportBundle;

import javax.inject.Inject;

public class ReportCloud implements IReportStorage {

    @Inject
    ReportCloud() {
    }

    /* Create */
    // ------------------------------------------
    @Override
    public GroupReportBundle addGroupReports(@NonNull GroupReportBundle bundle) {
        return null;
    }

    @Override
    public boolean addGroupReportToBundle(long id, GroupReport report) {
        return false;
    }

    /* Read */
    // ------------------------------------------
    @Override
    public long getLastId() {
        return 0;
    }

    @Nullable @Override
    public GroupReportBundle groupReports(long id) {
        return null;
    }

    /* Delete */
    // ------------------------------------------
    @Override
    public boolean clear() {
        return false;
    }

    @Override
    public boolean deleteGroupReports(long id) {
        return false;
    }
}
