package com.orcchg.vikstra.app.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

public interface MvpPresenter<V extends MvpView> {
    void attachView(V view);
    void detachView();

    void onCreate(@Nullable Bundle savedInstanceState);
    void onActivityResult(int requestCode, int resultCode, Intent data);
    void onStart();
    void onResume();
    void onPause();
    void onSaveInstanceState(Bundle outState);
    void onStop();
    void onDestroy();
}
