package com.orcchg.vikstra.domain.model.mapper;

public interface Populator<From, To> {
    void populate(From from, To to);
}
