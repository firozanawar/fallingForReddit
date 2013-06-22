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

import android.os.Bundle;

public class SubredditSearchFragment extends SubredditListFragment<SubredditSearchController> {

    public static SubredditSearchFragment newInstance(String accountName, String query,
            boolean singleChoice) {
        Bundle args = new Bundle(3);
        args.putString(SubredditSearchController.EXTRA_ACCOUNT_NAME, accountName);
        args.putString(SubredditSearchController.EXTRA_QUERY, query);
        args.putBoolean(SubredditSearchController.EXTRA_SINGLE_CHOICE, singleChoice);

        SubredditSearchFragment frag = new SubredditSearchFragment();
        frag.setArguments(args);
        return frag;
    }

    @Override
    protected SubredditSearchController createController() {
        return new SubredditSearchController(getActivity(), getArguments());
    }

    public String getQuery() {
        return controller.getAdapter().getQuery();
    }
}