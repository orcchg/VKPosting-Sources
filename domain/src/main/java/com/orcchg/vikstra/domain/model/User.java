package com.orcchg.vikstra.domain.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class User {

    public static Builder builder() {
        return new AutoValue_User.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(long id);
        public abstract Builder setFirstName(String firstName);
        public abstract Builder setLastName(String lastName);
        public abstract Builder setPhotoUrl(String photoUrl);
        public abstract User build();
    }

    public abstract long id();
    public abstract String firstName();
    public abstract String lastName();
    public abstract String photoUrl();
}
