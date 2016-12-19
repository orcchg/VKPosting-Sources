package com.orcchg.vikstra.app.ui.viewobject;

import com.google.auto.value.AutoValue;
import com.orcchg.vikstra.app.ui.common.content.ISelectableModel;
import com.orcchg.vikstra.domain.model.Keyword;

import java.util.Collection;

@AutoValue
public abstract class KeywordListItemVO implements ISelectableModel {

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

    @Override public abstract long id();
    public abstract Collection<Keyword> keywords();
    public abstract String title();

    @Override public void setSelection(boolean isSelected) { this.isSelected = isSelected; }
    @Override public boolean getSelection() { return isSelected; }
}
