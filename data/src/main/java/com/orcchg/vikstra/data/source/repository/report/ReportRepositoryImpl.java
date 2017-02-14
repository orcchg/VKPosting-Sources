package com.orcchg.vikstra.data.source.repository.report;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.executor.ReadWriteReentrantLock;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;
import com.orcchg.vikstra.domain.model.essense.mapper.GroupReportEssenceMapper;
import com.orcchg.vikstra.domain.repository.IReportRepository;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class ReportRepositoryImpl implements IReportRepository {

    private final IReportStorage cloudSource;
    private final IReportStorage localSource;
    private ReadWriteReentrantLock lock = new ReadWriteReentrantLock();

    @Inject
    ReportRepositoryImpl(@Named("reportCloud") IReportStorage cloudSource,
                         @Named("reportDatabase") IReportStorage localSource) {
        this.cloudSource = cloudSource;
        this.localSource = localSource;
    }

    @Override
    public long getLastId() {
        try {
            lock.lockRead();
            try {
                // TODO: impl cloudly
                return localSource.getLastId();
            } finally {
                lock.unlockRead();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return Constant.BAD_ID;
    }

    /* Create */
    // ------------------------------------------
    @Nullable @Override
    public GroupReportBundle addGroupReports(List<GroupReportEssence> many, long keywordBundleId, long postId) {
        try {
            lock.lockWrite();
            try {
                // TODO: impl cloudly
                long lastId = getLastId();
                List<GroupReport> reports = new ArrayList<>();
                GroupReportBundle bundle = GroupReportBundle.builder()
                        .setId(++lastId)
                        .setGroupReports(reports)
                        .setKeywordBundleId(keywordBundleId)
                        .setPostId(postId)
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
            } finally {
                lock.unlockWrite();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    /* Read */
    // ------------------------------------------
    @Nullable @Override
    public GroupReportBundle groupReports(long id) {
        try {
            lock.lockRead();
            try {
                // TODO: impl cloudly
                return localSource.groupReports(id);
            } finally {
                lock.unlockRead();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    @Override
    public List<GroupReportBundle> groupReports() {
        return groupReports(-1, 0);
    }

    @Override
    public List<GroupReportBundle> groupReports(int limit, int offset) {
        try {
            lock.lockRead();
            try {
                // TODO: impl cloudly
                return localSource.groupReports(limit, offset);
            } finally {
                lock.unlockRead();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return new ArrayList<>();
    }

    /* Update */
    // ------------------------------------------
    @Override
    public boolean updateReports(@NonNull GroupReportBundle reports) {
        try {
            lock.lockWrite();
            try {
                // TODO: impl cloudly
                return localSource.updateReports(reports);
            } finally {
                lock.unlockWrite();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    /* Delete */
    // ------------------------------------------
    @Override
    public boolean clear() {
        try {
            lock.lockWrite();
            try {
                // TODO: impl cloudly
                return localSource.clear();
            } finally {
                lock.unlockWrite();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    @Override
    public boolean deleteGroupReports(long id) {
        try {
            lock.lockWrite();
            try {
                // TODO: impl cloudly
                return localSource.deleteGroupReports(id);
            } finally {
                lock.unlockWrite();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }
}
