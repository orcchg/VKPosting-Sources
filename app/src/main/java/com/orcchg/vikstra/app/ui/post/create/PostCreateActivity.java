package com.orcchg.vikstra.app.ui.post.create;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.PermissionManager;
import com.orcchg.vikstra.app.ui.base.BaseActivity;
import com.orcchg.vikstra.app.ui.common.dialog.DialogProvider;
import com.orcchg.vikstra.app.ui.common.view.ThumbView;
import com.orcchg.vikstra.app.ui.post.create.injection.DaggerPostCreateComponent;
import com.orcchg.vikstra.app.ui.post.create.injection.PostCreateComponent;
import com.orcchg.vikstra.app.ui.post.create.injection.PostCreateModule;
import com.orcchg.vikstra.app.ui.util.UiUtility;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class PostCreateActivity extends BaseActivity<PostCreateContract.View, PostCreateContract.Presenter>
        implements PostCreateContract.View {
    private static final String EXTRA_POST_ID = "extra_post_id";
    public static final int REQUEST_CODE = Constant.RequestCode.POST_CREATE_SCREEN;

    private String SNACKBAR_MEDIA_ATTACH_LIMIT;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.et_post_description) AutoCompleteTextView postDescriptionEditText;
    @BindView(R.id.media_container_root) ViewGroup mediaContainerRoot;
    @BindView(R.id.media_container) ViewGroup mediaContainer;
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

    private PostCreateComponent postCreateComponent;
    private long postId = Constant.BAD_ID;

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
        initData();  // init data needed for injected dependencies
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_create);
        ButterKnife.bind(this);
        initResources();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        postDescriptionEditText.requestFocus();
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (granted) {
            switch (requestCode) {
                case PermissionManager.READ_EXTERNAL_STORAGE_REQUEST_CODE:
                    navigationComponent.navigator().openGallery(this);
                    break;
                case PermissionManager.WRITE_EXTERNAL_STORAGE_REQUEST_CODE:
                    navigationComponent.navigator().openCamera(this, true);
                    break;
            }
        } else {
            Timber.w("Permissions %s were not granted !", Arrays.toString(permissions));
        }
    }

    /* Data */
    // --------------------------------------------------------------------------------------------
    private void initData() {
        postId = getIntent().getLongExtra(EXTRA_POST_ID, Constant.BAD_ID);
    }

    /* View */
    // --------------------------------------------------------------------------------------------
    private void initView() {
        toolbar.setTitle(R.string.post_create_screen_title);
        toolbar.setNavigationOnClickListener((view) -> finish());
        toolbar.inflateMenu(R.menu.save);
        toolbar.setOnMenuItemClickListener((item) -> {
            switch (item.getItemId()) {
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
        UiUtility.showSnackbar(this, String.format(SNACKBAR_MEDIA_ATTACH_LIMIT, limit));
    }

    @Override
    public void showMediaLoadDialog() {
        DialogProvider.showUploadPhotoDialog(this);
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
    }

    // ------------------------------------------
    @Override
    public ContentResolver contentResolver() {
        return getContentResolver();
    }

    @Override
    public void closeView(int resultCode) {
        setResult(resultCode);
        finish();
    }

    @Override
    public void showError() {
        // TODO: show error
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private void addMediaThumbnail(ThumbView mediaView) {
        mediaView.setOnClickListener((view) -> {
            presenter.removeAttachedMedia();
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
