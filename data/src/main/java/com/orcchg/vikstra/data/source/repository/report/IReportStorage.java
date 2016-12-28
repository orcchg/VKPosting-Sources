package com.orcchg.vikstra.data.source.repository.report;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.data.source.repository.IStorage;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.GroupReportBundle;

public interface IReportStorage extends IStorage {

    /* Create */
    // ------------------------------------------
    GroupReportBundle addGroupReports(@NonNull GroupReportBundle bundle);
    boolean addGroupReportToBundle(long id, GroupReport report);

    /* Read */
    // ------------------------------------------
    @Nullable GroupReportBundle groupReports(long id);

    /* Delete */
    // ------------------------------------------
    boolean clear();
    boolean deleteGroupReports(long id);
}
