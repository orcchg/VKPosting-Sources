package com.orcchg.vikstra.data.source.local.model.mapper;

import com.orcchg.vikstra.data.source.local.model.KeywordDBO;
import com.orcchg.vikstra.data.source.local.model.populator.KeywordToDboPopulator;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.mapper.DuplexMapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class KeywordToDboMapper implements DuplexMapper<Keyword, KeywordDBO> {

    private final KeywordToDboPopulator keywordToDboPopulator;

    @Inject
    public KeywordToDboMapper(KeywordToDboPopulator keywordToDboPopulator) {
        this.keywordToDboPopulator = keywordToDboPopulator;
    }

    /* Direct mapping */
    // ------------------------------------------
    @Override
    public KeywordDBO map(Keyword object) {
        KeywordDBO dbo = new KeywordDBO();
        keywordToDboPopulator.populate(object, dbo);
        return dbo;
    }

    @Override
    public List<KeywordDBO> map(List<Keyword> list) {
        List<KeywordDBO> mapped = new ArrayList<>();
        for (Keyword item : list) {
            mapped.add(map(item));
        }
        return mapped;
    }

    /* Backward mapping */
    // ------------------------------------------
    @Override
    public Keyword mapBack(KeywordDBO object) {
        return Keyword.create(object.keyword);
    }

    @Override
    public List<Keyword> mapBack(List<KeywordDBO> list) {
        List<Keyword> mapped = new ArrayList<>();
        for (KeywordDBO item : list) {
            mapped.add(mapBack(item));
        }
        return mapped;
    }
}
