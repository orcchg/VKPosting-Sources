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
import com.orcchg.vikstra.domain.util.Constant;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class PostCreateActivity extends BaseActivity<PostCreateContract.View, PostCreateContract.Presenter>
        implements PostCreateContract.View {
    public static final int REQUEST_CODE = Constant.RequestCode.POST_CREATE_SCREEN;

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
        DialogProvider.showUploadPhotoDialog(this);
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

    @NonNull @Override
    protected PostCreateContract.Presenter createPresenter() {
        return postCreateComponent.presenter();
    }

    @Override
    protected void injectDependencies() {
        postCreateComponent = DaggerPostCreateComponent.builder()
                .applicationComponent(getApplicationComponent())
                .build();
        postCreateComponent.inject(this);
    }

    public static Intent getCallingIntent(@NonNull Context context) {
        Intent intent = new Intent(context, PostCreateActivity.class);
        return intent;
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_create);
        ButterKnife.bind(this);
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
    public void clearInputText() {
        postDescriptionEditText.setText("");
    }

    @Override
    public String getInputText() {
        return postDescriptionEditText.getText().toString();
    }

    @Override
    public ContentResolver contentResolver() {
        return getContentResolver();
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private void addMediaThumbnail(ThumbView mediaView) {
        mediaView.setOnClickListener((view) -> {
            mediaContainer.removeView(mediaView);
            if (mediaContainer.getChildCount() == 0) mediaContainerRoot.setVisibility(View.GONE);
        });
        mediaContainerRoot.setVisibility(View.VISIBLE);
        mediaContainer.addView(mediaView);
    }
}
