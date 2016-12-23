package com.orcchg.vikstra.data.source.remote.group;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.data.source.repository.group.IGroupStorage;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupBundle;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.List;

import javax.inject.Inject;

public class GroupCloud implements IGroupStorage {

    @Inject
    GroupCloud() {
    }

    /* Create */
    // ------------------------------------------
    @Override
    public long addGroups(@NonNull GroupBundle bundle) {
        return Constant.BAD_ID;
    }

    @Override
    public boolean addGroupToBundle(long id, Group group) {
        return false;
    }

    /* Read */
    // ------------------------------------------
    @Override
    public long getLastId() {
        return 0;
    }

    @Nullable @Override
    public GroupBundle groups(long id) {
        return null;
    }

    @Override
    public List<GroupBundle> groups(int limit, int offset) {
        return null;
    }

    /* Update */
    // ------------------------------------------
    @Override
    public boolean updateGroups(@NonNull GroupBundle groups) {
        return false;
    }

    /* Delete */
    // ------------------------------------------
    @Override
    public boolean deleteGroups(long id) {
        return false;
    }
}
