package com.orcchg.vikstra.data.source.local.model.populator;

import com.orcchg.vikstra.data.source.local.model.KeywordDBO;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.mapper.Populator;

import javax.inject.Inject;

public class KeywordToDboPopulator implements Populator<Keyword, KeywordDBO> {

    @Inject
    public KeywordToDboPopulator() {
    }

    @Override
    public void populate(Keyword object, KeywordDBO dbo) {
        dbo.keyword = object.keyword();
    }
}
