package com.orcchg.vikstra.data.source.repository.keyword;

import com.orcchg.vikstra.domain.model.KeywordBundle;

import java.util.List;

public interface IKeywordStorage {

    /* Create */
    // ------------------------------------------
    boolean addKeywords(KeywordBundle bundle);

    /* Read */
    // ------------------------------------------
    KeywordBundle keywords(long id);
    List<KeywordBundle> keywords();
    List<KeywordBundle> keywords(int limit, int offset);

    /* Update */
    // ------------------------------------------

    /* Delete */
    // ------------------------------------------
}
