package com.orcchg.vikstra.data.entity.mapper;

import com.orcchg.vikstra.domain.model.TotalValue;
import com.orcchg.vikstra.domain.model.mapper.Mapper;
import com.orcchg.vikstra.data.entity.TotalValueEntity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class TotalValueMapper implements Mapper<TotalValueEntity, TotalValue> {

    @Inject
    TotalValueMapper() {
    }

    @Override
    public TotalValue map(TotalValueEntity object) {
        return new TotalValue.Builder(object.getValue()).build();
    }

    @Override
    public List<TotalValue> map(List<TotalValueEntity> list) {
        List<TotalValue> mapped = new ArrayList<>();
        for (TotalValueEntity entity : list) {
            mapped.add(map(entity));
        }
        return mapped;
    }
}
