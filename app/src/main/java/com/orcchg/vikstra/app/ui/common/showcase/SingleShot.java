package com.orcchg.vikstra.app.ui.common.showcase;

import android.app.Activity;
import android.content.res.Resources;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.orcchg.vikstra.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

import timber.log.Timber;

public class SingleShot {
    public static final int CASE_HIDE = -1;
    public static final int CASE_NEW_LISTS = 0;
    public static final int CASE_ADD_KEYWORD = 1;
    public static final int CASE_SELECT_POST = 2;
    public static final int CASE_MAKE_WALL_POSTING = 3;
    public static final int CASE_DUMP_REPORT = 4;
    public static final int CASE_FILLED_LIST_POSTS = 5;
    public static final int CASE_FILLED_LIST_KEYWORDS = 6;
    @IntDef({
        CASE_HIDE,
        CASE_NEW_LISTS,
        CASE_ADD_KEYWORD,
        CASE_SELECT_POST,
        CASE_MAKE_WALL_POSTING,
        CASE_DUMP_REPORT,
        CASE_FILLED_LIST_POSTS,
        CASE_FILLED_LIST_KEYWORDS
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ShowCase {}

    public static final int MAIN_SCREEN = 10_005;
    public static final int GROUP_LIST_SCREEN = 20_005;
    public static final int REPORT_SCREEN = 30_005;
    @IntDef({MAIN_SCREEN, GROUP_LIST_SCREEN, REPORT_SCREEN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Screen {}

    // ------------------------------------------
    public static final class ShowcaseTag {
        private @ShowCase int showcase;
        private @Screen int screen;

        public ShowcaseTag(@ShowCase int showcase, @Screen int screen) {
            this.showcase = showcase;
            this.screen = screen;
        }

        @ShowCase
        public int showcase() {
            return showcase;
        }

        @Screen
        public int screen() {
            return screen;
        }
    }

    // ------------------------------------------
    public static ShowcaseView runShowcase(Activity activity, View targetView,
                                           @StringRes int titleId, @StringRes int descriptionId,
                                           @ShowCase int showcase, @Screen int screen, @LayoutRes int buttonStyle,
                                           OnShowcaseEventListener listener) {
        ViewTarget target = new ViewTarget(targetView);
        return runShowcase(activity, target, titleId, descriptionId, showcase, screen, buttonStyle, listener);
    }

    public static ShowcaseView runShowcase(Activity activity, ViewTarget target,
                                           @StringRes int titleId, @StringRes int descriptionId,
                                           @ShowCase int showcase, @Screen int screen, @LayoutRes int buttonStyle,
                                           OnShowcaseEventListener listener) {

        ShowcaseView.Builder svb = new ShowcaseView.Builder(activity)
                .withNewStyleShowcase()
                .setShowcaseEventListener(listener)
                .setStyle(R.style.CustomShowcaseTheme)
                .setTarget(target)
                .singleShot(screen + showcase)
                .hideOnTouchOutside()
                .replaceEndButton(buttonStyle);

        if (titleId != 0) svb.setContentTitle(titleId);
        if (descriptionId != 0) svb.setContentText(descriptionId);

        try {
            Field f = svb.getClass().getDeclaredField("showcaseView");
            f.setAccessible(true);
            ShowcaseView sv = (ShowcaseView) f.get(svb);
            sv.setTag(new ShowcaseTag(showcase, screen));
        } catch (NoSuchFieldException e) {
            Timber.e(e, "Reflection call");
        } catch (IllegalAccessException e) {
            Timber.e(e, "Reflection call");
        }

        return svb.build();
    }

    // ------------------------------------------
    public static RelativeLayout.LayoutParams moveButton(Resources resources) {
        RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        int margin = ((Number) (resources.getDisplayMetrics().density * 12)).intValue();
        lps.setMargins(margin, margin, margin, margin);
        return lps;
    }
}
