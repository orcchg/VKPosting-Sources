package com.orcchg.vikstra.app.ui.viewobject;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UserVO {

    public static UserVO create(String photoUrl) {
        return new AutoValue_UserVO(photoUrl);
    }

    public abstract String photoUrl();
}
