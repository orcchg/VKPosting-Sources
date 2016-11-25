package com.orcchg.vikstra.data.source.repository.keyword;

import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.repository.IKeywordRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class KeywordRepositoryImpl implements IKeywordRepository {

    @Inject
    KeywordRepositoryImpl() {
    }

    @Override
    public List<KeywordBundle> keywords() {
        return keywords(-1, 0);
    }

    @Override
    public List<KeywordBundle> keywords(int limit, int offset) {
        // TODO: real impl
        List<KeywordBundle> mockData = new ArrayList<>();

        List<Keyword> music = new ArrayList<>();
        music.add(Keyword.create("Timbaland"));
        music.add(Keyword.create("Jodi Foster"));
        music.add(Keyword.create("Dima Bilan"));
        music.add(Keyword.create("Mark Aurelis"));
        music.add(Keyword.create("Sandro Sanders"));
        mockData.add(KeywordBundle.builder().setId(1000).setTitle("Music").setKeywords(music).build());

        List<Keyword> travel = new ArrayList<>();
        travel.add(Keyword.create("South Korea"));
        travel.add(Keyword.create("Finland"));
        travel.add(Keyword.create("Denmark"));
        travel.add(Keyword.create("Russia"));
        travel.add(Keyword.create("Scandinavia"));
        mockData.add(KeywordBundle.builder().setId(1000).setTitle("Travel").setKeywords(travel).build());

        List<Keyword> sport = new ArrayList<>();
        sport.add(Keyword.create("Iron Man"));
        sport.add(Keyword.create("Formula One"));
        sport.add(Keyword.create("Tennis"));
        sport.add(Keyword.create("Hockey"));
        sport.add(Keyword.create("Footbal"));
        mockData.add(KeywordBundle.builder().setId(1000).setTitle("Sport").setKeywords(sport).build());

        List<Keyword> movies = new ArrayList<>();
        movies.add(Keyword.create("Hobbit"));
        movies.add(Keyword.create("The Matrix"));
        movies.add(Keyword.create("Lord of the Rings"));
        movies.add(Keyword.create("Doctor Strange"));
        movies.add(Keyword.create("Ride for 60 sec"));
        mockData.add(KeywordBundle.builder().setId(1000).setTitle("Movies").setKeywords(movies).build());

        List<Keyword> cars = new ArrayList<>();
        cars.add(Keyword.create("VW gti"));
        cars.add(Keyword.create("Chevrolet"));
        cars.add(Keyword.create("Lamborgini"));
        cars.add(Keyword.create("Camarro"));
        cars.add(Keyword.create("Opel Astra"));
        mockData.add(KeywordBundle.builder().setId(1000).setTitle("Cars").setKeywords(cars).build());

        return mockData;
    }
}
