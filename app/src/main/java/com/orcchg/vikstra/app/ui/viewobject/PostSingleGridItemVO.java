package com.orcchg.vikstra.app.ui.viewobject;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PostSingleGridItemVO {

    public static PostSingleGridItemVO.Builder builder() {
        return new AutoValue_PostSingleGridItemVO.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(long id);
        public abstract Builder setMedia(MediaVO media);
        public abstract PostSingleGridItemVO build();
    }

    public abstract long id();
    public abstract MediaVO media();
}
