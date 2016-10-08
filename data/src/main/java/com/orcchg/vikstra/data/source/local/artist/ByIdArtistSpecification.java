package com.orcchg.vikstra.data.source.local.artist;

import com.orcchg.vikstra.data.entity.ArtistEntity;

/**
 * Specification allows to query {@link ArtistEntity} items
 * from the storage by {@link ArtistEntity#id} value.
 */
class ByIdArtistSpecification implements ArtistSpecification {
    private final long id;

    ByIdArtistSpecification(long id) {
        this.id = id;
    }

    @Override
    public String getSelectionArgs() {
        return ArtistDatabaseContract.ArtistsTable.COLUMN_NAME_ID + " == " + Long.toString(id);
    }
}
