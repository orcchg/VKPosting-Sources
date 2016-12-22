package com.orcchg.vikstra.domain.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;

import java.util.Collection;
import java.util.List;

public interface IKeywordRepository {

    /* Create */
    // ------------------------------------------
    long addKeywords(String title, Collection<Keyword> keywords);
    boolean addKeywordToBundle(long id, Keyword keyword);

    /* Read */
    // ------------------------------------------
    @Nullable KeywordBundle keywords(long id);
    List<KeywordBundle> keywords();
    List<KeywordBundle> keywords(int limit, int offset);

    /* Update */
    // ------------------------------------------
    boolean updateKeywords(@NonNull KeywordBundle keywords);

    /* Delete */
    // ------------------------------------------
}
