package com.orcchg.vikstra.data.source.local.report;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.data.injection.migration.DaggerMigrationComponent;
import com.orcchg.vikstra.data.injection.migration.MigrationComponent;
import com.orcchg.vikstra.data.source.local.model.GroupReportBundleDBO;
import com.orcchg.vikstra.data.source.local.model.GroupReportDBO;
import com.orcchg.vikstra.data.source.local.model.mapper.GroupReportBundleToDboMapper;
import com.orcchg.vikstra.data.source.local.model.populator.GroupReportBundleToDboPopulator;
import com.orcchg.vikstra.data.source.local.model.populator.GroupReportToDboPopulator;
import com.orcchg.vikstra.data.source.repository.RepoUtility;
import com.orcchg.vikstra.data.source.repository.report.IReportStorage;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.util.Constant;
import com.orcchg.vikstra.domain.util.endpoint.EndpointUtility;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmResults;

@Singleton
public class ReportDatabase implements IReportStorage {

    private final GroupReportToDboPopulator groupReportToDboPopulator;
    private final GroupReportBundleToDboMapper groupReportBundleToDboMapper;
    private final GroupReportBundleToDboPopulator groupReportBundleToDboPopulator;

    private final MigrationComponent migrationComponent;

    @Inject
    ReportDatabase(GroupReportToDboPopulator groupReportToDboPopulator,
                   GroupReportBundleToDboMapper groupReportBundleToDboMapper,
                   GroupReportBundleToDboPopulator groupReportBundleToDboPopulator) {
        this.groupReportToDboPopulator = groupReportToDboPopulator;
        this.groupReportBundleToDboMapper = groupReportBundleToDboMapper;
        this.groupReportBundleToDboPopulator = groupReportBundleToDboPopulator;
        this.migrationComponent = DaggerMigrationComponent.create();
    }

    /* Create */
    // ------------------------------------------
    @DebugLog @Override
    public GroupReportBundle addGroupReports(@NonNull GroupReportBundle bundle) {
        Realm realm = Realm.getInstance(migrationComponent.realmConfiguration());
        realm.executeTransaction((xrealm) -> {
            GroupReportBundleDBO dbo = xrealm.createObject(GroupReportBundleDBO.class);
            groupReportBundleToDboPopulator.populate(bundle, dbo);
        });
        realm.close();
        return bundle;
    }

    @DebugLog @Override
    public boolean addGroupReportToBundle(long id, GroupReport report) {
        if (id == Constant.BAD_ID) return false;
        boolean result = false;
        Realm realm = Realm.getInstance(migrationComponent.realmConfiguration());
        GroupReportBundleDBO dbo = realm.where(GroupReportBundleDBO.class).equalTo(GroupReportBundleDBO.COLUMN_ID, id).findFirst();
        if (dbo != null) {
            realm.executeTransaction((xrealm) -> {
                GroupReportDBO xdbo = xrealm.createObject(GroupReportDBO.class);
                groupReportToDboPopulator.populate(report, xdbo);
                dbo.groupReports.add(0, xdbo);  // put new item at the top of list
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
        Realm realm = Realm.getInstance(migrationComponent.realmConfiguration());
        Number number = realm.where(GroupReportBundleDBO.class).max(GroupReportBundleDBO.COLUMN_ID);
        long lastId = number != null ? number.longValue() : Constant.INIT_ID;
        realm.close();
        return lastId;
    }

    @DebugLog @Nullable @Override
    public GroupReportBundle groupReports(long id) {
        if (id != Constant.BAD_ID) {
            Realm realm = Realm.getInstance(migrationComponent.realmConfiguration());
            GroupReportBundle model = null;
            GroupReportBundleDBO dbo = realm.where(GroupReportBundleDBO.class).equalTo(GroupReportBundleDBO.COLUMN_ID, id).findFirst();
            if (dbo != null) model = groupReportBundleToDboMapper.mapBack(dbo);
            realm.close();
            return model;
        }
        return null;
    }

    @Override
    public List<GroupReportBundle> groupReports(int limit, int offset) {
        RepoUtility.checkLimitAndOffset(limit, offset);
        Realm realm = Realm.getInstance(migrationComponent.realmConfiguration());
        RealmResults<GroupReportBundleDBO> dbos = realm.where(GroupReportBundleDBO.class).findAll();
        List<GroupReportBundle> models = new ArrayList<>();
        int size = limit < 0 ? dbos.size() : limit;
        RepoUtility.checkListBounds(offset + size - 1, dbos.size());
        for (int i = offset; i < offset + size; ++i) {
            models.add(groupReportBundleToDboMapper.mapBack(dbos.get(i)));
        }
        realm.close();
        return models;
    }

    @Override
    public List<GroupReportBundle> groupReportsForUser(long userId) {
        List<GroupReportBundle> models = new ArrayList<>();
        if (userId != EndpointUtility.BAD_USER_ID) {
            Realm realm = Realm.getInstance(migrationComponent.realmConfiguration());
            RealmResults<GroupReportBundleDBO> dbos = realm.where(GroupReportBundleDBO.class).equalTo(GroupReportBundleDBO.COLUMN_USER_ID, userId).findAll();
            for (GroupReportBundleDBO dbo : dbos) {
                models.add(groupReportBundleToDboMapper.mapBack(dbo));
            }
            realm.close();
        }
        return models;
    }

    /* Update */
    // ------------------------------------------
    @DebugLog @Override
    public boolean updateReports(@NonNull GroupReportBundle reports) {
        boolean result = false;
        Realm realm = Realm.getInstance(migrationComponent.realmConfiguration());
        GroupReportBundleDBO dbo = realm.where(GroupReportBundleDBO.class).equalTo(GroupReportBundleDBO.COLUMN_ID, reports.id()).findFirst();
        if (dbo != null) {
            realm.executeTransaction((xrealm) -> groupReportBundleToDboPopulator.populate(reports, dbo));
            result = true;
        }
        realm.close();
        return result;
    }

    /* Delete */
    // ------------------------------------------
    @DebugLog @Override
    public boolean clear() {
        Realm realm = Realm.getInstance(migrationComponent.realmConfiguration());
        realm.executeTransaction((xrealm) -> {
            RealmResults<GroupReportBundleDBO> dbos = xrealm.where(GroupReportBundleDBO.class).findAll();
            dbos.deleteAllFromRealm();
        });
        realm.close();
        return true;
    }

    @DebugLog @Override
    public boolean deleteGroupReports(long id) {
        if (id == Constant.BAD_ID) return false;
        boolean result = false;
        Realm realm = Realm.getInstance(migrationComponent.realmConfiguration());
        GroupReportBundleDBO dbo = realm.where(GroupReportBundleDBO.class).equalTo(GroupReportBundleDBO.COLUMN_ID, id).findFirst();
        if (dbo != null) {
            realm.executeTransaction((xrealm) -> dbo.deleteFromRealm());
            result = true;
        }
        realm.close();
        return result;
    }
}
