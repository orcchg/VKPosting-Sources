package com.orcchg.vikstra.app.ui.viewobject.mapper;

import com.orcchg.vikstra.app.ui.viewobject.KeywordListItemVO;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.model.mapper.Mapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class KeywordBundleToVoMapper implements Mapper<KeywordBundle, KeywordListItemVO> {

    @Inject
    public KeywordBundleToVoMapper() {
    }

    @Override
    public KeywordListItemVO map(KeywordBundle object) {
        return KeywordListItemVO.builder()
                .setId(object.id())
                .setKeywords(object.keywords())
                .setTitle(object.title())
                .build();
    }

    @Override
    public List<KeywordListItemVO> map(List<KeywordBundle> list) {
        List<KeywordListItemVO> mapped = new ArrayList<>();
        for (KeywordBundle item : list) {
            mapped.add(map(item));
        }
        return mapped;
    }
}
