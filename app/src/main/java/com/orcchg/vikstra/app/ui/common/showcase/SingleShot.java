package com.orcchg.vikstra.app.ui.common.showcase;

import android.app.Activity;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.view.View;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.orcchg.vikstra.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

public class SingleShot {
    public static final int CASE_HIDE = -1;
    public static final int CASE_NEW_LISTS = 0;
    public static final int CASE_ADD_KEYWORD = 1;
    public static final int CASE_SELECT_POST = 2;
    public static final int CASE_MAKE_WALL_POSTING = 3;
    @IntDef({CASE_HIDE, CASE_NEW_LISTS, CASE_ADD_KEYWORD, CASE_SELECT_POST, CASE_MAKE_WALL_POSTING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ShowCase {}

    public static final int MAIN_SCREEN = 1026;
    public static final int GROUP_LIST_SCREEN = 2028;
    @IntDef({MAIN_SCREEN, GROUP_LIST_SCREEN})
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
                                           @ShowCase int showcase, @Screen int screen,
                                           OnShowcaseEventListener listener) {

        ViewTarget target = new ViewTarget(targetView);

        ShowcaseView.Builder svb = new ShowcaseView.Builder(activity)
                .withMaterialShowcase()
                .setShowcaseEventListener(listener)
                .setStyle(R.style.CustomShowcaseTheme)
                .setTarget(target)
                .singleShot(screen + showcase)
                .replaceEndButton(R.layout.custom_showcase_button);

        if (titleId != 0) svb.setContentTitle(titleId);
        if (descriptionId != 0) svb.setContentText(descriptionId);

        try {
            Field f = svb.getClass().getDeclaredField("showcaseView");
            f.setAccessible(true);
            ShowcaseView sv = (ShowcaseView) f.get(svb);
            sv.setTag(new ShowcaseTag(showcase, screen));
        } catch (NoSuchFieldException e) {
            //
        } catch (IllegalAccessException e) {
            //
        }

        return svb.build();
    }
}
