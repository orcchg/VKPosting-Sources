package com.orcchg.vikstra.data.source.direct.vkontakte;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.data.source.direct.Endpoint;
import com.orcchg.vikstra.data.source.direct.ImageLoader;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.MultiUseCase;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.vkontakte.GetGroupById;
import com.orcchg.vikstra.domain.interactor.vkontakte.GetGroupsByKeywordsList;
import com.orcchg.vikstra.domain.interactor.vkontakte.MakeWallPostToGroups;
import com.orcchg.vikstra.domain.interactor.vkontakte.media.UploadPhotos;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.Media;
import com.orcchg.vikstra.domain.model.Post;
import com.vk.sdk.api.model.VKApiCommunityArray;
import com.vk.sdk.api.model.VKApiCommunityFull;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKPhotoArray;
import com.vk.sdk.api.model.VKWallPostResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class VkontakteEndpoint extends Endpoint {

    private final ImageLoader imageLoader;
    private final VkAttachLocalCache attachLocalCache;

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
            public void onFinish(VKApiCommunityArray values) {
                if (callback != null) callback.onFinish(convert(values.get(0)));
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
    public void getGroupsByKeywords(List<Keyword> keywords,
                                    @Nullable final UseCase.OnPostExecuteCallback<List<Group>> callback) {
        GetGroupsByKeywordsList useCase = new GetGroupsByKeywordsList(keywords, threadExecutor, postExecuteScheduler);
        useCase.setPostExecuteCallback(new UseCase.OnPostExecuteCallback<List<VKApiCommunityArray>>() {
            @Override
            public void onFinish(List<VKApiCommunityArray> values) {
                if (callback != null) callback.onFinish(convertMerge(values));
            }

            @Override
            public void onError(Throwable e) {
                if (callback != null) callback.onError(e);
            }
        });
        useCase.execute();
    }

    /**
     * Same as {@link VkontakteEndpoint#getGroupsByKeywords(List, UseCase.OnPostExecuteCallback)}, but
     * splits groups by keywords.
     */
    public void getGroupsByKeywordsSplit(Collection<Keyword> keywords,
                                         @Nullable final UseCase.OnPostExecuteCallback<List<List<Group>>> callback) {
        GetGroupsByKeywordsList useCase = new GetGroupsByKeywordsList(keywords, threadExecutor, postExecuteScheduler);
        useCase.setPostExecuteCallback(new UseCase.OnPostExecuteCallback<List<VKApiCommunityArray>>() {
            @Override
            public void onFinish(List<VKApiCommunityArray> values) {
                if (callback != null) callback.onFinish(convertSplit(values));
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
    public void makeWallPosts(Collection<Long> groupIds, Post post,
                              @Nullable final UseCase.OnPostExecuteCallback<List<GroupReport>> callback,
                              @Nullable MultiUseCase.ProgressCallback progressCallback) {
        MakeWallPostToGroups.Parameters.Builder paramsBuilder = new MakeWallPostToGroups.Parameters.Builder()
                .setGroupIds(groupIds)
                .setMessage(post.description());
        if (post.media() != null) {
            List<Media> cached = new ArrayList<>();
            List<Media> retained = new ArrayList<>();
            attachLocalCache.retain(post.media(), cached, retained);
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
            imageLoader.loadImages(retained, new UseCase.OnPostExecuteCallback<List<Bitmap>>() {
                @Override
                public void onFinish(@Nullable List<Bitmap> bitmaps) {
                    UploadPhotos useCase = new UploadPhotos(threadExecutor, postExecuteScheduler);
                    useCase.setParameters(new UploadPhotos.Parameters(bitmaps));
                    useCase.setPostExecuteCallback(createUploadPhotosCallback(retained, paramsBuilder, callback, progressCallback));
                }

                @Override
                public void onError(Throwable e) {
                    if (callback != null) callback.onError(e);
                }
            });
        } else {
            makeWallPosts(paramsBuilder.build(), callback, progressCallback);
        }
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private void makeWallPosts(MakeWallPostToGroups.Parameters parameters,
                               @Nullable final UseCase.OnPostExecuteCallback<List<GroupReport>> callback,
                               @Nullable MultiUseCase.ProgressCallback progressCallback) {
        MakeWallPostToGroups useCase = new MakeWallPostToGroups(threadExecutor, postExecuteScheduler);
        useCase.setParameters(parameters);
        useCase.setProgressCallback(progressCallback);
        useCase.setPostExecuteCallback(new UseCase.OnPostExecuteCallback<List<VKWallPostResult>>() {
            @Override
            public void onFinish(@Nullable List<VKWallPostResult> values) {
                if (callback != null) callback.onFinish(convert(values));
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
     * makes wall posts, as initially intented.
     */
    UseCase.OnPostExecuteCallback<List<VKPhotoArray>> createUploadPhotosCallback(
            List<Media> media, MakeWallPostToGroups.Parameters.Builder paramsBuilder,
            @Nullable UseCase.OnPostExecuteCallback<List<GroupReport>> callback,
            @Nullable MultiUseCase.ProgressCallback progressCallback) {
        return new UseCase.OnPostExecuteCallback<List<VKPhotoArray>>() {
            @Override
            public void onFinish(@Nullable List<VKPhotoArray> photos) {
                // TODO: NPE
                int index = 0;
                VKAttachments attachments = new VKAttachments();
                for (VKPhotoArray aPhoto : photos) {
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
    private List<Group> convertMerge(List<VKApiCommunityArray> vkModels) {
        List<Group> groups = new ArrayList<>();
        for (VKApiCommunityArray vkCommunityArray : vkModels) {
            groups.addAll(convert(vkCommunityArray));
        }
        return groups;
    }

    private List<List<Group>> convertSplit(List<VKApiCommunityArray> vkModels) {
        List<List<Group>> groupsSplit = new ArrayList<>();
        for (VKApiCommunityArray vkCommunityArray : vkModels) {
            List<Group> groups = convert(vkCommunityArray);
            groupsSplit.add(groups);
        }
        return groupsSplit;
    }

    private List<Group> convert(VKApiCommunityArray vkCommunityArray) {
        List<Group> groups = new ArrayList<>();
        for (VKApiCommunityFull vkGroup : vkCommunityArray) {
            groups.add(convert(vkGroup));
        }
        return groups;
    }

    private Group convert(VKApiCommunityFull vkGroup) {
        return Group.builder()
                .setId(vkGroup.id)
                .setMembersCount(vkGroup.members_count)
                .setName(vkGroup.name)
                .build();
    }

    private List<GroupReport> convert(List<VKWallPostResult> vkReports) {
        List<GroupReport> reports = new ArrayList<>();
        for (VKWallPostResult vkReport : vkReports) {
            reports.add(convert(vkReport));
        }
        return reports;
    }

    private GroupReport convert(VKWallPostResult vkReport) {
        return GroupReport.builder()
                .setWallPostId(vkReport.post_id)
                .build();  // TODO: impl with status data
    }
}
