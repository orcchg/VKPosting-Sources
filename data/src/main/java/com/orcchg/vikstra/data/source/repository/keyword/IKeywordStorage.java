package com.orcchg.vikstra.data.source.repository.keyword;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.data.source.repository.IStorage;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;

import java.util.List;

public interface IKeywordStorage extends IStorage {

    /* Create */
    // ------------------------------------------
    boolean addKeywords(@NonNull KeywordBundle bundle);
    boolean addKeywordToBundle(long id, Keyword keyword);

    /* Read */
    // ------------------------------------------
    @Nullable KeywordBundle keywords(long id);
    List<KeywordBundle> keywords(int limit, int offset);

    /* Update */
    // ------------------------------------------
    boolean updateKeywords(@NonNull KeywordBundle bundle);

    /* Delete */
    // ------------------------------------------
}
