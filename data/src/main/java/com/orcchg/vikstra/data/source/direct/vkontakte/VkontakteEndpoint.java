package com.orcchg.vikstra.data.source.direct.vkontakte;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.data.source.direct.Endpoint;
import com.orcchg.vikstra.data.source.direct.ImageLoader;
import com.orcchg.vikstra.domain.exception.ProgramException;
import com.orcchg.vikstra.domain.exception.vkontakte.VkUseCaseException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.MultiUseCase;
import com.orcchg.vikstra.domain.interactor.base.Ordered;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.vkontakte.GetGroupById;
import com.orcchg.vikstra.domain.interactor.vkontakte.GetGroupsByKeywordsList;
import com.orcchg.vikstra.domain.interactor.vkontakte.MakeWallPostToGroups;
import com.orcchg.vikstra.domain.interactor.vkontakte.media.UploadPhotos;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.Media;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;
import com.orcchg.vikstra.domain.notification.IPhotoUploadNotificationDelegate;
import com.orcchg.vikstra.domain.notification.IPostingNotificationDelegate;
import com.orcchg.vikstra.domain.util.Constant;
import com.orcchg.vikstra.domain.util.ValueUtility;
import com.vk.sdk.api.model.VKApiCommunityArray;
import com.vk.sdk.api.model.VKApiCommunityFull;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKPhotoArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class VkontakteEndpoint extends Endpoint {

    private final ImageLoader imageLoader;
    private final VkAttachLocalCache attachLocalCache;

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

    /* Group */
    // ------------------------------------------
    /**
     * Get group {@link Group} by it's string id {@param id}.
     */
    public void getGroupById(long id, @Nullable final UseCase.OnPostExecuteCallback<Group> callback) {
        GetGroupById useCase = new GetGroupById(id, threadExecutor, postExecuteScheduler);
        useCase.setPostExecuteCallback(new UseCase.OnPostExecuteCallback<VKApiCommunityArray>() {
            @Override
            public void onFinish(@Nullable VKApiCommunityArray values) {
                if (callback != null) {
                    Group group = values != null ? convert(values.get(0)) : null;  // null means no such group found by id
                    callback.onFinish(group);  // pass found group further: in case of null value destination screen will handle it
                }
            }

            @Override
            public void onError(Throwable e) {
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
    public void getGroupsByKeywords(Collection<Keyword> keywords,
                                    @Nullable final UseCase.OnPostExecuteCallback<List<Group>> callback) {
        GetGroupsByKeywordsList useCase = new GetGroupsByKeywordsList(keywords, threadExecutor, postExecuteScheduler);
        useCase.setPostExecuteCallback(new UseCase.OnPostExecuteCallback<List<Ordered<VKApiCommunityArray>>>() {
            @Override
            public void onFinish(@Nullable List<Ordered<VKApiCommunityArray>> values) {
                if (values == null) {
                    Timber.e("List of VKApiCommunityArray instances cannot be null, but could be empty instead");
                    throw new ProgramException();
                }
                if (callback != null) callback.onFinish(convertMerge(ValueUtility.unwrap(values)));
            }

            @Override
            public void onError(Throwable e) {
                if (callback != null) callback.onError(e);
            }
        });
        useCase.execute();
    }

    /**
     * Same as {@link VkontakteEndpoint#getGroupsByKeywords(Collection, UseCase.OnPostExecuteCallback)}, but
     * splits groups by keywords.
     */
    @DebugLog
    public void getGroupsByKeywordsSplit(Collection<Keyword> keywords,
                                         @Nullable final UseCase.OnPostExecuteCallback<List<List<Group>>> callback) {
        GetGroupsByKeywordsList useCase = new GetGroupsByKeywordsList(keywords, threadExecutor, postExecuteScheduler);
        useCase.setPostExecuteCallback(new UseCase.OnPostExecuteCallback<List<Ordered<VKApiCommunityArray>>>() {
            @Override
            public void onFinish(@Nullable List<Ordered<VKApiCommunityArray>> values) {
                if (values == null) {
                    Timber.e("List of VKApiCommunityArray instances cannot be null, but could be empty instead");
                    throw new ProgramException();
                }
                if (callback != null) callback.onFinish(convertSplit(ValueUtility.unwrap(values)));
            }

            @Override
            public void onError(Throwable e) {
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
        makeWallPosts(groups, post, callback, null, null);
    }

    public void makeWallPosts(Collection<Group> groups, @NonNull Post post,
                              @Nullable final UseCase.OnPostExecuteCallback<List<GroupReportEssence>> callback,
                              @Nullable MultiUseCase.ProgressCallback progressCallback,
                              @Nullable MultiUseCase.ProgressCallback photoUploadProgressCb) {
        makeWallPosts(groups, post, callback, null, null, null);
    }

    public void makeWallPosts(Collection<Group> groups, @NonNull Post post,
                              @Nullable final UseCase.OnPostExecuteCallback<List<GroupReportEssence>> callback,
                              @Nullable MultiUseCase.ProgressCallback progressCallback,
                              @Nullable MultiUseCase.ProgressCallback photoUploadProgressCb,
                              @Nullable MultiUseCase.ProgressCallback photoPrepareProgressCb) {
        MakeWallPostToGroups.Parameters.Builder paramsBuilder = new MakeWallPostToGroups.Parameters.Builder()
                .setGroups(new ArrayList<>(groups))
                .setMessage(post.description());
        List<Media> media = post.media();
        if (media != null && !media.isEmpty()) {
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
                @Override
                public void onFinish(@Nullable List<Ordered<Bitmap>> bitmaps) {
                    Timber.d("Finished to load images");
                    UploadPhotos useCase = new UploadPhotos(threadExecutor, postExecuteScheduler);
                    useCase.setParameters(new UploadPhotos.Parameters(ValueUtility.unwrap(bitmaps)));
                    useCase.setProgressCallback(photoUploadProgressCb);
                    useCase.setPostExecuteCallback(createUploadPhotosCallback(retained, paramsBuilder, callback, progressCallback));
                    useCase.execute();
                }

                @Override
                public void onError(Throwable e) {
                    if (callback != null) callback.onError(e);
                }
            }, photoPrepareProgressCb);
        } else {
            makeWallPosts(paramsBuilder.build(), callback, progressCallback);
        }
    }

    public void makeWallPostsWithDelegate(Collection<Group> groups, @NonNull Post post,
                                          @Nullable final UseCase.OnPostExecuteCallback<List<GroupReportEssence>> callback,
                                          @Nullable IPostingNotificationDelegate postingNotificationDelegate,
                                          @Nullable IPhotoUploadNotificationDelegate photoUploadNotificationDelegate) {
        MultiUseCase.ProgressCallback progressCallback = (index, total) -> {
            Timber.v("Make wall posts progress: %s / %s", index, total);
            if (postingNotificationDelegate == null) return;
            if (index < total) {
                postingNotificationDelegate.onPostingProgress(index, total);
            } else {
                postingNotificationDelegate.onPostingComplete();
            }
        };

        MultiUseCase.ProgressCallback photoUploadProgressCb = (index, total) -> {
            Timber.v("Photo uploading progress: %s / %s", index, total);
            if (photoUploadNotificationDelegate == null) return;
            if (index < total) {
                photoUploadNotificationDelegate.onPhotoUploadProgress(index, total);
            } else {
                photoUploadNotificationDelegate.onPhotoUploadComplete();
            }
        };

        MultiUseCase.ProgressCallback photoPrepareProgressCb = (index, total) -> {
            Timber.v("Photo preparing progress: %s / %s", index, total);
            if (photoUploadNotificationDelegate != null) {
                photoUploadNotificationDelegate.onPhotoUploadProgressInfinite();
            }
        };

        makeWallPosts(groups, post, callback, progressCallback, photoUploadProgressCb, photoPrepareProgressCb);
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private void makeWallPosts(MakeWallPostToGroups.Parameters parameters,
                               @Nullable final UseCase.OnPostExecuteCallback<List<GroupReportEssence>> callback,
                               @Nullable MultiUseCase.ProgressCallback progressCallback) {
        MakeWallPostToGroups useCase = new MakeWallPostToGroups(threadExecutor, postExecuteScheduler);
        useCase.setParameters(parameters);
        useCase.setProgressCallback(progressCallback);
        useCase.setPostExecuteCallback(new UseCase.OnPostExecuteCallback<List<Ordered<GroupReportEssence>>>() {
            @Override
            public void onFinish(@Nullable List<Ordered<GroupReportEssence>> reports) {
                Timber.d("Finished wall posting");
                if (reports == null) {
                    Timber.e("List of GroupReports can not be null");
                    throw new ProgramException();
                }
                int index = 0;
                List<GroupReportEssence> refinedReports = new ArrayList<>();
                for (Ordered<GroupReportEssence> item : reports) {
                    if (item.data != null) refinedReports.add(item.data);
                    if (item.error != null) {
                        VkUseCaseException e = (VkUseCaseException) item.error;
                        GroupReportEssence report = GroupReportEssence.builder()
                                .setErrorCode(e.getErrorCode())
                                .setGroup(parameters.getGroups().get(index))
                                .setWallPostId(Constant.BAD_ID)
                                .build();
                        refinedReports.add(report);
                        ++index;
                    }
                }
                if (callback != null) callback.onFinish(refinedReports);
            }

            @Override
            public void onError(Throwable e) {
                if (callback != null) callback.onError(e);
            }
        });
        useCase.execute();
    }

    // ------------------------------------------
    /**
     * Creates callback on finish uploading photos to Vkontakte and then
     * makes wall posts, as initially intended.
     */
    UseCase.OnPostExecuteCallback<List<Ordered<VKPhotoArray>>> createUploadPhotosCallback(
            List<Media> media, MakeWallPostToGroups.Parameters.Builder paramsBuilder,
            @Nullable UseCase.OnPostExecuteCallback<List<GroupReportEssence>> callback,
            @Nullable MultiUseCase.ProgressCallback progressCallback) {
        return new UseCase.OnPostExecuteCallback<List<Ordered<VKPhotoArray>>>() {
            @Override
            public void onFinish(@Nullable List<Ordered<VKPhotoArray>> photos) {
                // TODO: NPE
                Timber.d("Finished uploading images");
                int index = 0;
                VKAttachments attachments = new VKAttachments();
                List<VKPhotoArray> refinedPhotos = ValueUtility.unwrap(photos);
                for (VKPhotoArray aPhoto : refinedPhotos) {
                    VKApiPhoto photo = aPhoto.get(0);
                    attachments.add(photo);
                    attachLocalCache.writePhoto(media.get(index++).id(), photo);
                }
                paramsBuilder.setAttachments(attachments);
                makeWallPosts(paramsBuilder.build(), callback, progressCallback);
            }

            @Override
            public void onError(Throwable e) {
                if (callback != null) callback.onError(e);
            }
        };
    }

    /* Conversion */
    // --------------------------------------------------------------------------------------------
    @NonNull
    List<Group> convertMerge(List<VKApiCommunityArray> vkModels) {
        List<Group> groups = new ArrayList<>();
        for (VKApiCommunityArray vkCommunityArray : vkModels) {
            groups.addAll(convert(vkCommunityArray));
        }
        return groups;
    }

    @NonNull
    List<List<Group>> convertSplit(List<VKApiCommunityArray> vkModels) {
        List<List<Group>> groupsSplit = new ArrayList<>();
        for (VKApiCommunityArray vkCommunityArray : vkModels) {
            List<Group> groups = convert(vkCommunityArray);
            groupsSplit.add(groups);
        }
        return groupsSplit;
    }

    @NonNull
    List<Group> convert(VKApiCommunityArray vkCommunityArray) {
        List<Group> groups = new ArrayList<>();
        for (VKApiCommunityFull vkGroup : vkCommunityArray) {
            groups.add(convert(vkGroup));
        }
        return groups;
    }

    @NonNull
    Group convert(VKApiCommunityFull vkGroup) {
        return Group.builder()
                .setId(-vkGroup.id)  // negative id is for Vk Community, positive - for Vk User
                .setCanPost(vkGroup.can_post)
                .setMembersCount(vkGroup.members_count)
                .setName(vkGroup.name)
                .build();
    }
}
