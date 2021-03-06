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

package com.btmura.android.reddit.net;

import android.content.UriMatcher;
import android.net.Uri;

import com.btmura.android.reddit.app.Filter;
import com.btmura.android.reddit.app.ThingBundle;
import com.btmura.android.reddit.database.Kinds;
import com.btmura.android.reddit.util.ThingIds;

import java.util.List;

public class UriHelper {

  private static final String[] AUTHORITIES = {
      "www.reddit.com",
      "reddit.com",
      "oauth.reddit.com",
  };

  private static final UriMatcher MATCHER = new UriMatcher(0);
  private static final int MATCH_SUBREDDIT = 1;
  private static final int MATCH_SUBREDDIT_HOT = 2;
  private static final int MATCH_SUBREDDIT_NEW = 3;
  private static final int MATCH_SUBREDDIT_CONTROVERSIAL = 4;
  private static final int MATCH_SUBREDDIT_TOP = 5;
  private static final int MATCH_COMMENTS = 6;
  private static final int MATCH_COMMENTS_CONTEXT = 7;
  private static final int MATCH_USER = 8;
  private static final int MATCH_USER_OVERVIEW = 9;
  private static final int MATCH_USER_COMMENTS = 10;
  private static final int MATCH_USER_SUBMITTED = 11;
  private static final int MATCH_USER_SAVED = 12;

  static {
    for (int i = 0; i < AUTHORITIES.length; i++) {
      // http://www.reddit.com/r/rbb
      MATCHER.addURI(AUTHORITIES[i], "r/*", MATCH_SUBREDDIT);
      // TODO(btmura): add .json case to other matchers
      MATCHER.addURI(AUTHORITIES[i], "r/*/.json", MATCH_SUBREDDIT);

      // Various filters of subreddits.
      MATCHER.addURI(AUTHORITIES[i], "r/*/hot", MATCH_SUBREDDIT_HOT);
      MATCHER.addURI(AUTHORITIES[i], "r/*/new", MATCH_SUBREDDIT_NEW);
      MATCHER.addURI(AUTHORITIES[i], "r/*/controversial",
          MATCH_SUBREDDIT_CONTROVERSIAL);
      MATCHER.addURI(AUTHORITIES[i], "r/*/top", MATCH_SUBREDDIT_TOP);

      // http://www.reddit.com/r/rbb/comments/12zl0q/
      MATCHER.addURI(AUTHORITIES[i], "r/*/comments/*", MATCH_COMMENTS);

      // http://www.reddit.com/r/rbb/comments/12zl0q/test_1
      MATCHER.addURI(AUTHORITIES[i], "r/*/comments/*/*", MATCH_COMMENTS);

      // http://www.reddit.com/r/rbb/comments/12zl0q/test_1/c8c9uvt
      MATCHER.addURI(AUTHORITIES[i], "r/*/comments/*/*/*",
          MATCH_COMMENTS_CONTEXT);

      // http://www.reddit.com/u/btmura
      MATCHER.addURI(AUTHORITIES[i], "u/*", MATCH_USER);
      MATCHER.addURI(AUTHORITIES[i], "user/*", MATCH_USER);

      // Various filters of users.
      MATCHER.addURI(AUTHORITIES[i], "u/*/overview", MATCH_USER_OVERVIEW);
      MATCHER.addURI(AUTHORITIES[i], "u/*/comments", MATCH_USER_COMMENTS);
      MATCHER.addURI(AUTHORITIES[i], "u/*/submitted", MATCH_USER_SUBMITTED);
      MATCHER.addURI(AUTHORITIES[i], "u/*/saved", MATCH_USER_SAVED);

      MATCHER.addURI(AUTHORITIES[i], "user/*/overview", MATCH_USER_OVERVIEW);
      MATCHER.addURI(AUTHORITIES[i], "user/*/comments", MATCH_USER_COMMENTS);
      MATCHER.addURI(AUTHORITIES[i], "user/*/submitted", MATCH_USER_SUBMITTED);
      MATCHER.addURI(AUTHORITIES[i], "user/*/saved", MATCH_USER_SAVED);
    }
  }

  public static boolean hasSubreddit(Uri uri) {
    if (uri != null) {
      switch (MATCHER.match(uri)) {
        case MATCH_SUBREDDIT:
        case MATCH_SUBREDDIT_HOT:
        case MATCH_SUBREDDIT_NEW:
        case MATCH_SUBREDDIT_CONTROVERSIAL:
        case MATCH_SUBREDDIT_TOP:
        case MATCH_COMMENTS:
        case MATCH_COMMENTS_CONTEXT:
          return true;
      }
    }
    return false;
  }

  public static boolean hasUser(Uri uri) {
    if (uri != null) {
      switch (MATCHER.match(uri)) {
        case MATCH_USER:
        case MATCH_USER_OVERVIEW:
        case MATCH_USER_COMMENTS:
        case MATCH_USER_SUBMITTED:
        case MATCH_USER_SAVED:
          return true;
      }
    }
    return false;
  }

  public static String getSubreddit(Uri uri) {
    if (uri != null) {
      switch (MATCHER.match(uri)) {
        case MATCH_SUBREDDIT:
        case MATCH_SUBREDDIT_HOT:
        case MATCH_SUBREDDIT_NEW:
        case MATCH_SUBREDDIT_CONTROVERSIAL:
        case MATCH_SUBREDDIT_TOP:
        case MATCH_COMMENTS:
        case MATCH_COMMENTS_CONTEXT:
          String sr = uri.getPathSegments().get(1);
          int dot = sr.indexOf('.');
          if (dot != -1) {
            sr = sr.substring(0, dot);
          }
          return sr;
      }
    }
    return null;
  }

  public static int getSubredditFilter(Uri uri) {
    if (uri != null) {
      switch (MATCHER.match(uri)) {
        case MATCH_SUBREDDIT_HOT:
          return Filter.SUBREDDIT_HOT;

        case MATCH_SUBREDDIT_NEW:
          return Filter.SUBREDDIT_NEW;

        case MATCH_SUBREDDIT_CONTROVERSIAL:
          return Filter.SUBREDDIT_CONTROVERSIAL;

        case MATCH_SUBREDDIT_TOP:
          return Filter.SUBREDDIT_TOP;
      }
    }
    return -1;
  }

  public static ThingBundle getThingBundle(Uri data) {
    switch (MATCHER.match(data)) {
      case MATCH_COMMENTS:
        List<String> segments = data.getPathSegments();
        String subreddit = segments.get(1);
        String thingId = ThingIds.addTag(segments.get(3),
            Kinds.getTag(Kinds.KIND_LINK));
        return ThingBundle.newLinkReference(subreddit, thingId);

      case MATCH_COMMENTS_CONTEXT:
        segments = data.getPathSegments();
        subreddit = segments.get(1);
        thingId = ThingIds.addTag(segments.get(5),
            Kinds.getTag(Kinds.KIND_COMMENT));
        String linkId = ThingIds.addTag(segments.get(3),
            Kinds.getTag(Kinds.KIND_LINK));
        return ThingBundle.newCommentReference(subreddit, thingId, linkId);

      default:
        return null;
    }
  }

  public static String getUser(Uri data) {
    if (data != null) {
      switch (MATCHER.match(data)) {
        case MATCH_USER:
        case MATCH_USER_OVERVIEW:
        case MATCH_USER_COMMENTS:
        case MATCH_USER_SUBMITTED:
        case MATCH_USER_SAVED:
          return data.getPathSegments().get(1);
      }
    }
    return null;
  }

  public static int getUserFilter(Uri data) {
    if (data != null) {
      switch (MATCHER.match(data)) {
        case MATCH_USER_OVERVIEW:
          return Filter.PROFILE_OVERVIEW;

        case MATCH_USER_COMMENTS:
          return Filter.PROFILE_COMMENTS;

        case MATCH_USER_SUBMITTED:
          return Filter.PROFILE_SUBMITTED;

        case MATCH_USER_SAVED:
          return Filter.PROFILE_SAVED;
      }
    }
    return -1;
  }
}
