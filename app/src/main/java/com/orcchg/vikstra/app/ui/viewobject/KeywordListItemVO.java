package com.orcchg.vikstra.app.ui.viewobject;

import com.google.auto.value.AutoValue;
import com.orcchg.vikstra.domain.model.Keyword;

import java.util.Collection;

@AutoValue
public abstract class KeywordListItemVO {

    protected boolean isSelected;

    public static Builder builder() {
        return new AutoValue_KeywordListItemVO.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(long id);
        public abstract Builder setKeywords(Collection<Keyword> keywords);
        public abstract Builder setTitle(String title);
        public abstract KeywordListItemVO build();
    }

    public abstract long id();
    public abstract Collection<Keyword> keywords();
    public abstract String title();

    public void setSelection(boolean isSelected) { this.isSelected = isSelected; }
    public boolean getSelection() { return isSelected; }
}
