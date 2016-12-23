package com.orcchg.vikstra.data.source.repository.group;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.data.source.repository.IStorage;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupBundle;

import java.util.List;

public interface IGroupStorage extends IStorage {

    /* Create */
    // ------------------------------------------
    long addGroups(@NonNull GroupBundle bundle);
    boolean addGroupToBundle(long id, Group group);

    /* Read */
    // ------------------------------------------
    @Nullable GroupBundle groups(long id);
    List<GroupBundle> groups(int limit, int offset);

    /* Update */
    // ------------------------------------------
    boolean updateGroups(@NonNull GroupBundle groups);

    /* Delete */
    // ------------------------------------------
    boolean deleteGroups(long id);
}