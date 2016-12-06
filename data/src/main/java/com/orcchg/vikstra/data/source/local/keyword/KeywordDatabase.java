package com.orcchg.vikstra.data.source.local.keyword;

import com.orcchg.vikstra.data.source.local.model.KeywordBundleDBO;
import com.orcchg.vikstra.data.source.local.model.mapper.KeywordBundleToDboMapper;
import com.orcchg.vikstra.data.source.local.model.populator.KeywordBundleToDboPopulator;
import com.orcchg.vikstra.data.source.repository.keyword.IKeywordStorage;
import com.orcchg.vikstra.domain.model.KeywordBundle;

import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import io.realm.Realm;

public class KeywordDatabase implements IKeywordStorage {

    private final Realm realm;
    private final KeywordBundleToDboMapper keywordBundleToDboMapper;
    private final KeywordBundleToDboPopulator keywordBundleToDboPopulator;

    @Inject
    KeywordDatabase(Realm realm, KeywordBundleToDboMapper keywordBundleToDboMapper,
                    KeywordBundleToDboPopulator keywordBundleToDboPopulator) {
        this.realm = realm;
        this.keywordBundleToDboMapper = keywordBundleToDboMapper;
        this.keywordBundleToDboPopulator = keywordBundleToDboPopulator;
    }

    /* Create */
    // ------------------------------------------
    @DebugLog @Override
    public boolean addKeywords(KeywordBundle bundle) {
        realm.executeTransaction((realm) -> {
            KeywordBundleDBO dbo = realm.createObject(KeywordBundleDBO.class);
            keywordBundleToDboPopulator.populate(bundle, dbo);
        });
        return true;
    }

    /* Read */
    // ------------------------------------------
    @DebugLog @Override
    public KeywordBundle keywords(long id) {
        KeywordBundleDBO dbo = realm.where(KeywordBundleDBO.class).equalTo("id", id).findFirst();
        return keywordBundleToDboMapper.mapBack(dbo);
    }

    @DebugLog @Override
    public List<KeywordBundle> keywords() {
        // TODO: impl
        return null;
    }

    @DebugLog @Override
    public List<KeywordBundle> keywords(int limit, int offset) {
        // TODO: impl
        return null;
    }

    /* Update */
    // ------------------------------------------

    /* Delete */
    // ------------------------------------------
}
