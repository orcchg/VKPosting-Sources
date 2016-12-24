package com.orcchg.vikstra.data.source.local.group;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.data.source.local.model.GroupBundleDBO;
import com.orcchg.vikstra.data.source.local.model.GroupDBO;
import com.orcchg.vikstra.data.source.local.model.mapper.GroupBundleToDboMapper;
import com.orcchg.vikstra.data.source.local.model.populator.GroupBundleToDboPopulator;
import com.orcchg.vikstra.data.source.local.model.populator.GroupToDboPopulator;
import com.orcchg.vikstra.data.source.repository.RepoUtility;
import com.orcchg.vikstra.data.source.repository.group.IGroupStorage;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupBundle;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmResults;

public class GroupDatabase implements IGroupStorage {

    private final GroupToDboPopulator groupToDboPopulator;
    private final GroupBundleToDboMapper groupBundleToDboMapper;
    private final GroupBundleToDboPopulator groupBundleToDboPopulator;

    @Inject
    GroupDatabase(GroupToDboPopulator groupToDboPopulator,
                  GroupBundleToDboMapper groupBundleToDboMapper,
                  GroupBundleToDboPopulator groupBundleToDboPopulator) {
        this.groupToDboPopulator = groupToDboPopulator;
        this.groupBundleToDboMapper = groupBundleToDboMapper;
        this.groupBundleToDboPopulator = groupBundleToDboPopulator;
    }

    /* Create */
    // ------------------------------------------
    @DebugLog @Override
    public GroupBundle addGroups(@NonNull GroupBundle bundle) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction((xrealm) -> {
            GroupBundleDBO dbo = xrealm.createObject(GroupBundleDBO.class);
            groupBundleToDboPopulator.populate(bundle, dbo);
        });
        realm.close();
        return bundle;
    }

    @DebugLog @Override
    public boolean addGroupToBundle(long id, Group group) {
        boolean result = false;
        Realm realm = Realm.getDefaultInstance();
        GroupBundleDBO dbo = realm.where(GroupBundleDBO.class).equalTo(GroupBundleDBO.COLUMN_ID, id).findFirst();
        if (dbo != null) {
            realm.executeTransaction((xrealm) -> {
                GroupDBO xdbo = xrealm.createObject(GroupDBO.class);
                groupToDboPopulator.populate(group, xdbo);
                dbo.groups.add(0, xdbo);  // put new item at the top of list
            });
            result = true;
        }
        realm.close();
        return result;
    }

    /* Read */
    // ------------------------------------------
    @DebugLog @Override
    public long getLastId() {
        Realm realm = Realm.getDefaultInstance();
        Number number = realm.where(GroupBundleDBO.class).max(GroupBundleDBO.COLUMN_ID);
        long lastId = number != null ? number.longValue() : Constant.INIT_ID;
        realm.close();
        return lastId;
    }

    @DebugLog @Nullable @Override
    public GroupBundle groups(long id) {
        if (id != Constant.BAD_ID) {
            Realm realm = Realm.getDefaultInstance();
            GroupBundle model = null;
            GroupBundleDBO dbo = realm.where(GroupBundleDBO.class).equalTo(GroupBundleDBO.COLUMN_ID, id).findFirst();
            if (dbo != null) model = groupBundleToDboMapper.mapBack(dbo);
            realm.close();
            return model;
        }
        return null;
    }

    @DebugLog @Override
    public List<GroupBundle> groups(int limit, int offset) {
        RepoUtility.checkLimitAndOffset(limit, offset);
        Realm realm = Realm.getDefaultInstance();
        RealmResults<GroupBundleDBO> dbos = realm.where(GroupBundleDBO.class).findAll();
        List<GroupBundle> models = new ArrayList<>();
        int size = limit < 0 ? dbos.size() : limit;
        RepoUtility.checkListBounds(offset + size - 1, dbos.size());
        for (int i = offset; i < offset + size; ++i) {
            models.add(groupBundleToDboMapper.mapBack(dbos.get(i)));
        }
        realm.close();
        return models;
    }

    /* Update */
    // ------------------------------------------
    @DebugLog @Override
    public boolean updateGroups(@NonNull GroupBundle bundle) {
        boolean result = false;
        Realm realm = Realm.getDefaultInstance();
        GroupBundleDBO dbo = realm.where(GroupBundleDBO.class).equalTo(GroupBundleDBO.COLUMN_ID, bundle.id()).findFirst();
        if (dbo != null) {
            realm.executeTransaction((xrealm) -> {
                groupBundleToDboPopulator.populate(bundle, dbo);
            });
            result = true;
        }
        realm.close();
        return result;
    }

    /* Delete */
    // ------------------------------------------
    @Override
    public boolean deleteGroups(long id) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction((xrealm) -> {
            GroupBundleDBO dbo = realm.where(GroupBundleDBO.class).equalTo(GroupBundleDBO.COLUMN_ID, id).findFirst();
            dbo.deleteFromRealm();
        });
        realm.close();
        return true;
    }
}
