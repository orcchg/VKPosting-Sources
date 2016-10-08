package com.orcchg.vikstra.data.source.local.genre;

import com.orcchg.vikstra.data.source.local.base.ICache;
import com.orcchg.vikstra.data.source.remote.genre.GenreDataSource;

public interface GenreLocalSource extends GenreDataSource, GenreRepository, ICache {
}
