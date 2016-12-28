package com.orcchg.vikstra.data.source.repository.report;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;
import com.orcchg.vikstra.domain.model.essense.mapper.GroupReportEssenceMapper;
import com.orcchg.vikstra.domain.repository.IReportRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class ReportRepositoryImpl implements IReportRepository {

    private final IReportStorage cloudSource;
    private final IReportStorage localSource;

    @Inject
    ReportRepositoryImpl(@Named("reportCloud") IReportStorage cloudSource,
                         @Named("reportDatabase") IReportStorage localSource) {
        this.cloudSource = cloudSource;
        this.localSource = localSource;
    }

    /* Create */
    // ------------------------------------------
    @Override
    public GroupReportBundle addGroupReports(List<GroupReportEssence> many) {
        // TODO: impl cloudly
        long lastId = localSource.getLastId();
        List<GroupReport> reports = new ArrayList<>();
        GroupReportBundle bundle = GroupReportBundle.builder()
                .setId(++lastId)
                .setGroupReports(reports)
                .setTimestamp(System.currentTimeMillis())
                .build();

        // TODO: set proper id
        GroupReportEssenceMapper mapper = new GroupReportEssenceMapper(1000, System.currentTimeMillis());
        for (GroupReportEssence essence : many) {
            bundle.groupReports().add(mapper.map(essence));
            mapper.setGroupReportId(1000);
            mapper.setTimestamp(System.currentTimeMillis());
        }
        return localSource.addGroupReports(bundle);
    }

    /* Read */
    // ------------------------------------------
    @Nullable @Override
    public GroupReportBundle groupReports(long id) {
        // TODO: impl cloudly
        return localSource.groupReports(id);
    }

    /* Delete */
    // ------------------------------------------
    @Override
    public boolean clear() {
        // TODO: impl cloudly
        return localSource.clear();
    }

    @Override
    public boolean deleteGroupReports(long id) {
        // TODO: impl cloudly
        return localSource.deleteGroupReports(id);
    }
}
