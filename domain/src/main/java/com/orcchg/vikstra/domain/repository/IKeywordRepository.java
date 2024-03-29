package com.orcchg.vikstra.domain.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;

import java.util.Collection;
import java.util.List;

public interface IKeywordRepository extends IRepository {

    /* Create */
    // ------------------------------------------
    @Nullable KeywordBundle addKeywords(String title, Collection<Keyword> keywords);
    boolean addKeywordToBundle(long id, Keyword keyword);

    /* Read */
    // ------------------------------------------
    @Nullable KeywordBundle keywords(long id);
    List<KeywordBundle> keywords();
    List<KeywordBundle> keywords(long... ids);
    List<KeywordBundle> keywords(int limit, int offset);

    /* Update */
    // ------------------------------------------
    boolean updateKeywords(@NonNull KeywordBundle keywords);
    boolean updateKeywordsTitle(long id, String newTitle);

    /* Delete */
    // ------------------------------------------
    boolean deleteKeywords(long id);
}
