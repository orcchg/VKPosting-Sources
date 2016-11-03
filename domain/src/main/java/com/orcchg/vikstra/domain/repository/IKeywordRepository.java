package com.orcchg.vikstra.domain.repository;

import com.orcchg.vikstra.domain.model.KeywordBundle;

import java.util.List;

public interface IKeywordRepository {
    List<KeywordBundle> keywords();
    List<KeywordBundle> keywords(int limit, int offset);
}
