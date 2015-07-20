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

package com.btmura.android.reddit.widget;

import android.content.Context;
import android.database.Cursor;
import android.view.View;

import com.btmura.android.reddit.content.SearchSubredditLoader;
import com.btmura.android.reddit.util.Objects;

/** {@link SubredditAdapter} that handles searching for subreddits. */
public class SearchSubredditAdapter extends SubredditAdapter {

  public SearchSubredditAdapter(Context ctx, boolean singleChoice) {
    super(ctx, singleChoice);
  }

  @Override
  public void bindView(View v, Context ctx, Cursor c) {
    String name = c.getString(SearchSubredditLoader.INDEX_NAME);
    int subscribers = c.getInt(SearchSubredditLoader.INDEX_SUBSCRIBERS);
    boolean over18 = c.getInt(SearchSubredditLoader.INDEX_OVER_18) == 1;
    SubredditView sv = (SubredditView) v;
    sv.setData(name, over18, subscribers);
    sv.setChosen(singleChoice
        && Objects.equalsIgnoreCase(selectedSubreddit, name));
  }

  @Override
  public String getName(int pos) {
    return getString(pos, SearchSubredditLoader.INDEX_NAME);
  }
}
