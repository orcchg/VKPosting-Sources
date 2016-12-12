package com.orcchg.vikstra.data.source.local.keyword;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.data.source.local.model.KeywordBundleDBO;
import com.orcchg.vikstra.data.source.local.model.KeywordDBO;
import com.orcchg.vikstra.data.source.local.model.mapper.KeywordBundleToDboMapper;
import com.orcchg.vikstra.data.source.local.model.populator.KeywordBundleToDboPopulator;
import com.orcchg.vikstra.data.source.local.model.populator.KeywordToDboPopulator;
import com.orcchg.vikstra.data.source.repository.keyword.IKeywordStorage;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

public class KeywordDatabase implements IKeywordStorage {

    private final KeywordToDboPopulator keywordToDboPopulator;
    private final KeywordBundleToDboMapper keywordBundleToDboMapper;
    private final KeywordBundleToDboPopulator keywordBundleToDboPopulator;

    @Inject
    KeywordDatabase(KeywordToDboPopulator keywordToDboPopulator,
                    KeywordBundleToDboMapper keywordBundleToDboMapper,
                    KeywordBundleToDboPopulator keywordBundleToDboPopulator) {
        this.keywordToDboPopulator = keywordToDboPopulator;
        this.keywordBundleToDboMapper = keywordBundleToDboMapper;
        this.keywordBundleToDboPopulator = keywordBundleToDboPopulator;
    }

    /* Create */
    // ------------------------------------------
    @DebugLog @Override
    public boolean addKeywords(@NonNull KeywordBundle bundle) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction((xrealm) -> {
            KeywordBundleDBO dbo = xrealm.createObject(KeywordBundleDBO.class);
            keywordBundleToDboPopulator.populate(bundle, dbo);
        });
        realm.close();
        return true;
    }

    @DebugLog @Override
    public boolean addKeywordToBundle(long id, Keyword keyword) {
        boolean result = false;
        Realm realm = Realm.getDefaultInstance();
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
        Realm realm = Realm.getDefaultInstance();
        Number number = realm.where(KeywordBundleDBO.class).max(KeywordBundleDBO.COLUMN_ID);
        long lastId = number != null ? number.longValue() : Constant.INIT_ID;
        realm.close();
        return lastId;
    }

    @DebugLog @Nullable @Override
    public KeywordBundle keywords(long id) {
        if (id != Constant.BAD_ID) {
            Realm realm = Realm.getDefaultInstance();
            KeywordBundle model = null;
            KeywordBundleDBO dbo = realm.where(KeywordBundleDBO.class).equalTo(KeywordBundleDBO.COLUMN_ID, id).findFirst();
            if (dbo != null) model = keywordBundleToDboMapper.mapBack(dbo);
            realm.close();
            return model;
        }
        Timber.w("No keywords found by id %s", id);
        return null;
    }

    @DebugLog @Override
    public List<KeywordBundle> keywords() {
        return keywords(-1, 0);
    }

    @DebugLog @Override
    public List<KeywordBundle> keywords(int limit, int offset) {
        // TODO: use limit & offset
        Realm realm = Realm.getDefaultInstance();
        RealmResults<KeywordBundleDBO> dbos = realm.where(KeywordBundleDBO.class).findAll();
        List<KeywordBundle> models = new ArrayList<>();
        for (KeywordBundleDBO dbo : dbos) {
            models.add(keywordBundleToDboMapper.mapBack(dbo));
        }
        realm.close();
        return models;
    }

    /* Update */
    // ------------------------------------------
    @DebugLog @Override
    public boolean updateKeywords(@NonNull KeywordBundle bundle) {
        boolean result = false;
        Realm realm = Realm.getDefaultInstance();
        KeywordBundleDBO dbo = realm.where(KeywordBundleDBO.class).equalTo(KeywordBundleDBO.COLUMN_ID, bundle.id()).findFirst();
        if (dbo != null) {
            realm.executeTransaction((xrealm) -> {
                keywordBundleToDboPopulator.populate(bundle, dbo);
            });
            result = true;
        }
        realm.close();
        return result;
    }

    /* Delete */
    // ------------------------------------------
}
