package com.orcchg.vikstra.data.source.remote.genre;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.data.entity.GenreEntity;
import com.orcchg.vikstra.data.entity.TotalValueEntity;

import java.util.List;

public interface GenreDataSource {

    List<GenreEntity> genres();

    @Nullable
    GenreEntity genre(String name);

    TotalValueEntity total();
}
