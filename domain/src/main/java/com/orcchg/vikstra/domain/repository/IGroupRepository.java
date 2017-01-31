package com.orcchg.vikstra.domain.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupBundle;

import java.util.Collection;
import java.util.List;

public interface IGroupRepository {

    /* Create */
    // ------------------------------------------
    GroupBundle addGroups(String title, long keywordBundleId, Collection<Group> groups);
    boolean addGroupToBundle(long id, Group group);

    /* Read */
    // ------------------------------------------
    @Nullable GroupBundle groups(long id);
    List<GroupBundle> groups();
    List<GroupBundle> groups(int limit, int offset);

    /* Update */
    // ------------------------------------------
    boolean updateGroups(@NonNull GroupBundle groups);
    boolean updateGroupsTitle(long id, String newTitle);

    /* Delete */
    // ------------------------------------------
    boolean deleteGroups(long id);
}
