package com.orcchg.vikstra.domain.repository;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;

import java.util.List;

public interface IReportRepository {

    /* Create */
    // ------------------------------------------
    GroupReportBundle addGroupReports(List<GroupReportEssence> many);

    /* Read */
    // ------------------------------------------
    @Nullable GroupReportBundle groupReports(long id);

    /* Delete */
    // ------------------------------------------
    boolean clear();
    boolean deleteGroupReports(long id);
}
