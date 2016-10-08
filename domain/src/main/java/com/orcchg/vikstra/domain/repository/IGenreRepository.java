package com.orcchg.vikstra.domain.repository;

import com.orcchg.vikstra.domain.model.Genre;
import com.orcchg.vikstra.domain.model.TotalValue;

import java.util.List;

public interface IGenreRepository {
    List<Genre> genres();
    Genre genre(String name);
    boolean clear();
    TotalValue total();
}
