package com.orcchg.vikstra.data.source.local.keyword;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.data.source.local.model.KeywordBundleDBO;
import com.orcchg.vikstra.data.source.local.model.mapper.KeywordBundleToDboMapper;
import com.orcchg.vikstra.data.source.local.model.populator.KeywordBundleToDboPopulator;
import com.orcchg.vikstra.data.source.repository.keyword.IKeywordStorage;
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

    private final KeywordBundleToDboMapper keywordBundleToDboMapper;
    private final KeywordBundleToDboPopulator keywordBundleToDboPopulator;

    @Inject
    KeywordDatabase(KeywordBundleToDboMapper keywordBundleToDboMapper,
                    KeywordBundleToDboPopulator keywordBundleToDboPopulator) {
        this.keywordBundleToDboMapper = keywordBundleToDboMapper;
        this.keywordBundleToDboPopulator = keywordBundleToDboPopulator;
    }

    /* Create */
    // ------------------------------------------
    @DebugLog @Override
    public boolean addKeywords(KeywordBundle bundle) {
        Realm.getDefaultInstance().executeTransaction((realm) -> {
            KeywordBundleDBO dbo = realm.createObject(KeywordBundleDBO.class);
            keywordBundleToDboPopulator.populate(bundle, dbo);
        });
        return true;
    }

    /* Read */
    // ------------------------------------------
    @DebugLog @Nullable @Override
    public KeywordBundle keywords(long id) {
        if (id != Constant.BAD_ID) {
            KeywordBundleDBO dbo = Realm.getDefaultInstance().where(KeywordBundleDBO.class).equalTo("id", id).findFirst();
            if (dbo != null) return keywordBundleToDboMapper.mapBack(dbo);
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
        RealmResults<KeywordBundleDBO> dbos = Realm.getDefaultInstance().where(KeywordBundleDBO.class).findAll();
        List<KeywordBundle> models = new ArrayList<>();
        for (KeywordBundleDBO dbo : dbos) {
            models.add(keywordBundleToDboMapper.mapBack(dbo));
        }
        return models;
    }

    /* Update */
    // ------------------------------------------

    /* Delete */
    // ------------------------------------------
}
