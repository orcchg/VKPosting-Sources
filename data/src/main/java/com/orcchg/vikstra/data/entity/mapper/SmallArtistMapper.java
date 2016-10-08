package com.orcchg.vikstra.data.entity.mapper;

import com.orcchg.vikstra.domain.model.Artist;
import com.orcchg.vikstra.domain.model.mapper.Mapper;
import com.orcchg.vikstra.data.entity.SmallArtistEntity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class SmallArtistMapper implements Mapper<SmallArtistEntity, Artist> {

    @Inject
    SmallArtistMapper() {
    }

    @Override
    public Artist map(SmallArtistEntity object) {
        return new Artist.Builder(object.getId(), object.getName())
                .setCoverSmall(object.getCover())
                .build();
    }

    @Override
    public List<Artist> map(List<SmallArtistEntity> list) {
        List<Artist> mapped = new ArrayList<>();
        for (SmallArtistEntity entity : list) {
            mapped.add(map(entity));
        }
        return mapped;
    }
}
