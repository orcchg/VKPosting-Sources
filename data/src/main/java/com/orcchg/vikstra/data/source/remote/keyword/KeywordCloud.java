package com.orcchg.vikstra.data.source.remote.keyword;

import com.orcchg.vikstra.data.source.repository.keyword.IKeywordStorage;
import com.orcchg.vikstra.domain.model.KeywordBundle;

import java.util.List;

import javax.inject.Inject;

public class KeywordCloud implements IKeywordStorage {

    @Inject
    KeywordCloud() {
    }

    /* Create */
    // ------------------------------------------
    @Override
    public boolean addKeywords(KeywordBundle bundle) {
        return false;
    }

    /* Read */
    // ------------------------------------------
    @Override
    public KeywordBundle keywords(long id) {
        return null;
    }

    @Override
    public List<KeywordBundle> keywords() {
        return null;
    }

    @Override
    public List<KeywordBundle> keywords(int limit, int offset) {
        return null;
    }

    /* Update */
    // ------------------------------------------

    /* Delete */
    // ------------------------------------------
}