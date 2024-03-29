package com.orcchg.vikstra.data.source.local.keyword;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.data.injection.migration.DaggerMigrationComponent;
import com.orcchg.vikstra.data.injection.migration.MigrationComponent;
import com.orcchg.vikstra.data.source.local.model.KeywordBundleDBO;
import com.orcchg.vikstra.data.source.local.model.KeywordDBO;
import com.orcchg.vikstra.data.source.local.model.mapper.KeywordBundleToDboMapper;
import com.orcchg.vikstra.data.source.local.model.populator.KeywordBundleToDboPopulator;
import com.orcchg.vikstra.data.source.local.model.populator.KeywordToDboPopulator;
import com.orcchg.vikstra.data.source.repository.RepoUtility;
import com.orcchg.vikstra.data.source.repository.keyword.IKeywordStorage;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmResults;

@Singleton
public class KeywordDatabase implements IKeywordStorage {

    private final KeywordToDboPopulator keywordToDboPopulator;
    private final KeywordBundleToDboMapper keywordBundleToDboMapper;
    private final KeywordBundleToDboPopulator keywordBundleToDboPopulator;

    private final MigrationComponent migrationComponent;

    @Inject
    KeywordDatabase(KeywordToDboPopulator keywordToDboPopulator,
                    KeywordBundleToDboMapper keywordBundleToDboMapper,
                    KeywordBundleToDboPopulator keywordBundleToDboPopulator) {
        this.keywordToDboPopulator = keywordToDboPopulator;
        this.keywordBundleToDboMapper = keywordBundleToDboMapper;
        this.keywordBundleToDboPopulator = keywordBundleToDboPopulator;
        this.migrationComponent = DaggerMigrationComponent.create();
    }

    /* Create */
    // ------------------------------------------
    @DebugLog @Override
    public KeywordBundle addKeywords(@NonNull KeywordBundle bundle) {
        Realm realm = Realm.getInstance(migrationComponent.realmConfiguration());
        realm.executeTransaction((xrealm) -> {
            KeywordBundleDBO dbo = xrealm.createObject(KeywordBundleDBO.class);
            keywordBundleToDboPopulator.populate(bundle, dbo);
        });
        realm.close();
        return bundle;
    }

    @DebugLog @Override
    public boolean addKeywordToBundle(long id, Keyword keyword) {
        if (id == Constant.BAD_ID) return false;
        boolean result = false;
        Realm realm = Realm.getInstance(migrationComponent.realmConfiguration());
        KeywordBundleDBO dbo = realm.where(KeywordBundleDBO.class).equalTo(KeywordBundleDBO.COLUMN_ID, id).findFirst();
        if (dbo != null) {
            realm.executeTransaction((xrealm) -> {
                KeywordDBO xdbo = xrealm.createObject(KeywordDBO.class);
                keywordToDboPopulator.populate(keyword, xdbo);
                dbo.keywords.add(0, xdbo);  // put new item at the top of list
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
        Number number = realm.where(KeywordBundleDBO.class).max(KeywordBundleDBO.COLUMN_ID);
        long lastId = number != null ? number.longValue() : Constant.INIT_ID;
        realm.close();
        return lastId;
    }

    @DebugLog @Nullable @Override
    public KeywordBundle keywords(long id) {
        if (id != Constant.BAD_ID) {
            Realm realm = Realm.getInstance(migrationComponent.realmConfiguration());
            KeywordBundle model = null;
            KeywordBundleDBO dbo = realm.where(KeywordBundleDBO.class).equalTo(KeywordBundleDBO.COLUMN_ID, id).findFirst();
            if (dbo != null) model = keywordBundleToDboMapper.mapBack(dbo);
            realm.close();
            return model;
        }
        return null;
    }

    @DebugLog @Override
    public List<KeywordBundle> keywords(long... ids) {
        Realm realm = Realm.getInstance(migrationComponent.realmConfiguration());
        RealmResults<KeywordBundleDBO> dbos = realm.where(KeywordBundleDBO.class).findAll();
        List<KeywordBundle> models = new ArrayList<>();
        for (int i = 0; i < ids.length; ++i) {
            for (KeywordBundleDBO dbo : dbos) {
                if (dbo.id == ids[i]) {
                    models.add(keywordBundleToDboMapper.mapBack(dbo));
                    break;  // check next id, assuming all dbos have unique ids
                }
            }
        }
        realm.close();
        return models;
    }

    @DebugLog @Override
    public List<KeywordBundle> keywords(int limit, int offset) {
        RepoUtility.checkLimitAndOffset(limit, offset);
        Realm realm = Realm.getInstance(migrationComponent.realmConfiguration());
        RealmResults<KeywordBundleDBO> dbos = realm.where(KeywordBundleDBO.class).findAll();
        List<KeywordBundle> models = new ArrayList<>();
        int size = limit < 0 ? dbos.size() : limit;
        RepoUtility.checkListBounds(offset + size - 1, dbos.size());
        for (int i = offset; i < offset + size; ++i) {
            models.add(keywordBundleToDboMapper.mapBack(dbos.get(i)));
        }
        realm.close();
        return models;
    }

    /* Update */
    // ------------------------------------------
    @DebugLog @Override
    public boolean updateKeywords(@NonNull KeywordBundle bundle) {
        boolean result = false;
        Realm realm = Realm.getInstance(migrationComponent.realmConfiguration());
        KeywordBundleDBO dbo = realm.where(KeywordBundleDBO.class).equalTo(KeywordBundleDBO.COLUMN_ID, bundle.id()).findFirst();
        if (dbo != null) {
            realm.executeTransaction((xrealm) -> keywordBundleToDboPopulator.populate(bundle, dbo));
            result = true;
        }
        realm.close();
        return result;
    }

    @DebugLog @Override
    public boolean updateKeywordsTitle(long id, String newTitle) {
        boolean result = false;
        Realm realm = Realm.getInstance(migrationComponent.realmConfiguration());
        KeywordBundleDBO dbo = realm.where(KeywordBundleDBO.class).equalTo(KeywordBundleDBO.COLUMN_ID, id).findFirst();
        if (dbo != null) {
            realm.executeTransaction((xrealm) -> dbo.title = newTitle);
            result = true;
        }
        realm.close();
        return result;
    }

    /* Delete */
    // ------------------------------------------
    @DebugLog @Override
    public boolean deleteKeywords(long id) {
        if (id == Constant.BAD_ID) return false;
        boolean result = false;
        Realm realm = Realm.getInstance(migrationComponent.realmConfiguration());
        KeywordBundleDBO dbo = realm.where(KeywordBundleDBO.class).equalTo(KeywordBundleDBO.COLUMN_ID, id).findFirst();
        if (dbo != null) {
            realm.executeTransaction((xrealm) -> dbo.deleteFromRealm());
            result = true;
        }
        realm.close();
        return result;
    }
}
