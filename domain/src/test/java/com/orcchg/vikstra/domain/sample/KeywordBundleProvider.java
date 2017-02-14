package com.orcchg.vikstra.domain.sample;

import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class KeywordBundleProvider {

    @Inject
    public KeywordBundleProvider() {
    }

    public KeywordBundle keywordBundle_oneWord() {
        List<Keyword> keywords = new ArrayList<>();
        keywords.add(Keyword.create("Universe"));
        return KeywordBundle.builder()
                .setId(1001)
                .setKeywords(keywords)
                .setTimestamp(1_456_789_101)
                .setTitle("Universe")
                .build();
    }

    public KeywordBundle keywordBundle_twoWords() {
        List<Keyword> keywords = new ArrayList<>();
        keywords.add(Keyword.create("Harry"));
        keywords.add(Keyword.create("Potter"));
        return KeywordBundle.builder()
                .setId(1002)
                .setKeywords(keywords)
                .setTimestamp(1_456_789_101)
                .setTitle("J.K. Rowling")
                .build();
    }

    public KeywordBundle keywordBundle_threeWords() {
        List<Keyword> keywords = new ArrayList<>();
        keywords.add(Keyword.create("Android"));
        keywords.add(Keyword.create("iOS"));
        keywords.add(Keyword.create("Windows"));
        return KeywordBundle.builder()
                .setId(1003)
                .setKeywords(keywords)
                .setTimestamp(1_456_789_101)
                .setTitle("Mobile platforms")
                .build();
    }

    public KeywordBundle keywordBundle_fourWords() {
        List<Keyword> keywords = new ArrayList<>();
        keywords.add(Keyword.create("Mojito"));
        keywords.add(Keyword.create("Espresso"));
        keywords.add(Keyword.create("Cappuccino"));
        keywords.add(Keyword.create("Glisse"));
        return KeywordBundle.builder()
                .setId(1004)
                .setKeywords(keywords)
                .setTimestamp(1_456_789_101)
                .setTitle("Coffee")
                .build();
    }

    public KeywordBundle keywordBundle_fiveWords() {
        List<Keyword> keywords = new ArrayList<>();
        keywords.add(Keyword.create("Maxim"));
        keywords.add(Keyword.create("Vladimir"));
        keywords.add(Keyword.create("Andrew"));
        keywords.add(Keyword.create("Sveta"));
        keywords.add(Keyword.create("Stanislav"));
        return KeywordBundle.builder()
                .setId(1005)
                .setKeywords(keywords)
                .setTimestamp(1_456_789_101)
                .setTitle("Names")
                .build();
    }

    public KeywordBundle keywordBundle_sixWords() {
        List<Keyword> keywords = new ArrayList<>();
        keywords.add(Keyword.create("Russia"));
        keywords.add(Keyword.create("Argentina"));
        keywords.add(Keyword.create("Korea"));
        keywords.add(Keyword.create("Japan"));
        keywords.add(Keyword.create("China"));
        keywords.add(Keyword.create("Usa"));
        return KeywordBundle.builder()
                .setId(1006)
                .setKeywords(keywords)
                .setTimestamp(1_456_789_101)
                .setTitle("Countries")
                .build();
    }

    public KeywordBundle keywordBundle_sevenWords() {
        List<Keyword> keywords = new ArrayList<>();
        keywords.add(Keyword.create("Salsa"));
        keywords.add(Keyword.create("Bachata"));
        keywords.add(Keyword.create("Kizomba"));
        keywords.add(Keyword.create("Semba"));
        keywords.add(Keyword.create("Merenge"));
        keywords.add(Keyword.create("Tango"));
        keywords.add(Keyword.create("Rueda"));
        return KeywordBundle.builder()
                .setId(1007)
                .setKeywords(keywords)
                .setTimestamp(1_456_789_101)
                .setTitle("Latin dances")
                .build();
    }

    // ------------------------------------------
    public List<KeywordBundle> keywordBundles() {
        List<KeywordBundle> list = new ArrayList<>();
        list.add(keywordBundle_oneWord());
        list.add(keywordBundle_twoWords());
        list.add(keywordBundle_threeWords());
        list.add(keywordBundle_fourWords());
        list.add(keywordBundle_fiveWords());
        list.add(keywordBundle_sixWords());
        list.add(keywordBundle_sevenWords());
        return list;
    }
}
