package com.orcchg.vikstra.app.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.ui.details.DetailsActivity;
import com.orcchg.vikstra.app.ui.group.list.GroupListActivity;
import com.orcchg.vikstra.app.ui.keyword.create.KeywordCreateActivity;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListActivity;
import com.orcchg.vikstra.app.ui.list.ListActivity;
import com.orcchg.vikstra.app.ui.post.create.PostCreateActivity;
import com.orcchg.vikstra.app.ui.post.view.PostViewActivity;
import com.orcchg.vikstra.app.ui.tab.TabActivity;
import com.orcchg.vikstra.domain.util.Constant;

import javax.inject.Inject;

@PerActivity
public class Navigator {

    @Inject
    public Navigator() {
    }

    /* Keywords */
    // ------------------------------------------
    public void openKeywordListScreen(@NonNull Context context) {
        Intent intent = KeywordListActivity.getCallingIntent(context);
        context.startActivity(intent);
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
    public void openGroupsListScreen(@NonNull Context context, long keywordBunldeId) {
        Intent intent = GroupListActivity.getCallingIntent(context, keywordBunldeId);
        context.startActivity(intent);
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
