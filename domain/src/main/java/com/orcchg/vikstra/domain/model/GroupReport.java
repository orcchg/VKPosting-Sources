package com.orcchg.vikstra.domain.model;

import com.google.auto.value.AutoValue;

import static com.vk.sdk.api.model.VKApiPhotoSize.P;

@AutoValue
public abstract class GroupReport {

    private int errorCode;

    public static Builder builder() {
        return new AutoValue_GroupReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
//        public abstract Builder setErrorCode(int errorCode);
        public abstract Builder setGroupId(long id);
        public abstract Builder setWallPostId(int id);
        public abstract GroupReport build();
    }

//    public abstract int errorCode();
    public abstract long groupId();
    public abstract int wallPostId();

    public int getErrorCode() {
        return errorCode;
    }
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
