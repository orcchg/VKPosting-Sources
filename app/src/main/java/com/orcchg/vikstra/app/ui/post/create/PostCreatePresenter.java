package com.orcchg.vikstra.app.ui.post.create;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.app.util.ContentUtility;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.post.GetPostById;
import com.orcchg.vikstra.domain.interactor.post.PostPost;
import com.orcchg.vikstra.domain.interactor.post.PutPost;
import com.orcchg.vikstra.domain.model.Media;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.essense.PostEssense;
import com.orcchg.vikstra.domain.model.essense.mapper.PostEssenseMapper;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class PostCreatePresenter extends BasePresenter<PostCreateContract.View> implements PostCreateContract.Presenter {

    private final GetPostById getPostByIdUseCase;
    private final PostPost postPostUseCase;
    private final PutPost putPostUseCase;

    private List<Media> attachMedia = new ArrayList<>();  // TODO: save instance state

    private long timestamp;
    private String description;
    private String title;

    @Inject
    PostCreatePresenter(GetPostById getPostByIdUseCase, PostPost postPostUseCase, PutPost putPostUseCase) {
        this.getPostByIdUseCase = getPostByIdUseCase;
        this.getPostByIdUseCase.setPostExecuteCallback(createGetPostByIdCallback());
        this.postPostUseCase = postPostUseCase;
        this.postPostUseCase.setPostExecuteCallback(createPostPostCallback());
        this.putPostUseCase = putPostUseCase;
        this.putPostUseCase.setPostExecuteCallback(createPutPostCallback());
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
                ContentResolver resolver = getView().contentResolver();
                Cursor cursor = resolver.query(uri, pathColums, null, null, null);
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(pathColums[0]);
                    String imagePath = cursor.getString(columnIndex);
                    Timber.d("Selected image from Gallery, url: %s", imagePath);
                    if (isViewAttached()) getView().addMediaThumbnail(imagePath);
                    Media media = Media.builder().setId(1000).setUrl(imagePath).build();  // TODO: unique id
                    attachMedia.add(media);
                }
                cursor.close();
                break;
            case Constant.RequestCode.EXTERNAL_SCREEN_CAMERA:
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                if (isViewAttached()) getView().addMediaThumbnail(thumbnail);
                String url = ContentUtility.InMemoryStorage.getLastStoredInternalImageUrl();
                ContentUtility.InMemoryStorage.setLastStoredInternalImageUrl(null);  // drop camera image url
                Media media = Media.builder().setId(1000).setUrl(url).build();  // TODO: unique id
                attachMedia.add(media);
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
    public void onMediaPressed() {
        if (isViewAttached()) {
            if (attachMedia.size() < Constant.MEDIA_ATTACH_LIMIT) {
                getView().showMediaLoadDialog();
            } else {
                getView().onMediaAttachLimitReached(Constant.MEDIA_ATTACH_LIMIT);
            }
        }
    }

    @Override
    public void onPollPressed() {
        //
    }

    @Override
    public void onSavePressed() {
        long postId = getPostByIdUseCase.getPostId();
        if (postId == Constant.BAD_ID) {
            if (isViewAttached()) description = getView().getInputText();
            title = "";  // TODO: set proper title; @Nullable
        }

        // TODO: set location, file attach, poll
        PostEssense essense = PostEssense.builder()
                .setDescription(description)
                .setMedia(attachMedia)
                .setTitle(title)
                .build();
        PostEssenseMapper mapper = new PostEssenseMapper(postId, timestamp);

        if (postId == Constant.BAD_ID) {
            // add new post to repository
            PutPost.Parameters parameters = new PutPost.Parameters(essense);
            putPostUseCase.setParameters(parameters);
            putPostUseCase.execute();
        } else {
            // update existing post in repository
            PostPost.Parameters parameters = new PostPost.Parameters(mapper.map(essense));
            postPostUseCase.setParameters(parameters);
            postPostUseCase.execute();
        }
    }

    // ------------------------------------------
    @Override
    public void removeAttachedMedia() {
        // TODO: removeAttachedMedia
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
        getPostByIdUseCase.execute();
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<Post> createGetPostByIdCallback() {
        return new UseCase.OnPostExecuteCallback<Post>() {
            @Override
            public void onFinish(@Nullable Post post) {
                // TODO: NPE
                if (post != null) {
                    description = post.description();
                    attachMedia.addAll(post.media());
                    timestamp = post.timestamp();
                    title = post.title();
                    // TODO: other fields is needed
                }
                // TODO: if updating existing post - fill text field and media attachment view container
                if (isViewAttached()) {
                    for (Media media : attachMedia) {
                        getView().addMediaThumbnail(media.url());
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError();
            }
        };
    }

    private UseCase.OnPostExecuteCallback<Boolean> createPostPostCallback() {
        return new UseCase.OnPostExecuteCallback<Boolean>() {
            @Override
            public void onFinish(@Nullable Boolean values) {
                if (isViewAttached()) getView().closeView(Activity.RESULT_OK);
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError();
            }
        };
    }

    private UseCase.OnPostExecuteCallback<Long> createPutPostCallback() {
        return new UseCase.OnPostExecuteCallback<Long>() {
            @Override
            public void onFinish(@Nullable Long postId) {
                if (isViewAttached()) getView().closeView(Activity.RESULT_OK);
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError();
            }
        };
    }
}
