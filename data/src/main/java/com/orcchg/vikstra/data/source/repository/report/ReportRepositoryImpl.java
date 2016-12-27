package com.orcchg.vikstra.data.source.repository.report;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.model.GroupReport;
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
    public GroupReport addGroupReport(GroupReportEssence essence) {
        // TODO: impl cloudly
        long lastId = localSource.getLastId();
        GroupReportEssenceMapper mapper = new GroupReportEssenceMapper(++lastId, System.currentTimeMillis());
        return localSource.addGroupReport(mapper.map(essence));
    }

    @Override
    public List<GroupReport> addGroupReports(List<GroupReportEssence> many) {
        // TODO: make something more efficient in Realm
        List<GroupReport> list = new ArrayList<>();
        long lastId = localSource.getLastId();
        for (GroupReportEssence essence : many) {
            GroupReportEssenceMapper mapper = new GroupReportEssenceMapper(++lastId, System.currentTimeMillis());
            list.add(localSource.addGroupReport(mapper.map(essence)));
        }
        return list;
    }

    /* Read */
    // ------------------------------------------
    @Nullable @Override
    public GroupReport groupReport(long id) {
        // TODO: impl cloudly
        return localSource.groupReport(id);
    }

    @Nullable @Override
    public GroupReport pollGroupReport(long id) {
        // TODO: impl cloudly
        return localSource.pollGroupReport(id);
    }

    /* Delete */
    // ------------------------------------------
    @Override
    public boolean clear() {
        // TODO: impl cloudly
        return false;
    }

    @Override
    public boolean deleteGroupReport(long id) {
        // TODO: impl cloudly
        return false;
    }
}
