/*
 * Copyright (C) 2012 Brian Muramatsu
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
 * limitations under the License.
 */

package com.btmura.android.reddit.app;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

import com.btmura.android.reddit.provider.ThingProvider;

/**
 * {@link ListFragment} with code to handle results from {@link ThingProvider}.
 */
abstract class ThingProviderListFragment extends ListFragment implements LoaderCallbacks<Cursor> {

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // ThingProvider returns information about the result set via cursor
        // extras. Extract those and call dedicated methods so callers don't
        // forget to handle them. Cursor could be null if there was a problem.
        if (cursor != null) {
            Bundle extras = cursor.getExtras();
            if (extras.containsKey(ThingProvider.EXTRA_RESOLVED_SUBREDDIT)) {
                onSubredditLoaded(extras.getString(ThingProvider.EXTRA_RESOLVED_SUBREDDIT));
            }
        }
    }

    /** Callback called when /random is resolved to some other subreddit. */
    protected abstract void onSubredditLoaded(String subreddit);
}
