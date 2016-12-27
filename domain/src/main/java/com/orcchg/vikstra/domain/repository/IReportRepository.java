package com.orcchg.vikstra.domain.repository;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;

import java.util.List;

public interface IReportRepository {

    /* Create */
    // ------------------------------------------
    GroupReport addGroupReport(GroupReportEssence essence);
    List<GroupReport> addGroupReports(List<GroupReportEssence> many);

    /* Read */
    // ------------------------------------------
    @Nullable GroupReport groupReport(long id);
    @Nullable GroupReport pollGroupReport(long id);

    /* Delete */
    // ------------------------------------------
    boolean clear();
    boolean deleteGroupReport(long id);
}
