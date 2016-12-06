package com.orcchg.vikstra.domain.model.mapper;

public interface DuplexMapper<From, To> extends Mapper<From, To> {
    From mapBack(To object);
}
