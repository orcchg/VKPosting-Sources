package com.orcchg.vikstra.app.ui.post.create;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
    private static final int PrID = Constant.PresenterId.POST_CREATE_PRESENTER;

    private final GetPostById getPostByIdUseCase;
    private final PostPost postPostUseCase;
    private final PutPost putPostUseCase;

    private Memento memento = new Memento();

    private int thumbnailWidth, thumbnailHeight;

    // --------------------------------------------------------------------------------------------
    private static final class Memento {
        private static final String BUNDLE_KEY_ATTACH_LINK = "bundle_key_attach_link_" + PrID;
        private static final String BUNDLE_KEY_ATTACH_MEDIA = "bundle_key_attach_media_" + PrID;
        private static final String BUNDLE_KEY_HAS_ATTACH_CHANGED = "bundle_key_has_attach_changed_" + PrID;
        private static final String BUNDLE_KEY_INPUT_POST = "bundle_key_input_post_" + PrID;

        private String attachLink;
        private List<Media> attachMedia = new ArrayList<>();
        private boolean hasAttachChanged;
        private @Nullable Post inputPost;

        @DebugLog
        private void toBundle(Bundle outState) {
            outState.putString(BUNDLE_KEY_ATTACH_LINK, attachLink);
            if (ArrayList.class.isInstance(attachMedia)) {
                outState.putParcelableArrayList(BUNDLE_KEY_ATTACH_MEDIA, (ArrayList<Media>) attachMedia);
            } else {
                ArrayList<Media> copyAttachMedia = new ArrayList<>(attachMedia);
                outState.putParcelableArrayList(BUNDLE_KEY_ATTACH_MEDIA, copyAttachMedia);
            }
            outState.putBoolean(BUNDLE_KEY_HAS_ATTACH_CHANGED, hasAttachChanged);
            outState.putParcelable(BUNDLE_KEY_INPUT_POST, inputPost);
        }

        @DebugLog
        private static Memento fromBundle(Bundle savedInstanceState) {
            Memento memento = new Memento();
            memento.attachLink = savedInstanceState.getString(BUNDLE_KEY_ATTACH_LINK);
            memento.attachMedia = savedInstanceState.getParcelableArrayList(BUNDLE_KEY_ATTACH_MEDIA);
            if (memento.attachMedia == null) memento.attachMedia = new ArrayList<>();
            memento.hasAttachChanged = savedInstanceState.getBoolean(BUNDLE_KEY_HAS_ATTACH_CHANGED);
            memento.inputPost = savedInstanceState.getParcelable(BUNDLE_KEY_INPUT_POST);
            return memento;
        }
    }

    // --------------------------------------------------------------------------------------------
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
                    memento.attachMedia.add(media);
                    memento.hasAttachChanged = true;
                    cursor.close();
                } else if (cursor != null) {
                    cursor.close();
                }
                break;
            case Constant.RequestCode.EXTERNAL_SCREEN_CAMERA:
                String url = ContentUtility.InMemoryStorage.getLastStoredInternalImageUrl();
                ContentUtility.InMemoryStorage.setLastStoredInternalImageUrl(null);  // drop camera image url
                Timber.i("Received result image from Camera, url: %s", url);
                Bitmap thumbnail;
                if (data != null && data.getExtras() != null) {
                    thumbnail = (Bitmap) data.getExtras().get("data");
                } else {
                    thumbnail = UiUtility.getBitmapFromFile(url, thumbnailWidth, thumbnailHeight);
                }
                if (isViewAttached()) getView().addMediaThumbnail(thumbnail);
                Media media = Media.builder().setId(1000).setUrl(url).build();  // TODO: unique id
                memento.attachMedia.add(media);
                memento.hasAttachChanged = true;
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        memento.toBundle(outState);
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void attachLink(String link) {
        Timber.i("attachLink: %s", link);
        memento.attachLink = link;
        memento.hasAttachChanged = true;
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
            if (memento.attachMedia.size() < Constant.MEDIA_ATTACH_LIMIT) {
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
        String description = memento.inputPost != null ? memento.inputPost.description() : "";
        String title = memento.inputPost != null ? memento.inputPost.title() : "";
        long postId = getPostByIdUseCase.getPostId();

        if (isViewAttached()) {
            description = getView().getInputText();
            // TODO: set proper title; @Nullable
        }

        // TODO: set location, file attach, poll
        PostEssence essence = PostEssence.builder()
                .setDescription(description)
                .setMedia(memento.attachMedia)
                .setTitle(title)
                .build();

        if (postId == Constant.BAD_ID) {
            Timber.d("Input Post id is BAD - add new Post instance to repository");
            PutPost.Parameters parameters = new PutPost.Parameters(essence);
            putPostUseCase.setParameters(parameters);
            putPostUseCase.execute();
        } else {
            Timber.d("Input Post id is [%s] - update already existing Post instance in repository", postId);
            PostEssenceMapper mapper = new PostEssenceMapper(postId, memento.inputPost.timestamp());
            PostPost.Parameters parameters = new PostPost.Parameters(mapper.map(essence));
            postPostUseCase.setParameters(parameters);
            postPostUseCase.execute();
        }
    }

    // ------------------------------------------
    @Override
    public void removeAttachedMedia(int position) {
        Timber.i("removeAttachedMedia: %s", position);
        memento.attachMedia.remove(position);
        memento.hasAttachChanged = true;
    }

    @Override
    public void retry() {
        Timber.i("retry");
        memento.hasAttachChanged = false;
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

    @Override
    protected void onRestoreState() {
        memento = Memento.fromBundle(savedInstanceState);
        if (memento.inputPost != null) populatePost(memento.inputPost);
        populateMedia(memento.attachMedia);
    }

    @DebugLog
    private boolean hasChanges() {
        if (isViewAttached()) {
            String description = memento.inputPost != null ? memento.inputPost.description() : "";
            boolean hasTextContentChanged = !getView().getInputText().equals(description);
            return hasTextContentChanged || memento.hasAttachChanged;
        }
        return false;
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<Post> createGetPostByIdCallback() {
        return new UseCase.OnPostExecuteCallback<Post>() {
            @DebugLog @Override
            public void onFinish(@Nullable Post post) {
                memento.inputPost = post;
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
                        memento.attachMedia.clear();
                        memento.attachMedia.addAll(media);
                    }
                    populatePost(post);
                    populateMedia(memento.attachMedia);
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
                memento.hasAttachChanged = false;  // changes has been saved
                /**
                 * Generally, {@link memento.inputPost} can'not be null here in this callback, because
                 * we are updating an existing {@link Post} recorder to {@link memento.inputPost} field.
                 * But if it is null - then there is a bug in implementation. But in favor of safety
                 * we close the view with {@link Activity#RESULT_OK} and {@link Constant.BAD_ID} Post id,
                 * assuming that receiver will handle it properly.
                 */
                long postId = memento.inputPost != null ? memento.inputPost.id() : Constant.BAD_ID;
                if (isViewAttached()) getView().closeView(Activity.RESULT_OK, postId);
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
                memento.hasAttachChanged = false;  // changes has been saved
                if (isViewAttached()) getView().closeView(Activity.RESULT_OK, post.id());
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to put Post");
                if (isViewAttached()) getView().showCreatePostFailure();
            }
        };
    }

    /* Utility */
    // --------------------------------------------------------------------------------------------
    /**
     * {@param Post} has actually the same {@param media} data inside, but we use this as an additional
     * method parameter just because we don't want to check it for null.
     */
    private void populatePost(@NonNull Post post) {
        // TODO: other fields are needed
        // TODO: if updating existing post - fill text field and media attachment view container
        if (isViewAttached()) {
            // TODO: set title to view
            getView().setInputText(post.description());
            getView().showContent(PostCreateActivity.RV_TAG, false);

        }
    }

    private void populateMedia(@NonNull List<Media> media) {
        Timber.v("Attach media size: %s", memento.attachMedia.size());
        if (isViewAttached()) {
            for (Media item : media) {
                getView().addMediaThumbnail(item.url());
            }
        }
    }
}
