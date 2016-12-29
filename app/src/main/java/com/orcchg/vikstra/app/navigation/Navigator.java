package com.orcchg.vikstra.app.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.ui.common.dialog.DialogProvider;
import com.orcchg.vikstra.app.ui.group.detail.GroupDetailActivity;
import com.orcchg.vikstra.app.ui.group.list.activity.GroupListActivity;
import com.orcchg.vikstra.app.ui.keyword.create.KeywordCreateActivity;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListActivity;
import com.orcchg.vikstra.app.ui.post.create.PostCreateActivity;
import com.orcchg.vikstra.app.ui.post.list.PostListActivity;
import com.orcchg.vikstra.app.ui.post.view.PostViewActivity;
import com.orcchg.vikstra.app.ui.report.ReportActivity;
import com.orcchg.vikstra.app.util.ContentUtility;
import com.orcchg.vikstra.domain.util.Constant;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import timber.log.Timber;

@PerActivity
public class Navigator {

    @Inject
    public Navigator() {
    }

    /* Keywords */
    // ------------------------------------------
    public void openKeywordListScreen(@NonNull Activity context) {
        Intent intent = KeywordListActivity.getCallingIntent(context);
        context.startActivityForResult(intent, KeywordListActivity.REQUEST_CODE);
    }

    public void openKeywordCreateScreen(@NonNull Activity context) {
        openKeywordCreateScreen(context, Constant.BAD_ID);
    }

    public void openKeywordCreateScreen(@NonNull Activity context, long keywordBundleId) {
        Intent intent = KeywordCreateActivity.getCallingIntent(context, keywordBundleId);
        context.startActivityForResult(intent, KeywordCreateActivity.REQUEST_CODE);
    }

    /* Groups */
    // ------------------------------------------
    public void openGroupDetailScreen(@NonNull Context context, long groupId) {
        Intent intent = GroupDetailActivity.getCallingIntent(context, groupId);
        context.startActivity(intent);
    }

    public void openGroupListScreen(@NonNull Activity context, long keywordBunldeId, long postId) {
        Intent intent = GroupListActivity.getCallingIntent(context, keywordBunldeId, postId);
        context.startActivityForResult(intent, GroupListActivity.REQUEST_CODE);
    }

    /* Media */
    // ------------------------------------------
    @ExternalScreen
    public void openGallery(@NonNull Activity context) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivityForResult(intent, Constant.RequestCode.EXTERNAL_SCREEN_GALLERY);
        } else {
            Timber.e("No Activity was found to open Gallery !");
            DialogProvider.showTextDialog(context, R.string.dialog_error_title, R.string.error_external_screen_not_found_gallery);
        }
    }

    @ExternalScreen
    public void openCamera(@NonNull Activity context, boolean fullSize) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            if (fullSize) {
                try {
                    File imageFile = ContentUtility.createInternalImageFile(context);
                    ContentUtility.InMemoryStorage.setLastStoredInternalImageUrl(imageFile.getAbsolutePath());
                    Uri uri = FileProvider.getUriForFile(context, ContentUtility.getFileProviderAuthority(), imageFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                } catch (IOException e) {
                    Timber.e(e, "Failed to create file for internal image !");
                    return;
                }
            }
            context.startActivityForResult(intent, Constant.RequestCode.EXTERNAL_SCREEN_CAMERA);
        } else {
            Timber.e("No Activity was found to open Camera !");
            DialogProvider.showTextDialog(context, R.string.dialog_error_title, R.string.error_external_screen_not_found_camera);
        }
    }

    public void openSocialAlbumsScreen(@NonNull Activity context) {
        // TODO: openSocialAlbumsScreen
    }

    /* Posts */
    // ------------------------------------------
    public void openPostCreateScreen(@NonNull Activity context) {
        openPostCreateScreen(context, Constant.BAD_ID);
    }

    public void openPostCreateScreen(@NonNull Activity context, long postId) {
        Intent intent = PostCreateActivity.getCallingIntent(context, postId);
        context.startActivityForResult(intent, PostCreateActivity.REQUEST_CODE);
    }

    public void openPostListScreen(@NonNull Activity context) {
        Intent intent = PostListActivity.getCallingIntent(context);
        context.startActivityForResult(intent, PostListActivity.REQUEST_CODE);
    }

    public void openPostViewScreen(@NonNull Context context, long postId) {
        Intent intent = PostViewActivity.getCallingIntent(context, postId);
        context.startActivity(intent);
    }

    /* Report */
    // ------------------------------------------
    public void openReportScreen(@NonNull Context context, long groupReportBundleId, long postId) {
        Intent intent = ReportActivity.getCallingIntent(context, groupReportBundleId, postId);
        context.startActivity(intent);
    }
}
