package com.orcchg.vikstra.app.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
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
import com.orcchg.vikstra.app.ui.post.view.PostViewActivity;
import com.orcchg.vikstra.domain.util.Constant;

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

    public void openKeywordCreateScreen(@NonNull Activity context, long keywordBunldeId) {
        Intent intent = KeywordCreateActivity.getCallingIntent(context, keywordBunldeId);
        context.startActivityForResult(intent, KeywordCreateActivity.REQUEST_CODE);
    }

    /* Groups */
    // ------------------------------------------
    public void openGroupDetailScreen(@NonNull Context context, long groupId) {
        Intent intent = GroupDetailActivity.getCallingIntent(context, groupId);
        context.startActivity(intent);
    }

    public void openGroupListScreen(@NonNull Context context, long keywordBunldeId) {
        Intent intent = GroupListActivity.getCallingIntent(context, keywordBunldeId);
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
            int requestCode = fullSize ? Constant.RequestCode.EXTERNAL_SCREEN_CAMERA :
                    Constant.RequestCode.EXTERNAL_SCREEN_CAMERA_THUMBNAIL;
            context.startActivityForResult(intent, requestCode);
        } else {
            Timber.e("No Activity found to open Camera !");  // TODO: exception
        }
    }

    public void openSocialAlbumsScreen(@NonNull Activity context) {
        // TODO:
    }

    /* Posts */
    // ------------------------------------------
    public void openNewPostScreen(@NonNull Context context) {
        Intent intent = PostCreateActivity.getCallingIntent(context);
        context.startActivity(intent);
    }

    public void openPostViewScreen(@NonNull Context context) {
        Intent intent = PostViewActivity.getCallingIntent(context);
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
