package com.orcchg.vikstra.data.source.repository.group;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupBundle;
import com.orcchg.vikstra.domain.repository.IGroupRepository;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class GroupRepositoryImpl implements IGroupRepository {

    private final IGroupStorage cloudSource;
    private final IGroupStorage localSource;

    @Inject
    GroupRepositoryImpl(@Named("groupCloud") IGroupStorage cloudSource,
                        @Named("groupDatabase") IGroupStorage localSource) {
        this.cloudSource = cloudSource;
        this.localSource = localSource;
    }

    @Override
    public long getLastId() {
        // TODO: impl cloudly
        return localSource.getLastId();
    }

    /* Create */
    // ------------------------------------------
    @Override
    public GroupBundle addGroups(String title, long keywordBundleId, Collection<Group> groups) {
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
    }

    @Override
    public boolean addGroupToBundle(long id, Group group) {
        // TODO: impl cloudly
        return localSource.addGroupToBundle(id, group);
    }

    /* Read */
    // ------------------------------------------
    @Nullable @Override
    public GroupBundle groups(long id) {
        // TODO: impl cloudly
        return localSource.groups(id);
    }

    @Override
    public List<GroupBundle> groups() {
        return groups(-1, 0);
    }

    @Override
    public List<GroupBundle> groups(int limit, int offset) {
        // TODO: impl cloudly
        return localSource.groups(limit, offset);
    }

    /* Update */
    // ------------------------------------------
    @Override
    public boolean updateGroups(@NonNull GroupBundle groups) {
        // TODO: impl cloudly
        return localSource.updateGroups(groups);
    }

    @Override
    public boolean updateGroupsTitle(long id, String newTitle) {
        // TODO: impl cloudly
        return localSource.updateGroupsTitle(id, newTitle);
    }

    /* Delete */
    // ------------------------------------------
    @Override
    public boolean deleteGroups(long id) {
        // TODO: impl cloudly
        return localSource.deleteGroups(id);
    }
}
