package com.orcchg.vikstra.app.ui.viewobject;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.orcchg.vikstra.app.ui.common.content.ISelectableModel;

@AutoValue
public abstract class PostSingleGridItemVO implements ISelectableModel {

    protected boolean isSelected;

    public static PostSingleGridItemVO.Builder builder() {
        return new AutoValue_PostSingleGridItemVO.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(long id);
        public abstract Builder setDescription(String description);
        public abstract Builder setMedia(MediaVO media);
        public abstract Builder setMediaCount(int count);
        public abstract Builder setTitle(String title);
        public abstract PostSingleGridItemVO build();
    }

    @Override public abstract long id();
    public abstract @Nullable String description();
    public abstract @Nullable MediaVO media();
    public abstract int mediaCount();
    public abstract @Nullable String title();

    public boolean hasMedia() {
        return media() != null;
    }

    @Override public void setSelection(boolean isSelected) { this.isSelected = isSelected; }
    @Override public boolean getSelection() { return isSelected; }
}
