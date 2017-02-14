package com.orcchg.vikstra.domain.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;

import java.util.List;

public interface IReportRepository extends IRepository {

    /* Create */
    // ------------------------------------------
    @Nullable GroupReportBundle addGroupReports(List<GroupReportEssence> many, long keywordBundleId, long postId);

    /* Read */
    // ------------------------------------------
    @Nullable GroupReportBundle groupReports(long id);
    List<GroupReportBundle> groupReports();
    List<GroupReportBundle> groupReports(int limit, int offset);

    /* Update */
    // ------------------------------------------
    boolean updateReports(@NonNull GroupReportBundle reports);

    /* Delete */
    // ------------------------------------------
    boolean clear();
    boolean deleteGroupReports(long id);
}
