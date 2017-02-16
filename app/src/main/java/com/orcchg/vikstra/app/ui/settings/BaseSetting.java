package com.orcchg.vikstra.app.ui.settings;

import com.google.gson.Gson;

public abstract class BaseSetting {

    public abstract void copy(BaseSetting object);

    public abstract String getTag();

    public String toJson() {
        return new Gson().toJson(this);
    }

    public void fromJson(String json) {
        copy(new Gson().fromJson(json, this.getClass()));
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).append(": tag=").append(getTag()).toString();
    }
}
