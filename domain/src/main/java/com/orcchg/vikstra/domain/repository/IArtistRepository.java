package com.orcchg.vikstra.domain.repository;

import com.orcchg.vikstra.domain.model.Artist;
import com.orcchg.vikstra.domain.model.TotalValue;

import java.util.List;

public interface IArtistRepository {
    List<Artist> artists();
    List<Artist> artists(int limit, int offset);
    List<Artist> artists(List<String> genres);
    List<Artist> artists(int limit, int offset, List<String> genres);
    Artist artist(long artistId);
    boolean clear();
    TotalValue total();
    TotalValue total(List<String> genres);
}
