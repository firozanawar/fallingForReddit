/*
 * Copyright (C) 2015 Brian Muramatsu
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

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.btmura.android.reddit.R;
import com.btmura.android.reddit.accounts.AccountUtils;
import com.btmura.android.reddit.app.Filter;
import com.btmura.android.reddit.database.Subreddits;
import com.btmura.android.reddit.util.ThingIds;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class Urls2 {

  // TODO(btmura): make count a parameter

  public static final String OAUTH_REDIRECT_URL = "rbb://oauth/";

  private static final String NO_ACCOUNT = AccountUtils.NO_ACCOUNT;

  private static final int FORMAT_HTML = 0;
  private static final int FORMAT_JSON = 1;

  private static final String WWW_REDDIT_COM = "https://www.reddit.com";
  private static final String OAUTH_REDDIT_COM = "https://oauth.reddit.com";

  private static final String MY_SUBREDDITS_URL =
      OAUTH_REDDIT_COM + "/subreddits/mine/subscriber?limit=1000";

  private static final String AUTHORIZE_PATH = "/api/v1/authorize.compact";
  private static final String COMMENTS_PATH = "/comments/";
  private static final String MESSAGES_PATH = "/message/";
  private static final String MESSAGE_THREAD_PATH = "/message/messages/";
  private static final String SUBREDDIT_PATH = "/r/";
  private static final String USER_HTML_PATH = "/u/";
  private static final String USER_JSON_PATH = "/user/";


  public static CharSequence authorize(Context ctx, CharSequence state) {
    String clientId = ctx.getString(R.string.key_reddit_client_id);
    return new StringBuilder(WWW_REDDIT_COM)
        .append(AUTHORIZE_PATH)
        .append("?client_id=").append(encode(clientId))
        .append("&response_type=code&state=").append(encode(state))
        .append("&redirect_uri=").append(encode(OAUTH_REDIRECT_URL))
        .append("&duration=permanent&scope=")
        .append(encode("history,mysubreddits,privatemessages,read"));
  }

  public static CharSequence mySubreddits() {
    return MY_SUBREDDITS_URL;
  }

  public static CharSequence subreddit(
      String accountName,
      String subreddit,
      int filter,
      @Nullable String more) {
    return innerSubreddit(accountName, subreddit, filter, more, FORMAT_JSON);
  }

  public static CharSequence subredditLink(String subreddit) {
    return innerSubreddit(NO_ACCOUNT, subreddit, -1, null, FORMAT_HTML);
  }

  private static CharSequence innerSubreddit(
      String accountName,
      String subreddit,
      int filter,
      @Nullable String more,
      int format) {
    StringBuilder sb = new StringBuilder(getBaseUrl(accountName));

    if (!Subreddits.isFrontPage(subreddit)) {
      sb.append(SUBREDDIT_PATH).append(encode(subreddit));
    }

    if (!Subreddits.isRandom(subreddit)) {
      switch (filter) {
        case Filter.SUBREDDIT_CONTROVERSIAL:
          sb.append("/controversial");
          break;

        case Filter.SUBREDDIT_HOT:
          sb.append("/hot");
          break;

        case Filter.SUBREDDIT_NEW:
          sb.append("/new");
          break;

        case Filter.SUBREDDIT_RISING:
          sb.append("/rising");
          break;

        case Filter.SUBREDDIT_TOP:
          sb.append("/top");
          break;
      }
    }

    if (needsJsonExtension(accountName, format)) {
      sb.append(".json");
    }

    if (more != null) {
      sb.append("?count=25&after=").append(encode(more));
    }

    return sb;
  }

  public static CharSequence comments(
      String accountName,
      String thingId,
      String linkId,
      int filter,
      int numComments) {
    return innerComments(accountName, thingId, linkId, filter, numComments,
        FORMAT_JSON);
  }

  public static CharSequence commentsLink(String thingId, String linkId) {
    return innerComments(NO_ACCOUNT, thingId, linkId, -1, -1, FORMAT_HTML);
  }

  private static CharSequence innerComments(
      String accountName,
      String thingId,
      String linkId,
      int filter,
      int numComments,
      int format) {
    thingId = ThingIds.removeTag(thingId);

    boolean hasLinkId = !TextUtils.isEmpty(linkId);
    boolean hasLimit = numComments != -1;

    StringBuilder sb = new StringBuilder(getBaseUrl(accountName))
        .append(COMMENTS_PATH)
        .append(encode(hasLinkId ? ThingIds.removeTag(linkId) : thingId));

    if (needsJsonExtension(accountName, format)) {
      sb.append(".json");
    }

    if (hasLinkId || hasLimit || filter != -1) {
      sb.append("?");
    }

    if (hasLinkId) {
      sb.append("&comment=").append(encode(thingId)).append("&context=3");
    } else if (filter != -1) {
      switch (filter) {
        case Filter.COMMENTS_BEST:
          sb.append("&sort=confidence");
          break;

        case Filter.COMMENTS_CONTROVERSIAL:
          sb.append("&sort=controversial");
          break;

        case Filter.COMMENTS_HOT:
          sb.append("&sort=hot");
          break;

        case Filter.COMMENTS_NEW:
          sb.append("&sort=new");
          break;

        case Filter.COMMENTS_OLD:
          sb.append("&sort=old");
          break;

        case Filter.COMMENTS_TOP:
          sb.append("&sort=top");
          break;

        default:
          break;
      }
    }

    if (hasLimit) {
      sb.append("&limit=").append(numComments);
    }

    return sb;
  }

  public static CharSequence messages(
      int filter,
      @Nullable String more,
      boolean mark) {
    StringBuilder sb = new StringBuilder(OAUTH_REDDIT_COM)
        .append(MESSAGES_PATH);

    switch (filter) {
      case Filter.MESSAGE_INBOX:
        sb.append("inbox");
        break;

      case Filter.MESSAGE_UNREAD:
        sb.append("unread");
        break;

      case Filter.MESSAGE_SENT:
        sb.append("sent");
        break;

      default:
        throw new IllegalArgumentException();
    }
    if (more != null || mark) {
      sb.append("?");
    }
    if (more != null) {
      sb.append("&count=25&after=").append(encode(more));
    }
    if (mark) {
      sb.append("&mark=true");
    }
    return sb;
  }

  public static CharSequence profile(
      String accountName,
      String user,
      int filter,
      @Nullable String more) {
    StringBuilder sb = new StringBuilder(getBaseUrl(accountName))
        .append(USER_JSON_PATH)
        .append(encode(user));
    switch (filter) {
      case Filter.PROFILE_OVERVIEW:
        sb.append("/overview");
        break;

      case Filter.PROFILE_COMMENTS:
        sb.append("/comments");
        break;

      case Filter.PROFILE_SUBMITTED:
        sb.append("/submitted");
        break;

      case Filter.PROFILE_UPVOTED:
        sb.append("/upvoted");
        break;

      case Filter.PROFILE_DOWNVOTED:
        sb.append("/downvoted");
        break;

      case Filter.PROFILE_HIDDEN:
        sb.append("/hidden");
        break;

      case Filter.PROFILE_SAVED:
        sb.append("/saved");
        break;
    }
    if (needsJsonExtension(accountName, FORMAT_JSON)) {
      sb.append(".json");
    }
    if (more != null) {
      sb.append("?count=25&after=").append(encode(more));
    }
    return sb;
  }

  public static CharSequence profileLink(String user) {
    return new StringBuilder(WWW_REDDIT_COM)
        .append(USER_HTML_PATH)
        .append(encode(user));
  }

  public static CharSequence messageThread(String thingId) {
    return new StringBuilder(OAUTH_REDDIT_COM)
        .append(MESSAGE_THREAD_PATH)
        .append(encode(ThingIds.removeTag(thingId)));
  }

  public static CharSequence messageThreadLink(String thingId) {
    return new StringBuilder(WWW_REDDIT_COM)
        .append(MESSAGE_THREAD_PATH)
        .append(encode(ThingIds.removeTag(thingId)));
  }

  public static CharSequence search(
      String accountName,
      @Nullable String subreddit,
      String query,
      int filter,
      @Nullable String more) {
    return innerSearch(accountName, subreddit, query, filter, more);
  }

  private static CharSequence innerSearch(
      String accountName,
      @Nullable String subreddit,
      String query,
      int filter,
      @Nullable String more) {
    StringBuilder sb = new StringBuilder(getBaseUrl(accountName));

    if (!TextUtils.isEmpty(subreddit)) {
      sb.append(SUBREDDIT_PATH).append(encode(subreddit));
    }

    sb.append("/search");
    if (needsJsonExtension(accountName, FORMAT_JSON)) {
      sb.append(".json");
    }
    sb.append("?q=").append(encode(query));

    switch (filter) {
      case Filter.SEARCH_RELEVANCE:
        sb.append("&sort=relevance");
        break;

      case Filter.SEARCH_NEW:
        sb.append("&sort=new");
        break;

      case Filter.SEARCH_HOT:
        sb.append("&sort=hot");
        break;

      case Filter.SEARCH_TOP:
        sb.append("&sort=top");
        break;

      case Filter.SEARCH_COMMENTS:
        sb.append("&sort=comments");
        break;
    }
    if (more != null) {
      sb.append("&count=25&after=").append(encode(more));
    }
    if (!TextUtils.isEmpty(subreddit)) {
      sb.append("&restrict_sr=on");
    }
    return sb;
  }

  public static CharSequence sidebar(
      String accountName,
      String subreddit) {
    StringBuilder sb = new StringBuilder(getBaseUrl(accountName))
        .append(SUBREDDIT_PATH)
        .append(encode(subreddit))
        .append("/about");
    if (needsJsonExtension(accountName, FORMAT_JSON)) {
      sb.append(".json");
    }
    return sb;
  }

  public static CharSequence userInfo(String accountName, String user) {
    StringBuilder sb = new StringBuilder(getBaseUrl(accountName))
        .append(USER_JSON_PATH)
        .append(encode(user))
        .append("/about");
    if (needsJsonExtension(accountName, FORMAT_JSON)) {
      sb.append(".json");
    }
    return sb;
  }

  private static String getBaseUrl(String accountName) {
    return isOAuth(accountName) ? OAUTH_REDDIT_COM : WWW_REDDIT_COM;
  }

  private static boolean needsJsonExtension(String accountName, int format) {
    return !isOAuth(accountName) && format == FORMAT_JSON;
  }

  private static boolean isOAuth(String accountName) {
    return AccountUtils.isAccount(accountName);
  }

  public static URL newUrl(CharSequence url) {
    try {
      return new URL(url.toString());
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public static String encode(CharSequence param) {
    try {
      return URLEncoder.encode(param.toString(), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
