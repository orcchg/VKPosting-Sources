package com.orcchg.vikstra.app.ui.viewobject;

import com.google.auto.value.AutoValue;
import com.orcchg.vikstra.domain.model.Keyword;

import java.util.List;

@AutoValue
public abstract class KeywordListItemVO {

    public static Builder builder() {
        return new AutoValue_KeywordListItemVO.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setTitle(String title);
        public abstract Builder setKeywords(List<Keyword> keywords);
        public abstract KeywordListItemVO build();
    }

    public abstract String title();
    public abstract List<Keyword> keywords();
}
