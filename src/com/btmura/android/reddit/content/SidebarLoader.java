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

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.btmura.android.reddit.provider.NetApi;
import com.btmura.android.reddit.provider.NetApi.Sidebar;

public class SidebarLoader extends AsyncTaskLoader<Sidebar> {

    public static final String TAG = "SidebarLoader";

    private Sidebar results;

    private String subreddit;

    public SidebarLoader(Context context, String subreddit) {
        super(context.getApplicationContext());
        this.subreddit = subreddit;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (results != null) {
            deliverResult(results);
        } else {
            forceLoad();
        }
    }

    @Override
    public Sidebar loadInBackground() {
        try {
            return NetApi.querySidebar(getContext(), subreddit, null);
        } catch (IOException e) {
            Log.e(TAG, "loadInBackground", e);
        }
        return null;
    }
}
