package com.orcchg.vikstra.app.ui.viewobject;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PostSingleGridItemVO {

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

    public abstract long id();
    public abstract @Nullable String description();
    public abstract @Nullable MediaVO media();
    public abstract int mediaCount();
    public abstract @Nullable String title();

    public boolean hasMedia() {
        return media() != null;
    }

    public void setSelection(boolean isSelected) { this.isSelected = isSelected; }
    public boolean getSelection() { return isSelected; }
}
