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

import android.app.Activity;
import android.app.ListFragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.btmura.android.reddit.R;
import com.btmura.android.reddit.provider.CommentProvider;
import com.btmura.android.reddit.provider.ReplyProvider;
import com.btmura.android.reddit.provider.SubredditProvider;
import com.btmura.android.reddit.provider.SubredditSearchProvider;
import com.btmura.android.reddit.provider.ThingProvider;
import com.btmura.android.reddit.provider.VoteProvider;

public class ContentUriListFragment extends ListFragment {

    public static final String TAG = "ContentUriListFragment";

    interface OnUriClickListener {
        void onUriClick(Uri uri);
    }

    private OnUriClickListener listener;
    private ArrayAdapter<Uri> adapter;

    public static ContentUriListFragment newInstance() {
        return new ContentUriListFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnUriClickListener) {
            listener = (OnUriClickListener) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ArrayAdapter<Uri>(getActivity(), R.layout.content_uri_row);
        adapter.add(SubredditProvider.CONTENT_URI);
        adapter.add(ThingProvider.CONTENT_URI);
        adapter.add(CommentProvider.CONTENT_URI);
        adapter.add(SubredditSearchProvider.CONTENT_URI);
        adapter.add(ReplyProvider.CONTENT_URI);
        adapter.add(VoteProvider.CONTENT_URI);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (listener != null) {
            listener.onUriClick(adapter.getItem(position));
        }
    }
}