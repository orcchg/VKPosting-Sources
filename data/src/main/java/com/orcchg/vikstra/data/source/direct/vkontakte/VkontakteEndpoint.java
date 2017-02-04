package com.orcchg.vikstra.data.source.direct.vkontakte;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.data.source.direct.Endpoint;
import com.orcchg.vikstra.data.source.direct.ImageLoader;
import com.orcchg.vikstra.data.source.memory.ContentUtility;
import com.orcchg.vikstra.domain.DomainConfig;
import com.orcchg.vikstra.domain.exception.ProgramException;
import com.orcchg.vikstra.domain.exception.vkontakte.VkUseCaseException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.MultiUseCase;
import com.orcchg.vikstra.domain.interactor.base.Ordered;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.vkontakte.GetCurrentUser;
import com.orcchg.vikstra.domain.interactor.vkontakte.GetGroupById;
import com.orcchg.vikstra.domain.interactor.vkontakte.GetGroupsByKeywordsList;
import com.orcchg.vikstra.domain.interactor.vkontakte.MakeWallPostToGroups;
import com.orcchg.vikstra.domain.interactor.vkontakte.media.UploadPhotos;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.Media;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.User;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;
import com.orcchg.vikstra.domain.notification.IPhotoUploadNotificationDelegate;
import com.orcchg.vikstra.domain.notification.IPostingNotificationDelegate;
import com.orcchg.vikstra.domain.util.Constant;
import com.orcchg.vikstra.domain.util.DebugSake;
import com.orcchg.vikstra.domain.util.ValueUtility;
import com.vk.sdk.api.model.VKApiCommunityArray;
import com.vk.sdk.api.model.VKApiCommunityFull;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.api.model.VKPhotoArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class VkontakteEndpoint extends Endpoint {

    private final ImageLoader imageLoader;
    private final VkAttachLocalCache attachLocalCache;

    private @DebugSake int postingInterval = 0;  // use default sleep interval

    public static class Scope {
        public static final String PHOTOS = "photos";
        public static final String WALL = "wall";
    }

    @Inject
    public VkontakteEndpoint(ImageLoader imageLoader, VkAttachLocalCache attachLocalCache,
                             ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.imageLoader = imageLoader;
        this.attachLocalCache = attachLocalCache;
    }

    @DebugLog @DebugSake
    public void setPostingInterval(int interval) {
        postingInterval = interval;
    }

    /* Group */
    // ------------------------------------------
    /**
     * Get group {@link Group} by it's string id {@param id}.
     */
    @DebugLog
    public void getGroupById(long id, @Nullable final UseCase.OnPostExecuteCallback<Group> callback) {
        GetGroupById useCase = new GetGroupById(id, threadExecutor, postExecuteScheduler);
        useCase.setPostExecuteCallback(new UseCase.OnPostExecuteCallback<VKApiCommunityArray>() {
            @DebugLog @Override
            public void onFinish(@Nullable VKApiCommunityArray vkGroup) {
                Timber.i("Use-Case [Vkontakte Endpoint]: succeeded to get Group by id");
                if (callback != null) {
                    Group group = vkGroup != null ? convert(Keyword.empty(), vkGroup.get(0)) : null;  // null means no such group found by id
                    callback.onFinish(group);  // pass found group further: in case of null value destination screen will handle it
                }
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case [Vkontakte Endpoint]: failed to get Group by id");
                if (callback != null) callback.onError(e);
            }
        });
        useCase.execute();
    }

    /**
     * For each keyword {@link Keyword} in list {@param keywords} retrieves a list of groups {@link Group}.
     * Because one keyword generally corresponds to multiple groups, the resulting list is merged
     * and contains all retrieved groups.
     */
    @DebugLog
    public void getGroupsByKeywords(final List<Keyword> keywords,
                                    @Nullable final UseCase.OnPostExecuteCallback<List<Group>> callback,
                                    @Nullable final MultiUseCase.CancelCallback cancelCallback) {
        GetGroupsByKeywordsList useCase = new GetGroupsByKeywordsList(keywords, threadExecutor, postExecuteScheduler);
        useCase.setCancelCallback(cancelCallback);
        useCase.setPostExecuteCallback(new UseCase.OnPostExecuteCallback<List<Ordered<VKApiCommunityArray>>>() {
            @DebugLog @Override
            public void onFinish(@Nullable List<Ordered<VKApiCommunityArray>> values) {
                if (values == null) {
                    Timber.e("List of VKApiCommunityArray-s must not be null, it could be empty at least");
                    throw new ProgramException();
                }
                Timber.i("Use-Case [Vkontakte Endpoint]: succeeded to get Group-s by Keyword-s");
                if (callback != null) callback.onFinish(convertMerge(keywords, ValueUtility.unwrap(values)));
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case [Vkontakte Endpoint]: failed to get Group-s by Keyword-s");
                if (callback != null) callback.onError(e);
            }
        });
        useCase.execute();
    }

    /**
     * Same as {@link VkontakteEndpoint#getGroupsByKeywords(List, UseCase.OnPostExecuteCallback, MultiUseCase.CancelCallback)},
     *  but splits groups by keywords.
     */
    @DebugLog
    public void getGroupsByKeywordsSplit(final List<Keyword> keywords,
                                         @Nullable final UseCase.OnPostExecuteCallback<List<List<Group>>> callback,
                                         @Nullable final MultiUseCase.CancelCallback cancelCallback) {
        GetGroupsByKeywordsList useCase = new GetGroupsByKeywordsList(keywords, threadExecutor, postExecuteScheduler);
        useCase.setCancelCallback(cancelCallback);
        useCase.setPostExecuteCallback(new UseCase.OnPostExecuteCallback<List<Ordered<VKApiCommunityArray>>>() {
            @DebugLog @Override
            public void onFinish(@Nullable List<Ordered<VKApiCommunityArray>> values) {
                if (values == null) {
                    Timber.e("List of VKApiCommunityArray-s must not be null, it could be empty at least");
                    throw new ProgramException();
                }
                Timber.i("Use-Case [Vkontakte Endpoint]: succeeded to get Group-s by Keyword-s (split)");
                if (callback != null) callback.onFinish(convertSplit(keywords, ValueUtility.unwrap(values)));
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case [Vkontakte Endpoint]: failed to get Group-s by Keyword-s (split)");
                if (callback != null) callback.onError(e);
            }
        });
        useCase.execute();
    }

    /* Post */
    // ------------------------------------------
    // TODO: implement various Media types {photo, video, file, ...}
    public void makeWallPosts(Collection<Group> groups, @NonNull Post post,
                              @Nullable final UseCase.OnPostExecuteCallback<List<GroupReportEssence>> callback) {
        makeWallPosts(groups, post, callback, null);
    }

    public void makeWallPosts(Collection<Group> groups, @NonNull Post post,
                              @Nullable final UseCase.OnPostExecuteCallback<List<GroupReportEssence>> callback,
                              @Nullable MultiUseCase.ProgressCallback progressCallback) {
        makeWallPosts(groups, post, callback, progressCallback, null);
    }

    public void makeWallPosts(Collection<Group> groups, @NonNull Post post,
                              @Nullable final UseCase.OnPostExecuteCallback<List<GroupReportEssence>> callback,
                              @Nullable MultiUseCase.ProgressCallback progressCallback,
                              @Nullable MultiUseCase.ProgressCallback photoUploadProgressCb) {
        makeWallPosts(groups, post, callback, progressCallback, photoUploadProgressCb, null);
    }

    public void makeWallPosts(Collection<Group> groups, @NonNull Post post,
                              @Nullable final UseCase.OnPostExecuteCallback<List<GroupReportEssence>> callback,
                              @Nullable MultiUseCase.ProgressCallback progressCallback,
                              @Nullable MultiUseCase.ProgressCallback photoUploadProgressCb,
                              @Nullable MultiUseCase.ProgressCallback photoPrepareProgressCb) {

        List<Group> sortedGroups = new ArrayList<>(groups);  // preserve ordering between groups and reports in future
        Collections.sort(sortedGroups);

        MakeWallPostToGroups.Parameters.Builder paramsBuilder = new MakeWallPostToGroups.Parameters.Builder()
                .setGroups(sortedGroups)
                .setMessage(post.description());

        List<Media> media = post.media();
        if (media != null && !media.isEmpty()) {
            Timber.d("Found some media in attachments to Post, uploading media first before wall posting");
            List<Media> cached = new ArrayList<>();
            List<Media> retained = new ArrayList<>();
            attachLocalCache.retain(media, cached, retained);
            Timber.v("Total media: cached: %s, retained: %s", cached.size(), retained.size());

            /**
             * For each already cached media we just make wall post with attached image ids directly.
             */
            if (!cached.isEmpty()) {
                VKAttachments attachments = attachLocalCache.readPhotos(cached);
                MakeWallPostToGroups.Parameters parameters = paramsBuilder.build();
                parameters.setAttachments(attachments);
                makeWallPosts(parameters, callback, progressCallback);
            }

            /**
             * For each retained media we need to launch full pipeline:
             *     1. load image bitmaps by urls into memory
             *     2. upload image bitmaps to Vkontakte, retrieving image ids
             *     3. make wall post with attached image ids
             *
             * Nothing will be done is 'retained' list is empty or NULL
             */
            imageLoader.loadImages(retained, new UseCase.OnPostExecuteCallback<List<Ordered<Bitmap>>>() {
                @DebugLog @Override
                public void onFinish(@Nullable List<Ordered<Bitmap>> bitmaps) {
                    Timber.i("Use-Case: succeeded to load images by urls");
                    UploadPhotos useCase = new UploadPhotos(threadExecutor, postExecuteScheduler);
                    useCase.setParameters(new UploadPhotos.Parameters(ValueUtility.unwrap(bitmaps)));
                    useCase.setProgressCallback(photoUploadProgressCb);
                    useCase.setPostExecuteCallback(createUploadPhotosCallback(retained, paramsBuilder, callback, progressCallback));
                    useCase.execute();
                }

                @DebugLog @Override
                public void onError(Throwable e) {
                    Timber.e("Use-Case: failed to load images by urls");
                    if (callback != null) callback.onError(e);
                }
            }, photoPrepareProgressCb);
        } else {
            Timber.d("No media attached to Post, make wall posting directly");
            makeWallPosts(paramsBuilder.build(), callback, progressCallback);
        }
    }

    public void makeWallPostsWithDelegate(Collection<Group> groups, @NonNull Post post,
                                          @Nullable final UseCase.OnPostExecuteCallback<List<GroupReportEssence>> callback,
                                          @Nullable IPostingNotificationDelegate postingNotificationDelegate,
                                          @Nullable IPhotoUploadNotificationDelegate photoUploadNotificationDelegate) {
        MultiUseCase.ProgressCallback<GroupReportEssence> progressCallback = (index, total, data) -> {
            Timber.d("Make wall posts progress: %s / %s", index + 1, total);
            if (postingNotificationDelegate == null) return;
            if (index + 1 < total) {  // progress == index + 1
                postingNotificationDelegate.onPostingProgress(index + 1, total);
            } else {
                postingNotificationDelegate.onPostingComplete();
            }
        };

        MultiUseCase.ProgressCallback<VKPhotoArray> photoUploadProgressCb = (index, total, data) -> {
            Timber.d("Photo uploading progress: %s / %s", index + 1, total);
            if (photoUploadNotificationDelegate == null) return;
            if (index + 1 < total) {  // progress == index + 1
                photoUploadNotificationDelegate.onPhotoUploadProgress(index + 1, total);
            } else {
                photoUploadNotificationDelegate.onPhotoUploadComplete();
            }
        };

        MultiUseCase.ProgressCallback<Bitmap> photoPrepareProgressCb = (index, total, data) -> {
            Timber.d("Photo preparing progress: %s / %s", index + 1, total);
            if (photoUploadNotificationDelegate != null) {
                photoUploadNotificationDelegate.onPhotoUploadProgressInfinite();
            }
        };

        makeWallPosts(groups, post, callback, progressCallback, photoUploadProgressCb, photoPrepareProgressCb);
    }

    /* User */
    // ------------------------------------------
    public void getCurrentUser(@Nullable final UseCase.OnPostExecuteCallback<User> callback) {
        GetCurrentUser useCase = new GetCurrentUser(threadExecutor, postExecuteScheduler);
        useCase.setPostExecuteCallback(new UseCase.OnPostExecuteCallback<VKList<VKApiUserFull>>() {
            @DebugLog @Override
            public void onFinish(@Nullable VKList<VKApiUserFull> users) {
                if (users == null || users.isEmpty()) {
                    Timber.e("List of VKApiUserFull-s must not be null or empty, it must contain current User info");
                    throw new ProgramException();
                }
                Timber.i("Use-Case [Vkontakte Endpoint]: succeeded to get current User");
                if (callback != null) callback.onFinish(convert(users.get(0)));
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case [Vkontakte Endpoint]: failed to get current User");
                if (callback != null) callback.onError(e);
            }
        });
        useCase.execute();
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void makeWallPosts(MakeWallPostToGroups.Parameters parameters,
                               @Nullable final UseCase.OnPostExecuteCallback<List<GroupReportEssence>> callback,
                               @Nullable MultiUseCase.ProgressCallback progressCallback) {
        MakeWallPostToGroups useCase = new MakeWallPostToGroups(threadExecutor, postExecuteScheduler);
        useCase.setParameters(parameters);
        useCase.setProgressCallback(((index, total, data) -> {
            ContentUtility.InMemoryStorage.setPostingProgress(index, total, data);
            if (progressCallback != null) progressCallback.onDone(index, total, data);
        }));
        useCase.setCancelCallback(ContentUtility.InMemoryStorage::onPostingCancelled);
        useCase.setPostExecuteCallback(new UseCase.OnPostExecuteCallback<List<Ordered<GroupReportEssence>>>() {
            @DebugLog @Override
            public void onFinish(@Nullable List<Ordered<GroupReportEssence>> reports) {
                if (reports == null) {
                    Timber.e("List of GroupReport-s must not be null, it could be empty at least");
                    throw new ProgramException();
                }
                Timber.i("Use-Case [Vkontakte Endpoint]: succeeded to make wall posting");
                ContentUtility.InMemoryStorage.onPostingFinished();  // fire additional callback
                int index = 0;
                List<GroupReportEssence> refinedReports = new ArrayList<>();
                // loop over all available results (there could a bit cancelled ones)
                for (Ordered<GroupReportEssence> item : reports) {
                    /**
                     * @Less_Reliable_Suggestion
                     * Order of resulting 'reports' strongly corresponds to the order of Group-s in
                     * input parameters of the use-case {@link MakeWallPostToGroups}, because it is
                     * {@link MultiUseCase} which returns results preserving the order of it's internal
                     * use-cases, which in turn are ordered according to the order of Group-s in parameters.
                     *
                     * This is less reliable suggestion, because ordering would probably change in future by mistake.
                     *
                     * @Stable_Suggestion
                     * This code is equivalent to the following variant:
                     *
                     *      MakeWallPost.Parameters params = (MakeWallPost.Parameters) item.parameters;
                     *      Group group = params.getGroup();
                     *
                     * Here we obtain input parameters, containing Group, for each single use-case
                     * {@link com.orcchg.vikstra.domain.interactor.vkontakte.MakeWallPost} inside the
                     * main use-case {@link MakeWallPostToGroups}, which results are eventually delivered
                     * here in this callback.
                     *
                     * This is stable suggestion, because we use parameters corresponding to the item under consideration.
                     */
                    Group group = parameters.getGroups().get(index);
                    refinedReports.add(refineModel(item, group, useCase.getTerminalErrors()));
                    ++index;
                }
                if (callback != null) callback.onFinish(refinedReports);
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case [Vkontakte Endpoint]: failed to get make wall posting");
                if (callback != null) callback.onError(e);
            }
        });
        if (postingInterval > 0) useCase.setSleepInterval(postingInterval);
        useCase.execute();
    }

    // ------------------------------------------
    /**
     * Creates callback on finish uploading photos to Vkontakte and then
     * makes wall posts, as initially intended.
     */
    private UseCase.OnPostExecuteCallback<List<Ordered<VKPhotoArray>>> createUploadPhotosCallback(
            List<Media> media, MakeWallPostToGroups.Parameters.Builder paramsBuilder,
            @Nullable UseCase.OnPostExecuteCallback<List<GroupReportEssence>> callback,
            @Nullable MultiUseCase.ProgressCallback progressCallback) {
        return new UseCase.OnPostExecuteCallback<List<Ordered<VKPhotoArray>>>() {
            @DebugLog @Override
            public void onFinish(@Nullable List<Ordered<VKPhotoArray>> photos) {
                if (photos == null) {
                    Timber.e("List of VKPhotoArray-s must not be null, it could be empty at least");
                    throw new ProgramException();
                }
                Timber.i("Use-Case [Vkontakte Endpoint]: succeeded to upload photos");
                VKAttachments attachments = new VKAttachments();
                List<VKPhotoArray> refinedPhotos = ValueUtility.unwrap(photos);
                int index = 0;
                for (VKPhotoArray aPhoto : refinedPhotos) {
                    VKApiPhoto photo = aPhoto.get(0);
                    attachments.add(photo);
                    attachLocalCache.writePhoto(media.get(index++).url(), photo);  // cache uploaded photos
                }
                paramsBuilder.setAttachments(attachments);
                makeWallPosts(paramsBuilder.build(), callback, progressCallback);
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case [Vkontakte Endpoint]: failed to upload photos");
                if (callback != null) callback.onError(e);
            }
        };
    }

    /* Conversion */
    // --------------------------------------------------------------------------------------------
    @NonNull
    private List<Group> convertMerge(List<Keyword> keywords, List<VKApiCommunityArray> vkModels) {
        int i = 0;
        List<Group> groups = new ArrayList<>();
        for (VKApiCommunityArray vkCommunityArray : vkModels) {
            groups.addAll(convert(keywords.get(i), vkCommunityArray));
            ++i;
        }
        return groups;
    }

    @NonNull
    private List<List<Group>> convertSplit(List<Keyword> keywords, List<VKApiCommunityArray> vkModels) {
        int i = 0;
        List<List<Group>> groupsSplit = new ArrayList<>();
        for (VKApiCommunityArray vkCommunityArray : vkModels) {
            List<Group> groups = convert(keywords.get(i), vkCommunityArray);
            groupsSplit.add(groups);
            ++i;
        }
        return groupsSplit;
    }

    @NonNull
    private List<Group> convert(Keyword keyword, VKApiCommunityArray vkCommunityArray) {
        int skipped = 0;
        List<Group> groups = new ArrayList<>();
        for (VKApiCommunityFull vkGroup : vkCommunityArray) {
            if (DomainConfig.INSTANCE.useOnlyGroupsWhereCanPostFreely() && !vkGroup.can_post) {
                ++skipped;
                continue;  // skip Group-s where is no access for current user to make wall post
            }
            groups.add(convert(keyword, vkGroup));
        }
        Timber.v("convert: keyword [%s], total vkGroups [%s], result size [%s], skipped [%s]",
                keyword.keyword(), vkCommunityArray.size(), groups.size(), skipped);
        return groups;
    }

    @NonNull
    private Group convert(Keyword keyword, VKApiCommunityFull vkGroup) {
        return Group.builder()
                .setId(vkGroup.id)
                .setCanPost(vkGroup.can_post)
                .setKeyword(keyword)
                .setLink("https://vk.com/" + vkGroup.screen_name)
                .setMembersCount(vkGroup.members_count)
                .setName(vkGroup.name)
                .setScreenName(vkGroup.screen_name)
                .setWebSite(vkGroup.site)
                .build();
    }

    @NonNull
    private User convert(VKApiUserFull vkUser) {
        return User.builder()
                .setId(vkUser.id)
                .setFirstName(vkUser.first_name)
                .setLastName(vkUser.last_name)
                .setPhotoUrl(vkUser.photo_50)
                .build();
    }

    /* Common */
    // --------------------------------------------------------------------------------------------
    /**
     * Compose an ordered collection of refined posting results, preserving the order
     * as of input parameters. Cancellation flag is ignored for successful results and
     * those results, finished due to non-terminal error.
     *
     * There can not be non-null data (successful result) and error simultaneously.
     * So, the following two if-statements are mutually exclusive.
     */
    public static GroupReportEssence refineModel(Ordered<GroupReportEssence> item, Group group, Class... terminalErrors) {
        if (item.data != null) return item.data;
        if (item.error != null) {
            VkUseCaseException e = (VkUseCaseException) item.error;
            boolean cancelByError = ValueUtility.containsClass(e, terminalErrors);
            return GroupReportEssence.builder()
                    .setCancelled(cancelByError)
                    .setErrorCode(e.getErrorCode())
                    .setGroup(group)
                    .setWallPostId(Constant.BAD_ID)
                    .build();
        }
        // Wall posting has been cancelled before obtaining any valid data or error code
        if (!item.cancelled) {
            Timber.e("Wall posting result has no data and error and must have been cancelled, but it hasn't");
            throw new ProgramException();
        }
        return GroupReportEssence.builder()
                .setCancelled(item.cancelled)
                .setErrorCode(Constant.NO_ERROR)
                .setGroup(group)
                .setWallPostId(Constant.BAD_ID)
                .build();
    }
}
