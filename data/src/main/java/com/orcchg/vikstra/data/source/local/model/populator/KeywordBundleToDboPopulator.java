package com.orcchg.vikstra.data.source.local.model.populator;

import com.orcchg.vikstra.data.source.local.model.KeywordBundleDBO;
import com.orcchg.vikstra.data.source.local.model.mapper.KeywordToDboMapper;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.model.mapper.Populator;

import javax.inject.Inject;

import io.realm.RealmList;

public class KeywordBundleToDboPopulator implements Populator<KeywordBundle, KeywordBundleDBO> {

    private final KeywordToDboMapper keywordToDboMapper;

    @Inject
    public KeywordBundleToDboPopulator(KeywordToDboMapper keywordToDboMapper) {
        this.keywordToDboMapper = keywordToDboMapper;
    }

    @Override
    public void populate(KeywordBundle object, KeywordBundleDBO dbo) {
        dbo.id = object.id();
        dbo.groupBundleId = object.getGroupBundleId();
        dbo.timestamp = object.timestamp();
        dbo.title = object.title();
        dbo.keywords = new RealmList<>();
        for (Keyword keyword : object.keywords()) {
            dbo.keywords.add(keywordToDboMapper.map(keyword));
        }
    }
}
