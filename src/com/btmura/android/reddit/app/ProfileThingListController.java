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

import com.btmura.android.reddit.content.ProfileThingLoader;
import com.btmura.android.reddit.widget.OnVoteListener;

class ProfileThingListController extends AbstractThingTableListController {

    /** String argument specifying the profileUser profile to load. */
    static final String EXTRA_PROFILE_USER = "profileUser";

    private final String profileUser;

    ProfileThingListController(Context context, Bundle args, OnVoteListener listener) {
        super(context, args, listener);
        this.profileUser = getProfileUserExtra(args);
    }

    @Override
    public boolean isLoadable() {
        return getAccountName() != null && profileUser != null;
    }

    @Override
    public Loader<Cursor> createLoader() {
        return new ProfileThingLoader(context, getAccountName(), profileUser, getFilter(),
                getMoreId(), getSessionId());
    }

    public String getProfileUser() {
        return profileUser;
    }

    private String getProfileUserExtra(Bundle extras) {
        return extras.getString(EXTRA_PROFILE_USER);
    }
}
