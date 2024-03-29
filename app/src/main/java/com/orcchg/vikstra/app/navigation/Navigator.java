package com.orcchg.vikstra.app.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.ui.common.dialog.DialogProvider;
import com.orcchg.vikstra.app.ui.dialog.DialogActivity;
import com.orcchg.vikstra.app.ui.group.detail.GroupDetailActivity;
import com.orcchg.vikstra.app.ui.group.list.activity.GroupListActivity;
import com.orcchg.vikstra.app.ui.keyword.create.KeywordCreateActivity;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListActivity;
import com.orcchg.vikstra.app.ui.main.MainActivity;
import com.orcchg.vikstra.app.ui.main.StartActivity;
import com.orcchg.vikstra.app.ui.post.create.PostCreateActivity;
import com.orcchg.vikstra.app.ui.post.list.PostListActivity;
import com.orcchg.vikstra.app.ui.post.view.PostViewActivity;
import com.orcchg.vikstra.app.ui.report.history.ReportHistoryActivity;
import com.orcchg.vikstra.app.ui.report.main.ReportActivity;
import com.orcchg.vikstra.app.ui.report.service.WallPostingService;
import com.orcchg.vikstra.app.ui.settings.group.GroupSettingsActivity;
import com.orcchg.vikstra.data.source.memory.ContentUtility;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.misc.EmailContent;
import com.orcchg.vikstra.domain.util.Constant;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

@PerActivity
public class Navigator {

    private final List<WeakReference<AlertDialog>> dialogs = new ArrayList<>();

    @Inject
    public Navigator() {
    }

    /* Dialog */
    // ------------------------------------------
    public void openAccessTokenExhaustedDialog(@NonNull Activity context) {
        openDialog(context, 0, R.string.toast_access_token_has_expired, R.string.button_logout, false);
    }

    public void openAuthorizationNotPassedDialog(@NonNull Activity context) {
        openDialog(context, R.string.dialog_error_title, R.string.main_dialog_authorization_failed, R.string.button_close, true);
    }

    public void openDialog(@NonNull Activity context, @StringRes int title, @StringRes int description,
                           @StringRes int yesLabel, boolean finishAll) {
        Intent intent = DialogActivity.getCallingIntent(context, title, description, yesLabel, finishAll);
        context.startActivityForResult(intent, DialogActivity.REQUEST_CODE);
    }

    /* Email */
    // ------------------------------------------
    /**
     * {@see http://stackoverflow.com/questions/2197741/how-can-i-send-emails-from-my-android-application}
     */
    @DebugLog @ExternalScreen
    public void openEmailScreen(@NonNull Activity context, @NonNull EmailContent emailContent) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        if (emailContent.recipients() != null) {
            intent.putExtra(Intent.EXTRA_EMAIL, emailContent.recipients().toArray());
        }
        intent.putExtra(Intent.EXTRA_STREAM, emailContent.attachment());
        intent.putExtra(Intent.EXTRA_SUBJECT, emailContent.subject());
        intent.putExtra(Intent.EXTRA_TEXT, emailContent.body());
        try {
            String title = context.getResources().getString(R.string.message_send_email);
            context.startActivity(Intent.createChooser(intent, title));
        } catch (android.content.ActivityNotFoundException ex) {
            Timber.e("No Activity was found to send an email !");
            AlertDialog dialog = DialogProvider.showTextDialog(context, R.string.dialog_error_title,
                    R.string.error_external_screen_not_found_email);
            dialogs.add(new WeakReference<>(dialog));
        }
    }

    /* File & Web */
    // ------------------------------------------
    @DebugLog @ExternalScreen
    public void openBrowser(@NonNull Activity context, String url) {
        if (URLUtil.isValidUrl(url)) {
            Uri uri = Uri.parse(url);
            if (TextUtils.isEmpty(uri.getScheme())) {
                url = "https://" + url;
                uri = Uri.parse(url);
            }

            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                Timber.e("No Activity was found to open Browser !");
                AlertDialog dialog = DialogProvider.showTextDialog(context, R.string.dialog_error_title,
                        R.string.error_external_screen_not_found_browser);
                dialogs.add(new WeakReference<>(dialog));
            }
        } else {
            Timber.e("Input url [%s] is invalid !", url);
        }
    }

    /* Groups */
    // ------------------------------------------
    public void openGroupDetailScreen(@NonNull Context context, long groupId) {
        Intent intent = GroupDetailActivity.getCallingIntent(context, groupId);
        context.startActivity(intent);
    }

    public void openGroupListScreen(@NonNull Activity context) {
        openGroupListScreen(context, Constant.BAD_ID, Constant.BAD_ID);
    }

    public void openGroupListScreen(@NonNull Activity context, long keywordBundleId, long postId) {
        Intent intent = GroupListActivity.getCallingIntent(context, keywordBundleId, postId);
        context.startActivityForResult(intent, GroupListActivity.REQUEST_CODE);
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

    /* Main */
    // ------------------------------------------
    public void openMainScreen(@NonNull Context context) {
        Intent intent = MainActivity.getCallingIntent(context);
        context.startActivity(intent);
    }

    public void openStartScreen(@NonNull Context context) {
        Intent intent = StartActivity.getCallingIntent(context);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
            Timber.e("No Activity was found to open Gallery !");
            AlertDialog dialog = DialogProvider.showTextDialog(context, R.string.dialog_error_title,
                    R.string.error_external_screen_not_found_gallery);
            dialogs.add(new WeakReference<>(dialog));
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

                    /**
                     * Grant permissions for {@link FileProvider} to read / write Uri.
                     *
                     * {@see http://stackoverflow.com/questions/33650632/fileprovider-not-working-with-camera}
                     */
                    List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                } catch (IOException e) {
                    Timber.e(e, "Failed to create file for internal image !");
                    return;
                }
            }
            context.startActivityForResult(intent, Constant.RequestCode.EXTERNAL_SCREEN_CAMERA);
        } else {
            Timber.e("No Activity was found to open Camera !");
            AlertDialog dialog = DialogProvider.showTextDialog(context, R.string.dialog_error_title,
                    R.string.error_external_screen_not_found_camera);
            dialogs.add(new WeakReference<>(dialog));
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
        openPostListScreen(context, Constant.BAD_ID);
    }

    public void openPostListScreen(@NonNull Activity context, long selectedPostId) {
        Intent intent = PostListActivity.getCallingIntent(context, selectedPostId);
        context.startActivityForResult(intent, PostListActivity.REQUEST_CODE);
    }

    public void openPostViewScreen(@NonNull Activity context, long postId) {
        openPostViewScreen(context, postId, true);
    }

    public void openPostViewScreen(@NonNull Activity context, long postId, boolean editable) {
        Intent intent = PostViewActivity.getCallingIntent(context, postId, editable);
        context.startActivityForResult(intent, PostViewActivity.REQUEST_CODE);
    }

    /* Report */
    // ------------------------------------------
    public void openReportScreen(@NonNull Context context, long keywordBundleId, long postId) {
        openReportScreen(context, Constant.BAD_ID /* interactive mode (if config set) */, keywordBundleId, postId);
    }

    public void openReportScreen(@NonNull Context context, long groupReportBundleId, long keywordBundleId, long postId) {
        Intent intent = ReportActivity.getCallingIntent(context, groupReportBundleId, keywordBundleId, postId);
        context.startActivity(intent);
    }

    public void openReportScreenNoInteractive(@NonNull Context context, long groupReportBundleId, long keywordBundleId, long postId) {
        Intent intent = ReportActivity.getCallingIntentNoInteractive(context, groupReportBundleId, keywordBundleId, postId);
        context.startActivity(intent);
    }

    public void openReportHistoryScreen(@NonNull Context context) {
        Intent intent = ReportHistoryActivity.getCallingIntent(context);
        context.startActivity(intent);
    }

    /* Service */
    // ------------------------------------------
    public void startWallPostingService(@NonNull Context context, long keywordBundleId,
                                        Collection<Group> selectedGroups, Post post) {
        Intent intent = WallPostingService.getCallingIntent(context, keywordBundleId, selectedGroups, post);
        context.startService(intent);
    }

    /* Settings */
    // ------------------------------------------
    @ExternalScreen
    public void openSettings(@NonNull Context context) {
        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
        context.startActivity(intent);
    }

    public void openGroupSettings(@NonNull Activity context) {
        Intent intent = GroupSettingsActivity.getCallingIntent(context);
        context.startActivityForResult(intent, GroupSettingsActivity.REQUEST_CODE);
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    public void onDestroy() {
        for (WeakReference<AlertDialog> refDialog : dialogs) {
            AlertDialog dialog = refDialog.get();
            if (dialog != null) dialog.dismiss();
            refDialog.clear();
        }
        dialogs.clear();
    }
}
