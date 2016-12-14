package com.orcchg.vikstra.app.ui.viewobject.mapper;

import com.orcchg.vikstra.app.ui.viewobject.MediaVO;
import com.orcchg.vikstra.domain.model.Media;
import com.orcchg.vikstra.domain.model.mapper.Mapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class MediaToVoMapper implements Mapper<Media, MediaVO> {

    @Inject
    public MediaToVoMapper() {
    }

    @Override
    public MediaVO map(Media object) {
        return MediaVO.builder().setUrl(object.url()).build();
    }

    @Override
    public List<MediaVO> map(List<Media> list) {
        List<MediaVO> mapped = new ArrayList<>();
        for (Media item : list) {
            mapped.add(map(item));
        }
        return mapped;
    }
}
