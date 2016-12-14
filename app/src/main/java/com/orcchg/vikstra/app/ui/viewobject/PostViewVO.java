package com.orcchg.vikstra.app.ui.viewobject;

import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class PostViewVO {

    public static Builder builder() {
        return new AutoValue_PostViewVO.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setDescription(String description);
        public abstract Builder setMedia(List<MediaVO> media);
        public abstract PostViewVO build();
    }

    public abstract String description();
    public abstract List<MediaVO> media();
    // TODO: add other fields
}
