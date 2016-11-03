package com.orcchg.vikstra.app.ui.viewobject.mapper;

import com.orcchg.vikstra.app.ui.viewobject.KeywordListItemVO;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.model.mapper.Mapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class KeywordListItemMapper implements Mapper<KeywordBundle, KeywordListItemVO> {

    @Inject
    public KeywordListItemMapper() {
    }

    @Override
    public KeywordListItemVO map(KeywordBundle object) {
        return KeywordListItemVO.builder()
                .setTitle(object.title())
                .setKeywords(object.keywords())
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
