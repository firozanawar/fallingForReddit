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

package com.btmura.android.reddit.content;

import java.io.IOException;

import android.content.Context;
import android.util.Log;

import com.btmura.android.reddit.net.RedditApi;
import com.btmura.android.reddit.net.SidebarResult;

/**
 * {@link BaseAsyncTaskLoader} that loads the sidebar of a subreddit.
 */
public class SidebarLoader extends BaseAsyncTaskLoader<SidebarResult> {

    public static final String TAG = "SidebarLoader";

    private final String subreddit;

    public SidebarLoader(Context context, String subreddit) {
        super(context.getApplicationContext());
        this.subreddit = subreddit;
    }

    @Override
    public SidebarResult loadInBackground() {
        try {
            return RedditApi.getSidebar(getContext(), subreddit, null);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    @Override
    protected void onNewDataDelivered(SidebarResult oldData, SidebarResult newData) {
        if (oldData != null) {
            oldData.recycle();
        }
    }

    @Override
    protected void onCleanData(SidebarResult data) {
        if (data != null) {
            data.recycle();
        }
    }
}
