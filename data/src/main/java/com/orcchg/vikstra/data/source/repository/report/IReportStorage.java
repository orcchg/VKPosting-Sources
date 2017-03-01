package com.orcchg.vikstra.data.source.repository.report;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.data.source.repository.IStorage;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.GroupReportBundle;

import java.util.List;

public interface IReportStorage extends IStorage {

    /* Create */
    // ------------------------------------------
    GroupReportBundle addGroupReports(@NonNull GroupReportBundle bundle);
    boolean addGroupReportToBundle(long id, GroupReport report);

    /* Read */
    // ------------------------------------------
    @Nullable GroupReportBundle groupReports(long id);
    List<GroupReportBundle> groupReports(int limit, int offset);
    List<GroupReportBundle> groupReportsForUser(long userId);

    /* Update */
    // ------------------------------------------
    boolean updateReports(@NonNull GroupReportBundle reports);

    /* Delete */
    // ------------------------------------------
    boolean clear();
    boolean deleteGroupReports(long id);
}
