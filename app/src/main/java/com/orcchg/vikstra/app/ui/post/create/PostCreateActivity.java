package com.orcchg.vikstra.app.ui.post.create;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.permission.BasePermissionActivity;
import com.orcchg.vikstra.app.ui.common.dialog.DialogProvider;
import com.orcchg.vikstra.app.ui.common.view.ThumbView;
import com.orcchg.vikstra.app.ui.post.OutConstants;
import com.orcchg.vikstra.app.ui.post.create.injection.DaggerPostCreateComponent;
import com.orcchg.vikstra.app.ui.post.create.injection.PostCreateComponent;
import com.orcchg.vikstra.app.ui.post.create.injection.PostCreateModule;
import com.orcchg.vikstra.app.ui.util.UiUtility;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hugo.weaving.DebugLog;
import timber.log.Timber;

import static com.orcchg.vikstra.R.id.view;

public class PostCreateActivity extends BasePermissionActivity<PostCreateContract.View, PostCreateContract.Presenter>
        implements PostCreateContract.View {
    private static final String BUNDLE_KEY_POST_ID = "bundle_key_post_id";
    private static final String EXTRA_POST_ID = "extra_post_id";
    public static final int REQUEST_CODE = Constant.RequestCode.POST_CREATE_SCREEN;
    public static final int RV_TAG = Constant.ListTag.POST_CREATE_SCREEN;

    private String SNACKBAR_MEDIA_ATTACH_LIMIT;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.container) ViewGroup container;
    @BindView(R.id.et_post_description) AutoCompleteTextView postDescriptionEditText;
    @BindView(R.id.media_container_root) ViewGroup mediaContainerRoot;
    @BindView(R.id.media_container) ViewGroup mediaContainer;
    @BindView(R.id.loading_view) View loadingView;
    @BindView(R.id.error_view) View errorView;
    @OnClick(R.id.ibtn_panel_location)
    void onLocationButtonClick() {
        presenter.onLocationPressed();
    }
    @OnClick(R.id.ibtn_panel_media)
    void onMediaButtonClick() {
        presenter.onMediaPressed();
    }
    @OnClick(R.id.ibtn_panel_attach)
    void onAttachButtonClick() {
        presenter.onAttachPressed();
    }
    @OnClick(R.id.ibtn_panel_poll)
    void onPollButtonClick() {
        presenter.onPollPressed();
    }
    @OnClick(R.id.ibtn_panel_link)
    void onLinkButtonClick() {
        presenter.onLinkPressed();
    }
    @OnClick(R.id.btn_retry)
    void onRetryClick() {
        presenter.retry();
    }

    private PostCreateComponent postCreateComponent;
    private long postId = Constant.BAD_ID;

    private @Nullable AlertDialog dialog1, dialog2, dialog3;

    @NonNull @Override
    protected PostCreateContract.Presenter createPresenter() {
        return postCreateComponent.presenter();
    }

    @Override
    protected void injectDependencies() {
        postCreateComponent = DaggerPostCreateComponent.builder()
                .applicationComponent(getApplicationComponent())
                .postCreateModule(new PostCreateModule(postId))
                .build();
        postCreateComponent.inject(this);
    }

    public static Intent getCallingIntent(@NonNull Context context) {
        return getCallingIntent(context, Constant.BAD_ID);
    }

    public static Intent getCallingIntent(@NonNull Context context, long postId) {
        Intent intent = new Intent(context, PostCreateActivity.class);
        intent.putExtra(EXTRA_POST_ID, postId);
        return intent;
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initData(savedInstanceState);  // init data needed for injected dependencies
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_create);
        ButterKnife.bind(this);
        initResources();
        initToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        postDescriptionEditText.requestFocus();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(BUNDLE_KEY_POST_ID, postId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog1 != null) dialog1.dismiss();
        if (dialog2 != null) dialog2.dismiss();
        if (dialog3 != null) dialog3.dismiss();
    }

    /* Data */
    // --------------------------------------------------------------------------------------------
    @DebugLog
    private void initData(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            postId = savedInstanceState.getLong(BUNDLE_KEY_POST_ID, Constant.BAD_ID);
        } else {
            postId = getIntent().getLongExtra(EXTRA_POST_ID, Constant.BAD_ID);
        }
        Timber.d("Post id: %s", postId);
    }

    /* Permissions */
    // ------------------------------------------
    @Override
    protected void onPermissionGranted_readExternalStorage() {
        navigationComponent.navigator().openGallery(this);
    }

    @Override
    protected void onPermissionGranted_writeExternalStorage() {
        navigationComponent.navigator().openCamera(this, true);
    }

    /* View */
    // --------------------------------------------------------------------------------------------
    private void initToolbar() {
        toolbar.setTitle(R.string.post_create_screen_title);
        toolbar.setNavigationOnClickListener((view) -> presenter.onBackPressed());
        toolbar.inflateMenu(R.menu.save);  // TODO: use view_save menu instead
//        toolbar.inflateMenu(R.menu.view_save);
        toolbar.setOnMenuItemClickListener((item) -> {
            switch (item.getItemId()) {
                case view:
                    // TODO: BAD_ID in PostView is not allowed, but should allow to preview posts
                    // TODO: currently under creation, so we should finish creation - save the post -
                    // TODO: obtaining it's id - and then open PostView. Or something else ?
                    navigationComponent.navigator().openPostViewScreen(this, postId);

                    // TODO: do not reload PostCreate on back from PostView in order to prevent from
                    // TODO: losing input data (text, media, etc...) for post under creation.
                    return true;
                case R.id.save:
                    presenter.onSavePressed();
                    return true;
            }
            return false;
        });
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void addMediaThumbnail(Bitmap bmp) {
        ThumbView mediaView = new ThumbView(this, ThumbView.SIZE_SMALL);
        mediaView.setImage(bmp);
        addMediaThumbnail(mediaView);
    }

    @Override
    public void addMediaThumbnail(String filePath) {
        ThumbView mediaView = new ThumbView(this, ThumbView.SIZE_SMALL);
        mediaView.setImageLocal(filePath);
        addMediaThumbnail(mediaView);
    }

    @Override
    public void onMediaAttachLimitReached(int limit) {
        UiUtility.showSnackbar(this, String.format(Locale.ENGLISH, SNACKBAR_MEDIA_ATTACH_LIMIT, limit));
    }

    // ------------------------------------------
    @Override
    public void openEditLinkDialog() {
        dialog1 = DialogProvider.showEditTextDialog(this, R.string.post_create_dialog_attach_link_title,
                R.string.post_create_dialog_attach_link_hint, "",
                (dialog, which, text) -> {
                    if (!TextUtils.isEmpty(text)) {
                        dialog.dismiss();
                        // TODO: attach link visually
                        presenter.attachLink(text);
                    }
                });
    }

    @Override
    public void openMediaLoadDialog() {
        dialog2 = DialogProvider.showUploadPhotoDialog(this);
    }

    @Override
    public void openSaveChangesDialog() {
        dialog3 = DialogProvider.showTextDialogTwoButtons(this, R.string.post_create_dialog_save_changes_title,
                R.string.post_create_dialog_save_changes_description, R.string.button_save, R.string.button_close,
                (dialog, which) -> {
                    dialog.dismiss();
                    presenter.onSavePressed();
                },
                (dialog, which) -> {
                    dialog.dismiss();
                    closeView(Activity.RESULT_CANCELED, postId);
                });
    }

    // ------------------------------------------
    @Override
    public int getThumbnailWidth() {
        return getResources().getDimensionPixelSize(R.dimen.standard_thumbnail_size_small);
    }

    @Override
    public int getThumbnailHeight() {
        return getResources().getDimensionPixelSize(R.dimen.standard_thumbnail_size_small);
    }

    // ------------------------------------------
    @Override
    public void clearInputText() {
        postDescriptionEditText.setText("");
    }

    @Override
    public String getInputText() {
        return postDescriptionEditText.getText().toString();
    }

    @Override
    public void setInputText(String text) {
        postDescriptionEditText.setText(text);
        postDescriptionEditText.setSelection(text.length());  // move cursor to the end of text
    }

    // ------------------------------------------
    @Override
    public ContentResolver contentResolver() {
        return getContentResolver();
    }

    @Override
    public void closeView() {
        finish();  // with currently set result
    }

    @Override
    public void closeView(int resultCode, long postId) {
        Intent data = new Intent();
        data.putExtra(OutConstants.OUT_EXTRA_POST_ID, postId);
        setResult(resultCode, data);
        finish();
    }

    // ------------------------------------------
    @Override
    public void showCreatePostFailure() {
        UiUtility.showSnackbar(this, R.string.post_create_snackbar_failed_to_create_post,
                Snackbar.LENGTH_LONG, R.string.button_retry, (view) -> presenter.retryCreatePost());
    }

    @Override
    public void showUpdatePostFailure() {
        UiUtility.showSnackbar(this, R.string.post_create_snackbar_failed_to_update_post,
                Snackbar.LENGTH_LONG, R.string.button_retry, (view) -> presenter.retryUpdatePost());
    }

    // ------------------------------------------
    @Override
    public boolean isContentViewVisible(int tag) {
        return true;  // always visible
    }

    @Override
    public void showContent(int tag, boolean isEmpty) {
        container.setVisibility(View.VISIBLE);
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
    }

    @Override
    public void showEmptyList(int tag) {
        showContent(tag, true);
    }

    @Override
    public void showError(int tag) {
        container.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showLoading(int tag) {
        container.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private void addMediaThumbnail(ThumbView mediaView) {
        final int position = mediaContainer.getChildCount();
        mediaView.setCornerIcon(R.drawable.ic_close_white_18dp);
        mediaView.setOnClickListener((view) -> {
            presenter.removeAttachedMedia(position);
            mediaContainer.removeView(mediaView);
            if (mediaContainer.getChildCount() == 0) mediaContainerRoot.setVisibility(View.GONE);
        });
        mediaContainerRoot.setVisibility(View.VISIBLE);
        mediaContainer.addView(mediaView);
    }

    /* Resources */
    // --------------------------------------------------------------------------------------------
    private void initResources() {
        SNACKBAR_MEDIA_ATTACH_LIMIT = getResources().getString(R.string.post_create_snackbar_media_attach_limit_message);
    }
}
