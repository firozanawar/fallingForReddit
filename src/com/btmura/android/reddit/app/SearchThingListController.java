/*
 * Copyright (C) 2013 Brian Muramatsu
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

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;

import com.btmura.android.reddit.content.SearchThingLoader;
import com.btmura.android.reddit.util.Objects;
import com.btmura.android.reddit.widget.ThingListAdapter;

class SearchThingListController extends AbstractThingListController {

    private static final String EXTRA_QUERY = "query";

    private String query;

    public SearchThingListController(Context context, ThingListAdapter adapter) {
        super(context, adapter);
    }

    @Override
    public boolean isLoadable() {
        return getAccountName() != null && getQuery() != null;
    }

    @Override
    public Loader<Cursor> createLoader() {
        return new SearchThingLoader(context, getAccountName(), getSubreddit(), getQuery());
    }

    @Override
    public void loadState(Bundle state) {
        super.loadState(state);
        state = Objects.nullToEmpty(state);
        if (state.containsKey(EXTRA_QUERY)) {
            setQuery(state.getString(EXTRA_QUERY));
        }
    }

    @Override
    public void saveState(Bundle state) {
        super.saveState(state);
        state.putString(EXTRA_QUERY, getQuery());
    }

    private void setQuery(String query) {
        this.query = query;
    }

    @Override
    public String getQuery() {
        return query;
    }
}