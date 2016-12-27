package com.orcchg.vikstra.data.source.repository.report;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.data.source.repository.IStorage;
import com.orcchg.vikstra.domain.model.GroupReport;

import java.util.List;

public interface IReportStorage extends IStorage {

    /* Create */
    // ------------------------------------------
    GroupReport addGroupReport(GroupReport report);
    List<GroupReport> addGroupReports(List<GroupReport> many);

    /* Read */
    // ------------------------------------------
    @Nullable GroupReport groupReport(long id);
    @Nullable GroupReport pollGroupReport(long id);

    /* Delete */
    // ------------------------------------------
    boolean clear();
    boolean deleteGroupReport(long id);
}
