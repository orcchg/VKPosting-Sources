package com.orcchg.vikstra.data.source.repository.keyword;

import android.support.annotation.NonNull;

import com.orcchg.vikstra.domain.model.KeywordBundle;

import java.util.List;

public interface IKeywordStorage {

    /* Create */
    // ------------------------------------------
    boolean addKeywords(@NonNull KeywordBundle bundle);

    /* Read */
    // ------------------------------------------
    KeywordBundle keywords(long id);
    List<KeywordBundle> keywords();
    List<KeywordBundle> keywords(int limit, int offset);

    /* Update */
    // ------------------------------------------
    boolean updateKeywords(@NonNull KeywordBundle bundle);

    /* Delete */
    // ------------------------------------------
}
