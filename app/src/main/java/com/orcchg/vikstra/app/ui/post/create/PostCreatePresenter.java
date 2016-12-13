package com.orcchg.vikstra.app.ui.post.create;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.app.util.ContentUtility;
import com.orcchg.vikstra.domain.model.Media;
import com.orcchg.vikstra.domain.model.essense.PostEssense;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class PostCreatePresenter extends BasePresenter<PostCreateContract.View> implements PostCreateContract.Presenter {

    private List<Media> medias = new ArrayList<>();  // TODO: save instance state

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
                        Media media = Media.builder().setId(1000).setUrl(imagePath).build();  // TODO: unique id
                        medias.add(media);
                    }
                    cursor.close();
                }
                break;
            case Constant.RequestCode.EXTERNAL_SCREEN_CAMERA:
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                if (isViewAttached()) getView().addMediaThumbnail(thumbnail);
                String url = ContentUtility.InMemoryStorage.getLastStoredInternalImageUrl();
                ContentUtility.InMemoryStorage.setLastStoredInternalImageUrl(null);  // drop camera image url
                Media media = Media.builder().setId(1000).setUrl(url).build();  // TODO: unique id
                medias.add(media);
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
        if (isViewAttached()) {
            String title = null;  // TODO: title
            String description = getView().getInputText();

            // TODO: set location, file attach, poll
            PostEssense essense = PostEssense.builder()
                    .setDescription(description)
                    .setMedia(medias)
                    .setTitle(title)
                    .build();


        }
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
}
