package com.orcchg.vikstra.app.ui.post.create;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.app.ui.util.UiUtility;
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

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class PostCreatePresenter extends BasePresenter<PostCreateContract.View> implements PostCreateContract.Presenter {

    private final GetPostById getPostByIdUseCase;
    private final PostPost postPostUseCase;
    private final PutPost putPostUseCase;

    private String attachLink;
    private List<Media> attachMedia = new ArrayList<>();  // TODO: save instance state
    private boolean hasAttachChanged;

    private @Nullable Post inputPost;

    private int thumbnailWidth, thumbnailHeight;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isViewAttached()) {
            thumbnailWidth  = getView().getThumbnailWidth();
            thumbnailHeight = getView().getThumbnailHeight();
        }
    }

    @DebugLog @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            Timber.d("Result from screen with request code [%s] is not OK", requestCode);
            return;
        }

        switch (requestCode) {
            case Constant.RequestCode.EXTERNAL_SCREEN_GALLERY:
                Timber.i("Received result image from Gallery");
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
                    hasAttachChanged = true;
                } else if (cursor != null) {
                    cursor.close();
                }
                break;
            case Constant.RequestCode.EXTERNAL_SCREEN_CAMERA:
                String url = ContentUtility.InMemoryStorage.getLastStoredInternalImageUrl();
                ContentUtility.InMemoryStorage.setLastStoredInternalImageUrl(null);  // drop camera image url
                Timber.i("Received result image from Camera, url: %s", url);
                Bitmap thumbnail = null;
                if (data != null && data.getExtras() != null) {
                    thumbnail = (Bitmap) data.getExtras().get("data");
                } else {
                    thumbnail = UiUtility.getBitmapFromFile(url, thumbnailWidth, thumbnailHeight);
                }
                if (isViewAttached()) getView().addMediaThumbnail(thumbnail);
                Media media = Media.builder().setId(1000).setUrl(url).build();  // TODO: unique id
                attachMedia.add(media);
                hasAttachChanged = true;
                break;
        }
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void attachLink(String link) {
        Timber.i("attachLink: %s", link);
        attachLink = link;
        hasAttachChanged = true;
    }

    // ------------------------------------------
    @Override
    public void onAttachPressed() {
        Timber.i("onAttachPressed");
        // TODO: onAttachPressed
    }

    @Override
    public void onBackPressed() {
        Timber.i("onBackPressed");
        if (isViewAttached()) {
            if (hasChanges()) {
                getView().openSaveChangesDialog();
            } else {
                getView().closeView();
            }
        }
    }

    @Override
    public void onLinkPressed() {
        Timber.i("onLinkPressed");
        if (isViewAttached()) getView().openEditLinkDialog();
    }

    @Override
    public void onLocationPressed() {
        Timber.i("onLocationPressed");
        // TODO: onLocationPressed
    }

    @Override
    public void onMediaPressed() {
        Timber.i("onMediaPressed");
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
        Timber.i("onPollPressed");
        // TODO: onPollPressed
    }

    @Override
    public void onSavePressed() {
        Timber.i("onSavePressed");
        String description = inputPost != null ? inputPost.description() : "";
        String title = inputPost != null ? inputPost.title() : "";
        long postId = getPostByIdUseCase.getPostId();

        if (isViewAttached()) {
            description = getView().getInputText();
            // TODO: set proper title; @Nullable
        }

        // TODO: set location, file attach, poll
        PostEssence essence = PostEssence.builder()
                .setDescription(description)
                .setMedia(attachMedia)
                .setTitle(title)
                .build();

        if (postId == Constant.BAD_ID) {
            Timber.d("Input Post id is BAD - add new Post instance to repository");
            PutPost.Parameters parameters = new PutPost.Parameters(essence);
            putPostUseCase.setParameters(parameters);
            putPostUseCase.execute();
        } else {
            Timber.d("Input Post id is [%s] - update already existing Post instance in repository", postId);
            PostEssenceMapper mapper = new PostEssenceMapper(postId, inputPost.timestamp());
            PostPost.Parameters parameters = new PostPost.Parameters(mapper.map(essence));
            postPostUseCase.setParameters(parameters);
            postPostUseCase.execute();
        }
    }

    // ------------------------------------------
    @Override
    public void removeAttachedMedia(int position) {
        Timber.i("removeAttachedMedia: %s", position);
        attachMedia.remove(position);
        hasAttachChanged = true;
    }

    @Override
    public void retry() {
        Timber.i("retry");
        hasAttachChanged = false;
        freshStart();
    }

    @Override
    public void retryCreatePost() {
        Timber.i("retryCreatePost");
        putPostUseCase.execute();  // parameters have been already set at previous failed attempt
    }

    @Override
    public void retryUpdatePost() {
        Timber.i("retryUpdatePost");
        postPostUseCase.execute();  // parameters have been already set at previous failed attempt
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
        if (isViewAttached()) getView().showLoading(PostCreateActivity.RV_TAG);
        getPostByIdUseCase.execute();
    }

    @DebugLog
    private boolean hasChanges() {
        if (isViewAttached()) {
            String description = inputPost != null ? inputPost.description() : "";
            boolean hasTextContentChanged = !getView().getInputText().equals(description);
            return hasTextContentChanged || hasAttachChanged;
        }
        return false;
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<Post> createGetPostByIdCallback() {
        return new UseCase.OnPostExecuteCallback<Post>() {
            @DebugLog @Override
            public void onFinish(@Nullable Post post) {
                inputPost = post;
                long postId = getPostByIdUseCase.getPostId();
                if (postId != Constant.BAD_ID && post == null) {
                    Timber.e("Post wasn't found by id: %s", postId);
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to get Post by id");
                if (post != null) {
                    Timber.d("Existing Post with id [%s] will be updated on PostCreateScreen", postId);
                    List<Media> media = post.media();
                    if (media != null) {
                        /**
                         * Persist all attached media supplied with loaded Post in 'attachMedia' list.
                         *
                         * This will override any media added by user, but this can'not be the case,
                         * because re-loading could only occur on retry, and the other requests such as
                         * update (POST) and create (PUT) have their own success-failure pipeline, which
                         * doesn't involve standard retry invocation (and Post re-loading).
                         */
                        attachMedia.clear();
                        attachMedia.addAll(media);
                    }
                    // TODO: other fields are needed
                    // TODO: if updating existing post - fill text field and media attachment view container
                    if (isViewAttached()) {
                        // TODO: set title to view
                        getView().setInputText(post.description());
                        getView().showContent(PostCreateActivity.RV_TAG, false);
                        for (Media item : attachMedia) {
                            getView().addMediaThumbnail(item.url());
                        }
                    }
                } else {  // post is null and id is BAD
                    Timber.d("New Post instance will be created on PostCreateScreen");
                    if (isViewAttached()) getView().showEmptyList(PostCreateActivity.RV_TAG);
                }
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get Post by id");
                if (isViewAttached()) getView().showError(PostCreateActivity.RV_TAG);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<Boolean> createPostPostCallback() {
        return new UseCase.OnPostExecuteCallback<Boolean>() {
            @DebugLog @Override
            public void onFinish(@Nullable Boolean result) {
                if (result == null || !result) {
                    Timber.e("Failed to update Post in repository - item not found by correct id, as expected");
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to post Post");
                hasAttachChanged = false;  // changes has been saved
                if (isViewAttached()) getView().closeView(Activity.RESULT_OK, inputPost.id());
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to post Post");
                if (isViewAttached()) getView().showUpdatePostFailure();
            }
        };
    }

    private UseCase.OnPostExecuteCallback<Post> createPutPostCallback() {
        return new UseCase.OnPostExecuteCallback<Post>() {
            @DebugLog @Override
            public void onFinish(@Nullable Post post) {
                if (post == null) {
                    Timber.e("Failed to put new Post to repository - item not created, as expected");
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to put Post");
                hasAttachChanged = false;  // changes has been saved
                if (isViewAttached()) getView().closeView(Activity.RESULT_OK, post.id());
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to put Post");
                if (isViewAttached()) getView().showCreatePostFailure();
            }
        };
    }
}
