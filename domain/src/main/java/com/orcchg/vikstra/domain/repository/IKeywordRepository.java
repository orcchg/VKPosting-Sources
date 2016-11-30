package com.orcchg.vikstra.domain.repository;

import com.orcchg.vikstra.domain.model.KeywordBundle;

import java.util.List;

public interface IKeywordRepository {
    KeywordBundle keywords(long id);
    List<KeywordBundle> keywords();
    List<KeywordBundle> keywords(int limit, int offset);
}
