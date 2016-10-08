package com.orcchg.vikstra.data.source.remote.artist;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.data.entity.ArtistEntity;
import com.orcchg.vikstra.data.entity.SmallArtistEntity;
import com.orcchg.vikstra.data.entity.TotalValueEntity;

import java.util.List;

public interface ArtistDataSource {

    List<SmallArtistEntity> artists();
    List<SmallArtistEntity> artists(int limit, int offset);
    List<SmallArtistEntity> artists(@Nullable List<String> genres);
    List<SmallArtistEntity> artists(int limit, int offset, @Nullable List<String> genres);

    @Nullable
    ArtistEntity artist(long artistId);

    TotalValueEntity total();
    TotalValueEntity total(@Nullable List<String> genres);
}
