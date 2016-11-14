package com.orcchg.vikstra.data.source.direct.vkontakte;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.BundledException;
import com.orcchg.vikstra.domain.exception.vkontakte.VkUseCaseException;
import com.orcchg.vikstra.domain.interactor.UseCase;
import com.orcchg.vikstra.domain.interactor.vkontakte.GetGroupById;
import com.orcchg.vikstra.domain.interactor.vkontakte.GetGroupsByKeyword;
import com.orcchg.vikstra.domain.interactor.vkontakte.MakeWallPost;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupBundle;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.util.ValueUtility;
import com.vk.sdk.api.VKError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class VkontakteEndpoint {

    @Inject GetGroupById getGroupByIdVkUseCase;
    @Inject GetGroupsByKeyword getGroupsByKeywordVkUseCase;
    @Inject MakeWallPost makeWallPostVkUseCase;

    protected Object mLock = new Object();

    @Inject
    public VkontakteEndpoint() {
    }

    public GroupBundle getGroupsByKeywords(List<Keyword> keywords) {
        //
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private List<Group> requestGroupsByKeywords(List<Keyword> keywords) {
        //
    }

    private <Result> void performMultipleRequests(int total, UseCase<Result> useCase) {
        final List<Result> results = new ArrayList<>();
        final List<Throwable> errors = new ArrayList<>();
        final boolean[] doneFlags = new boolean[total];
        Arrays.fill(doneFlags, false);

        for (int i = 0; i < total; ++i) {
            final int index = i;
            final long start = System.currentTimeMillis();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long elapsed = start;
                    boolean finishedWithError = false;
                    Result result = null;
                    while (elapsed - start < 30_000) {
                        try {
                            result = useCase.executeSync();
                        } catch (VkUseCaseException e) {
                            if (e.getErrorCode() == VKError.VK_API_ERROR) {
                                // в случае VKError.VK_API_ERROR - подождать случайный тайм-аут и повторить запрос
                                try {
                                    long delta = ValueUtility.random(100, 1000);
                                    Thread.sleep(1000 + delta);
                                } catch (InterruptedException ie) {
                                    Thread.interrupted();  // в случае прерывания - продолжить выполнение цикла
                                }
                            } else {
                                addToErrors(errors, e);
                                finishedWithError = true;
                                break;
                            }
                        }
                        elapsed = System.currentTimeMillis();
                    }
                    if (!finishedWithError) {
                        addToResults(results, result);
                    }
                    synchronized (mLock) {
                        doneFlags[index] = true;
                        mLock.notify();  // пробудить поток-обработчик ответа
                    }
                }
            }).start();
        }

        synchronized (mLock) {
            while (!ValueUtility.isAllTrue(doneFlags)) {
                try {
                    mLock.wait();
                    if (!errors.isEmpty()) {
                        /**
                         * Обработка списка исключений, которые не удалось обработать в рабочих потоках выше.
                         * Здесь - передача исключения "наверх", на вызывающий данную команду клиент.
                         */
                        throw new BundledException.Builder().addErrors(errors).build();
                    }
                } catch (InterruptedException e) {
                    Thread.interrupted();  // в случае прерывания - продолжить выполнение цикла
                }
            }
        }
    }

    synchronized <Result> void addToResults(List<Result> results, @Nullable Result result) {
        if (result != null) {
            results.add(result);
        }
    }

    synchronized void addToErrors(List<Throwable> errors, @Nullable VkUseCaseException error) {
        if (error != null) {
            errors.add(error);
        }
    }
}
