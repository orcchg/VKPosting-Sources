package com.orcchg.vikstra.app.ui.post.create;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.domain.util.Constant;

import javax.inject.Inject;

import timber.log.Timber;

public class PostCreatePresenter extends BasePresenter<PostCreateContract.View> implements PostCreateContract.Presenter {

    @Inject
    PostCreatePresenter() {
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            Timber.d("Result from screen with request code %s is not OK: " + requestCode);
            return;
        }

        switch (requestCode) {
            case Constant.RequestCode.EXTERNAL_SCREEN_GALLERY:
                Uri uri = data.getData();
                String[] pathColums = { MediaStore.Images.Media.DATA };
                if (isViewAttached()) {
                    ContentResolver resolver = getView().contentResolver();
                    Cursor cursor = resolver.query(uri, pathColums, null, null, null);
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(pathColums[0]);
                        String imagePath = cursor.getString(columnIndex);
                        Timber.d("Selected image from Gallery, url: %s", imagePath);
                        getView().addMediaThumbnail(imagePath);
                    }
                    cursor.close();
                }
                break;
            case Constant.RequestCode.EXTERNAL_SCREEN_CAMERA_THUMBNAIL:
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                if (isViewAttached()) getView().addMediaThumbnail(thumbnail);
                break;
        }
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onAttachPressed() {
        //
    }

    @Override
    public void onLocationPressed() {
        //
    }

    @Override
    public void onPollPressed() {
        //
    }

    @Override
    public void onSavePressed() {
        // TODO: impl
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
}
