/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.tv.settings.util;

import android.content.AsyncTaskLoader;
import android.content.Context;

/**
 * This class fills in some boilerplate for AsyncTaskLoader to actually load things.
 *
 * Subclasses need to implement {@link SettingsAsyncTaskLoader#loadInBackground()} to perform the
 * actual background task, and {@link SettingsAsyncTaskLoader#onDiscardResult(T)} to clean up
 * previously loaded results.
 */

public abstract class SettingsAsyncTaskLoader<T> extends AsyncTaskLoader<T> {
    private T mResult;

    public SettingsAsyncTaskLoader(final Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (mResult != null) {
            deliverResult(mResult);
        }

        if (takeContentChanged() || mResult == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void deliverResult(final T data) {
        if (isReset()) {
            if (data != null) {
                onDiscardResult(data);
            }
            return;
        }

        final T oldResult = mResult;
        mResult = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        if (oldResult != null && oldResult != mResult) {
            onDiscardResult(oldResult);
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();

        if (mResult != null) {
            onDiscardResult(mResult);
        }
        mResult = null;
    }

    @Override
    public void onCanceled(final T data) {
        super.onCanceled(data);

        if (data != null) {
            onDiscardResult(data);
        }
    }

    /**
     * Called when discarding the load results so subclasses can take care of clean-up or
     * recycling tasks. This is not called if the same result (by way of pointer equality) is
     * returned again by a subsequent call to loadInBackground, or if result is null.
     *
     * Note that this may be called concurrently with loadInBackground(), and in some circumstances
     * may be called more than once for a given object.
     *
     * @param result The value returned from {@link SettingsAsyncTaskLoader#loadInBackground()} which
     *               is to be discarded.
     */
    protected abstract void onDiscardResult(final T result);
}
