package com.orcchg.vikstra.data.source.repository.group;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.executor.ReadWriteReentrantLock;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupBundle;
import com.orcchg.vikstra.domain.repository.IGroupRepository;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class GroupRepositoryImpl implements IGroupRepository {

    private final IGroupStorage cloudSource;
    private final IGroupStorage localSource;
    private ReadWriteReentrantLock lock = new ReadWriteReentrantLock();

    @Inject
    GroupRepositoryImpl(@Named("groupCloud") IGroupStorage cloudSource,
                        @Named("groupDatabase") IGroupStorage localSource) {
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
    public GroupBundle addGroups(String title, long keywordBundleId, Collection<Group> groups) {
        try {
            lock.lockWrite();
            try {
                // TODO: impl cloudly
                long lastId = getLastId();
                GroupBundle bundle = GroupBundle.builder()
                        .setId(++lastId)
                        .setGroups(groups)
                        .setKeywordBundleId(keywordBundleId)
                        .setTimestamp(System.currentTimeMillis())
                        .setTitle(title)
                        .build();

                return localSource.addGroups(bundle);
            } finally {
                lock.unlockWrite();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    @Override
    public boolean addGroupToBundle(long id, Group group) {
        try {
            lock.lockWrite();
            try {
                // TODO: impl cloudly
                return localSource.addGroupToBundle(id, group);
            } finally {
                lock.unlockWrite();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    /* Read */
    // ------------------------------------------
    @Nullable @Override
    public GroupBundle groups(long id) {
        try {
            lock.lockRead();
            try {
                // TODO: impl cloudly
                return localSource.groups(id);
            } finally {
                lock.unlockRead();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    @Override
    public List<GroupBundle> groups() {
        return groups(-1, 0);
    }

    @Override
    public List<GroupBundle> groups(int limit, int offset) {
        try {
            lock.lockRead();
            try {
                // TODO: impl cloudly
                return localSource.groups(limit, offset);
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
    public boolean updateGroups(@NonNull GroupBundle groups) {
        try {
            lock.lockWrite();
            try {
                // TODO: impl cloudly
                return localSource.updateGroups(groups);
            } finally {
                lock.unlockWrite();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    @Override
    public boolean updateGroupsTitle(long id, String newTitle) {
        try {
            lock.lockWrite();
            try {
                // TODO: impl cloudly
                return localSource.updateGroupsTitle(id, newTitle);
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
    public boolean deleteGroups(long id) {
        try {
            lock.lockWrite();
            try {
                // TODO: impl cloudly
                return localSource.deleteGroups(id);
            } finally {
                lock.unlockWrite();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }
}
