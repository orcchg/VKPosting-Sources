package com.orcchg.vikstra.data.source.remote.keyword;

import android.support.annotation.NonNull;

import com.orcchg.vikstra.data.source.repository.keyword.IKeywordStorage;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class KeywordCloud implements IKeywordStorage {

    @Inject
    KeywordCloud() {
    }

    /* Create */
    // ------------------------------------------
    @Override
    public KeywordBundle addKeywords(KeywordBundle bundle) {
        return null;
    }

    @Override
    public boolean addKeywordToBundle(long id, Keyword keyword) {
        return false;
    }

    /* Read */
    // ------------------------------------------
    @Override
    public long getLastId() {
        return 0;
    }

    @Override
    public KeywordBundle keywords(long id) {
        if (id != Constant.BAD_ID) {
            // TODO: cloud impl
        }
        return null;
    }

    @Override
    public List<KeywordBundle> keywords(int limit, int offset) {
        return null;
    }

    /* Update */
    // ------------------------------------------
    @Override
    public boolean updateKeywords(@NonNull KeywordBundle bundle) {
        return false;
    }

    @Override
    public boolean updateGroupsTitle(long id, String newTitle) {
        return false;
    }

    /* Delete */
    // ------------------------------------------
    @Override
    public boolean deleteKeywords(long id) {
        if (id == Constant.BAD_ID) return false;
        // TODO: cloud impl
        return false;
    }
}
