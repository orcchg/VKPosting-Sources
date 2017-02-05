package com.orcchg.vikstra.data.source.local.model.mapper;

import com.orcchg.vikstra.data.source.local.model.KeywordBundleDBO;
import com.orcchg.vikstra.data.source.local.model.KeywordDBO;
import com.orcchg.vikstra.data.source.local.model.populator.KeywordBundleToDboPopulator;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.model.mapper.DuplexMapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class KeywordBundleToDboMapper implements DuplexMapper<KeywordBundle, KeywordBundleDBO> {

    private final KeywordToDboMapper keywordToDboMapper;
    private final KeywordBundleToDboPopulator keywordBundleToDboPopulator;

    @Inject
    public KeywordBundleToDboMapper(KeywordToDboMapper keywordToDboMapper,
                                    KeywordBundleToDboPopulator keywordBundleToDboPopulator) {
        this.keywordToDboMapper = keywordToDboMapper;
        this.keywordBundleToDboPopulator = keywordBundleToDboPopulator;
    }

    /* Direct mapping */
    // ------------------------------------------
    @Override
    public KeywordBundleDBO map(KeywordBundle object) {
        KeywordBundleDBO dbo = new KeywordBundleDBO();
        keywordBundleToDboPopulator.populate(object, dbo);
        return dbo;
    }

    @Override
    public List<KeywordBundleDBO> map(List<KeywordBundle> list) {
        List<KeywordBundleDBO> mapped = new ArrayList<>();
        for (KeywordBundle item : list) {
            mapped.add(map(item));
        }
        return mapped;
    }

    /* Backward mapping */
    // ------------------------------------------
    @Override
    public KeywordBundle mapBack(KeywordBundleDBO object) {
        List<Keyword> keywords = new ArrayList<>();
        for (KeywordDBO dbo : object.keywords) {
            keywords.add(keywordToDboMapper.mapBack(dbo));
        }
        KeywordBundle bundle = KeywordBundle.builder()
                .setId(object.id)
                .setKeywords(keywords)
                .setTimestamp(object.timestamp)
                .setTitle(object.title)
                .build();
        bundle.setGroupBundleId(object.groupBundleId);
        bundle.setSelectedGroupsCount(object.countSelectedGroups);
        bundle.setTotalGroupsCount(object.countTotalGroups);
        return bundle;
    }

    @Override
    public List<KeywordBundle> mapBack(List<KeywordBundleDBO> list) {
        List<KeywordBundle> mapped = new ArrayList<>();
        for (KeywordBundleDBO item : list) {
            mapped.add(mapBack(item));
        }
        return mapped;
    }
}
