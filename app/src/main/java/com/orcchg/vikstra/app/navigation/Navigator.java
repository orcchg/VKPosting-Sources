package com.orcchg.vikstra.app.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.FileProvider;
import android.view.View;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.ui.group.detail.GroupDetailActivity;
import com.orcchg.vikstra.app.ui.group.list.GroupListActivity;
import com.orcchg.vikstra.app.ui.keyword.create.KeywordCreateActivity;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListActivity;
import com.orcchg.vikstra.app.ui.legacy.details.DetailsActivity;
import com.orcchg.vikstra.app.ui.legacy.list.ListActivity;
import com.orcchg.vikstra.app.ui.legacy.tab.TabActivity;
import com.orcchg.vikstra.app.ui.post.create.PostCreateActivity;
import com.orcchg.vikstra.app.ui.post.list.PostListActivity;
import com.orcchg.vikstra.app.ui.post.view.PostViewActivity;
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

    public void openGroupListScreen(@NonNull Context context, long keywordBunldeId, long postId) {
        Intent intent = GroupListActivity.getCallingIntent(context, keywordBunldeId, postId);
        context.startActivity(intent);
    }

    /* Media */
    // ------------------------------------------
    @ExternalScreen
    public void openGallery(@NonNull Activity context) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivityForResult(intent, Constant.RequestCode.EXTERNAL_SCREEN_GALLERY);
        } else {
            Timber.e("No Activity found to open Gallery !");  // TODO: exception
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
            Timber.e("No Activity found to open Camera !");  // TODO: exception
        }
    }

    public void openSocialAlbumsScreen(@NonNull Activity context) {
        // TODO:
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

    // TODO remove completely
    /* Sample screens */
    // --------------------------------------------------------------------------------------------
    public void openListScreen(@NonNull Context context) {
        Intent intent = ListActivity.getCallingIntent(context);
        context.startActivity(intent);
    }

    public void openDetailsScreen(@NonNull Context context, long artistId, View view) {
        Intent intent = DetailsActivity.getCallingIntent(context, artistId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
            view != null && Activity.class.isInstance(context)) {
            Activity activity = (Activity) context;
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, "profile");
            context.startActivity(intent, options.toBundle());
        } else {
            context.startActivity(intent);
        }
    }

    public void openTabsScreen(@NonNull Context context) {
        Intent intent = TabActivity.getCallingIntent(context);
        context.startActivity(intent);
    }
}
