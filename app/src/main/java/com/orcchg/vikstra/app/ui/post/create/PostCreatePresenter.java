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
import com.orcchg.vikstra.data.source.memory.ContentUtility;
import com.orcchg.vikstra.domain.exception.ProgramException;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.post.GetPostById;
import com.orcchg.vikstra.domain.interactor.post.PostPost;
import com.orcchg.vikstra.domain.interactor.post.PutPost;
import com.orcchg.vikstra.domain.model.Media;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.essense.PostEssence;
import com.orcchg.vikstra.domain.model.essense.mapper.PostEssenceMapper;
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
            Timber.d("Result from screen with request code %s is not OK", requestCode);
            return;
        }

        switch (requestCode) {
            case Constant.RequestCode.EXTERNAL_SCREEN_GALLERY:
                Uri uri = data.getData();
                String[] pathColumns = { MediaStore.Images.Media.DATA };
                ContentResolver resolver = getView().contentResolver();
                Cursor cursor = resolver.query(uri, pathColumns, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(pathColumns[0]);
                    String imagePath = cursor.getString(columnIndex);
                    Timber.d("Selected image from Gallery, url: %s", imagePath);
                    if (isViewAttached()) getView().addMediaThumbnail(imagePath);
                    Media media = Media.builder().setId(1000).setUrl(imagePath).build();  // TODO: unique id
                    attachMedia.add(media);
                } else if (cursor != null) {
                    cursor.close();
                }
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
    public void onBackPressed() {
        if (isViewAttached()) {
            if (hasChanges()) {
                getView().openSaveChangesDialog();
            } else {
                getView().closeView();
            }
        }
    }

    @Override
    public void onLocationPressed() {
        //
    }

    @Override
    public void onMediaPressed() {
        if (isViewAttached()) {
            if (attachMedia.size() < Constant.MEDIA_ATTACH_LIMIT) {
                getView().openMediaLoadDialog();
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
        String xtitle = title;  // initial title
        String xdescription = description;  // initial description
        long postId = getPostByIdUseCase.getPostId();

        if (isViewAttached()) {
            xdescription = getView().getInputText();
            // TODO: set proper title; @Nullable
        }

        // TODO: set location, file attach, poll
        PostEssence essence = PostEssence.builder()
                .setDescription(xdescription)
                .setMedia(attachMedia)
                .setTitle(xtitle)
                .build();
        PostEssenceMapper mapper = new PostEssenceMapper(postId, timestamp);

        if (postId == Constant.BAD_ID) {
            Timber.d("add new post to repository");
            PutPost.Parameters parameters = new PutPost.Parameters(essence);
            putPostUseCase.setParameters(parameters);
            putPostUseCase.execute();
        } else {
            Timber.d("update existing post in repository");
            PostPost.Parameters parameters = new PostPost.Parameters(mapper.map(essence));
            postPostUseCase.setParameters(parameters);
            postPostUseCase.execute();
        }
    }

    // ------------------------------------------
    @Override
    public void removeAttachedMedia() {
        // TODO: removeAttachedMedia
    }

    @Override
    public void retry() {
        freshStart();
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
        if (isViewAttached()) getView().showLoading(PostCreateActivity.RV_TAG);
        getPostByIdUseCase.execute();
    }

    private boolean hasChanges() {
        if (isViewAttached()) {
            boolean hasTextContentChanged = !getView().getInputText().equals(description);
            return hasTextContentChanged || !attachMedia.isEmpty();
        }
        return false;
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<Post> createGetPostByIdCallback() {
        return new UseCase.OnPostExecuteCallback<Post>() {
            @Override
            public void onFinish(@Nullable Post post) {
                long postId = getPostByIdUseCase.getPostId();
                if (postId != Constant.BAD_ID && post == null) {
                    Timber.e("Post wasn't found by id: %s", getPostByIdUseCase.getPostId());
                    throw new ProgramException();
                }
                if (post != null) {
                    Timber.d("Editing existing Post instance");
                    List<Media> media = post.media();
                    description = post.description();
                    if (media != null) attachMedia.addAll(media);
                    timestamp = post.timestamp();
                    title = post.title();
                    // TODO: other fields are needed
                    // TODO: if updating existing post - fill text field and media attachment view container
                    if (isViewAttached()) {
                        // TODO: set title to view
                        getView().setInputText(description);
                        getView().showContent(PostCreateActivity.RV_TAG, false);
                        for (Media item : attachMedia) {
                            getView().addMediaThumbnail(item.url());
                        }
                    }
                } else {
                    Timber.d("New Post instance will be created on this screen");
                    if (isViewAttached()) getView().showEmptyList(PostCreateActivity.RV_TAG);
                }
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError(PostCreateActivity.RV_TAG);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<Boolean> createPostPostCallback() {
        return new UseCase.OnPostExecuteCallback<Boolean>() {
            @Override
            public void onFinish(@Nullable Boolean result) {
                // TODO: result false - post not updated
                if (isViewAttached()) getView().closeView(Activity.RESULT_OK);
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError(PostCreateActivity.RV_TAG);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<Post> createPutPostCallback() {
        return new UseCase.OnPostExecuteCallback<Post>() {
            @Override
            public void onFinish(@Nullable Post post) {
                if (post == null) {
                    Timber.e("Failed to create new Post and put it to Repository");
                    throw new ProgramException();
                }
                if (isViewAttached()) getView().closeView(Activity.RESULT_OK);
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError(PostCreateActivity.RV_TAG);
            }
        };
    }
}
