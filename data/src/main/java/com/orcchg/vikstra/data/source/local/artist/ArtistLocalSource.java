package com.orcchg.vikstra.data.source.local.artist;

import com.orcchg.vikstra.data.source.local.base.ICache;
import com.orcchg.vikstra.data.source.remote.artist.ArtistDataSource;

public interface ArtistLocalSource extends ArtistDataSource, ArtistRepository, ICache {
}
