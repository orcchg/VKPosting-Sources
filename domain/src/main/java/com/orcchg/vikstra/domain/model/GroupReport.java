package com.orcchg.vikstra.domain.model;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

import static com.vk.sdk.api.model.VKApiPhotoSize.P;

@AutoValue
public abstract class GroupReport implements Comparable<GroupReport> {

    public static Builder builder() {
        return new AutoValue_GroupReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(long id);
        public abstract Builder setErrorCode(int errorCode);
        public abstract Builder setGroupId(long id);
        public abstract Builder setTimestamp(long ts);
        public abstract Builder setWallPostId(int id);
        public abstract GroupReport build();
    }

    public abstract long id();
    public abstract int errorCode();
    public abstract long groupId();
    public abstract long timestamp();
    public abstract int wallPostId();

    @Override
    public int compareTo(@NonNull GroupReport o) {
        return (int) (o.timestamp() - timestamp());
    }
}
