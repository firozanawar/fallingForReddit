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

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonToken;

import com.btmura.android.reddit.database.Kinds;
import com.btmura.android.reddit.net.Urls;
import com.btmura.android.reddit.text.MarkdownFormatter;
import com.btmura.android.reddit.util.BundleSupport;
import com.btmura.android.reddit.util.JsonParser;
import com.btmura.android.reddit.util.Strings;

import java.io.IOException;

/**
 * {@link ThingBundle} is a {@link Bundle} representation of some thing. This
 * class adds type information to the underlying {@link Bundle}s attributes and
 * nothing more.
 */
public class ThingBundle extends BundleSupport implements Parcelable {

  // TODO: Make separate reference classes rather than reusing this one.
  public static final int TYPE_LINK = 0;
  public static final int TYPE_COMMENT = 1;
  public static final int TYPE_MESSAGE = 2;
  public static final int TYPE_LINK_REFERENCE = 3;
  public static final int TYPE_COMMENT_REFERENCE = 4;

  private static final int NUM_KEYS = 20;

  private static final String KEY_AUTHOR = "author";
  private static final String KEY_CREATED_UTC = "createdUtc";
  private static final String KEY_DOMAIN = "domain";
  private static final String KEY_DOWNS = "downs";
  private static final String KEY_KIND = "kind";
  private static final String KEY_LIKES = "likes";
  private static final String KEY_LINK_ID = "linkId";
  private static final String KEY_LINK_TITLE = "linkTitle";
  private static final String KEY_NUM_COMMENTS = "numComments";
  private static final String KEY_OVER_18 = "over18";
  private static final String KEY_PERMA_LINK = "permaLink";
  private static final String KEY_SAVED = "saved";
  private static final String KEY_SCORE = "score";
  private static final String KEY_SELF = "self";
  private static final String KEY_SUBJECT = "subject";
  private static final String KEY_SUBREDDIT = "subreddit";
  private static final String KEY_THING_ID = "thingId";
  private static final String KEY_THUMBNAIL_URL = "thumbnailUrl";
  private static final String KEY_TITLE = "title";
  private static final String KEY_UPS = "ups";
  private static final String KEY_URL = "url";

  public static final Parcelable.Creator<ThingBundle> CREATOR =
      new Parcelable.Creator<ThingBundle>() {
        public ThingBundle createFromParcel(Parcel in) {
          return new ThingBundle(in);
        }

        public ThingBundle[] newArray(int size) {
          return new ThingBundle[size];
        }
      };

  private final Bundle data;
  private final int type;

  public static ThingBundle fromJsonReader(
      JsonReader reader,
      MarkdownFormatter formatter) throws IOException {
    ThingBundleParser parser = new ThingBundleParser(formatter);
    parser.parseListingObject(reader);
    return new ThingBundle(parser.data, TYPE_LINK);
  }

  public static ThingBundle newLinkInstance(
      String author,
      long createdUtc,
      String domain,
      int downs,
      int likes,
      int kind,
      int numComments,
      boolean over18,
      String permaLink,
      boolean saved,
      int score,
      boolean self,
      String subreddit,
      String thingId,
      String thumbnailUrl,
      String title,
      int ups,
      String url) {
    Bundle data = new Bundle(18);
    data.putString(KEY_AUTHOR, author);
    data.putLong(KEY_CREATED_UTC, createdUtc);
    data.putString(KEY_DOMAIN, domain);
    data.putInt(KEY_DOWNS, downs);
    data.putInt(KEY_KIND, kind);
    data.putInt(KEY_LIKES, likes);
    data.putInt(KEY_NUM_COMMENTS, numComments);
    data.putBoolean(KEY_OVER_18, over18);
    data.putString(KEY_PERMA_LINK, permaLink);
    data.putBoolean(KEY_SAVED, saved);
    data.putInt(KEY_SCORE, score);
    data.putBoolean(KEY_SELF, self);
    data.putString(KEY_SUBREDDIT, subreddit);
    data.putString(KEY_THING_ID, thingId);
    data.putString(KEY_THUMBNAIL_URL, thumbnailUrl);
    data.putString(KEY_TITLE, title);
    data.putInt(KEY_UPS, ups);
    data.putString(KEY_URL, url);
    return new ThingBundle(data, TYPE_LINK);
  }

  public static ThingBundle newCommentInstance(
      String author,
      long createdUtc,
      String domain,
      int downs,
      int likes,
      int kind,
      String linkId,
      String linkTitle,
      int numComments,
      boolean over18,
      String permaLink,
      boolean saved,
      int score,
      boolean self,
      String subreddit,
      String thingId,
      String thumbnailUrl,
      String title,
      int ups,
      String url) {
    Bundle data = new Bundle(20);
    data.putString(KEY_AUTHOR, author);
    data.putLong(KEY_CREATED_UTC, createdUtc);
    data.putString(KEY_DOMAIN, domain);
    data.putInt(KEY_DOWNS, downs);
    data.putInt(KEY_KIND, kind);
    data.putString(KEY_LINK_ID, linkId);
    data.putString(KEY_LINK_TITLE, linkTitle);
    data.putInt(KEY_LIKES, likes);
    data.putInt(KEY_NUM_COMMENTS, numComments);
    data.putBoolean(KEY_OVER_18, over18);
    data.putString(KEY_PERMA_LINK, permaLink);
    data.putBoolean(KEY_SAVED, saved);
    data.putInt(KEY_SCORE, score);
    data.putBoolean(KEY_SELF, self);
    data.putString(KEY_SUBREDDIT, subreddit);
    data.putString(KEY_THING_ID, thingId);
    data.putString(KEY_THUMBNAIL_URL, thumbnailUrl);
    data.putString(KEY_TITLE, title);
    data.putInt(KEY_UPS, ups);
    data.putString(KEY_URL, url);
    return new ThingBundle(data, TYPE_COMMENT);
  }

  public static ThingBundle newMessageInstance(
      String author,
      int kind,
      String subject,
      String thingId) {
    Bundle data = new Bundle(4);
    data.putString(KEY_AUTHOR, author);
    data.putInt(KEY_KIND, kind);
    data.putString(KEY_SUBJECT, subject);
    data.putString(KEY_THING_ID, thingId);
    return new ThingBundle(data, TYPE_MESSAGE);
  }

  public static ThingBundle newLinkReference(String subreddit, String thingId) {
    Bundle data = new Bundle(2);
    data.putString(KEY_SUBREDDIT, subreddit);
    data.putString(KEY_THING_ID, thingId);
    return new ThingBundle(data, TYPE_LINK_REFERENCE);
  }

  public static ThingBundle newCommentReference(
      String subreddit, String thingId,
      String linkId) {
    Bundle data = new Bundle(3);
    data.putString(KEY_SUBREDDIT, subreddit);
    data.putString(KEY_THING_ID, thingId);
    data.putString(KEY_LINK_ID, linkId);
    return new ThingBundle(data, TYPE_COMMENT_REFERENCE);
  }

  protected ThingBundle(ThingBundle thingBundle) {
    this(thingBundle.data, thingBundle.type);
  }

  private ThingBundle(Parcel in) {
    this.data = in.readBundle();
    this.type = in.readInt();
  }

  private ThingBundle(Bundle data, int type) {
    this.data = data;
    this.type = type;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeBundle(data);
    dest.writeInt(type);
  }

  public int getType() {
    return type;
  }

  // TODO: Move these methods with logic to a different class.

  public String getDisplayTitle() {
    switch (getKind()) {
      case Kinds.KIND_MESSAGE:
        return getSubject();

      default:
        return getTitle();
    }
  }

  public CharSequence getCommentsUrl() {
    return Urls.permaLink(getPermaLink(), null);
  }

  public CharSequence getLinkUrl() {
    return getUrl();
  }

  public boolean hasCommentsUrl() {
    return getKind() == Kinds.KIND_LINK;
  }

  public boolean hasLinkUrl() {
    return getKind() == Kinds.KIND_LINK && !isSelf();
  }

  // Simple getters that return attributes as they are

  public String getAuthor() {
    return getString(data, KEY_AUTHOR);
  }

  public long getCreatedUtc() {
    return getLong(data, KEY_CREATED_UTC);
  }

  public String getDomain() {
    return getString(data, KEY_DOMAIN);
  }

  public int getDowns() {
    return getInt(data, KEY_DOWNS);
  }

  public int getKind() {
    return getInt(data, KEY_KIND);
  }

  public int getLikes() {
    return getInt(data, KEY_LIKES);
  }

  public String getLinkId() {
    return getString(data, KEY_LINK_ID);
  }

  public String getLinkTitle() {
    return getString(data, KEY_LINK_TITLE);
  }

  public int getNumComments() {
    return getInt(data, KEY_NUM_COMMENTS);
  }

  public String getPermaLink() {
    return getString(data, KEY_PERMA_LINK);
  }

  public int getScore() {
    return getInt(data, KEY_SCORE);
  }

  public String getSubject() {
    return getString(data, KEY_SUBJECT);
  }

  public String getSubreddit() {
    return getString(data, KEY_SUBREDDIT);
  }

  public String getTitle() {
    return getString(data, KEY_TITLE);
  }

  public String getThingId() {
    return getString(data, KEY_THING_ID);
  }

  public String getThumbnailUrl() {
    return getString(data, KEY_THUMBNAIL_URL);
  }

  public int getUps() {
    return getInt(data, KEY_UPS);
  }

  public String getUrl() {
    return getString(data, KEY_URL);
  }

  public boolean hasLinkId() {
    return !TextUtils.isEmpty(getLinkId());
  }

  public boolean isOver18() {
    return data.getBoolean(KEY_OVER_18);
  }

  public boolean isSaved() {
    return data.getBoolean(KEY_SAVED);
  }

  public boolean isSelf() {
    return data.getBoolean(KEY_SELF);
  }

  static class ThingBundleParser extends JsonParser {

    private final Bundle data = new Bundle(NUM_KEYS);
    private final MarkdownFormatter formatter;

    ThingBundleParser(MarkdownFormatter formatter) {
      this.formatter = formatter;
    }

    @Override
    public void onAuthor(JsonReader r, int i) throws IOException {
      data.putString(KEY_AUTHOR, readString(r, ""));
    }

    @Override
    public void onCreatedUtc(JsonReader r, int i) throws IOException {
      data.putLong(KEY_CREATED_UTC, r.nextLong());
    }

    @Override
    public void onDomain(JsonReader r, int i) throws IOException {
      data.putString(KEY_DOMAIN, readString(r, ""));
    }

    @Override
    public void onDowns(JsonReader r, int i) throws IOException {
      data.putInt(KEY_DOWNS, r.nextInt());
    }

    @Override
    public void onKind(JsonReader r, int i) throws IOException {
      data.putInt(KEY_KIND, Kinds.parseKind(r.nextString()));
    }

    @Override
    public void onLikes(JsonReader r, int i) throws IOException {
      int likes = 0;
      if (r.peek() == JsonToken.BOOLEAN) {
        likes = r.nextBoolean() ? 1 : -1;
      } else {
        r.skipValue();
      }
      data.putInt(KEY_LIKES, likes);
    }

    @Override
    public void onLinkId(JsonReader r, int i) throws IOException {
      data.putString(KEY_LINK_ID, readString(r, null));
    }

    @Override
    public void onLinkTitle(JsonReader r, int i) throws IOException {
      data.putString(KEY_LINK_TITLE, readFormattedString(r));
    }

    @Override
    public void onName(JsonReader r, int i) throws IOException {
      data.putString(KEY_THING_ID, readString(r, ""));
    }

    @Override
    public void onNumComments(JsonReader r, int i) throws IOException {
      data.putInt(KEY_NUM_COMMENTS, r.nextInt());
    }

    @Override
    public void onOver18(JsonReader r, int i) throws IOException {
      data.putBoolean(KEY_OVER_18, r.nextBoolean());
    }

    @Override
    public void onPermaLink(JsonReader r, int i) throws IOException {
      data.putString(KEY_PERMA_LINK, readString(r, ""));
    }

    @Override
    public void onSaved(JsonReader r, int i) throws IOException {
      data.putBoolean(KEY_SAVED, r.nextBoolean());
    }

    @Override
    public void onScore(JsonReader r, int i) throws IOException {
      data.putInt(KEY_SCORE, r.nextInt());
    }

    @Override
    public void onIsSelf(JsonReader r, int i) throws IOException {
      data.putBoolean(KEY_SELF, r.nextBoolean());
    }

    @Override
    public void onSubject(JsonReader r, int i) throws IOException {
      data.putString(KEY_SUBJECT, readFormattedString(r));
    }

    @Override
    public void onSubreddit(JsonReader r, int i) throws IOException {
      data.putString(KEY_SUBREDDIT, readString(r, ""));
    }

    @Override
    public void onThumbnail(JsonReader r, int i) throws IOException {
      String thumbnail = readString(r, null);
      if (!TextUtils.isEmpty(thumbnail) && thumbnail.startsWith("http")) {
        data.putString(KEY_THUMBNAIL_URL, thumbnail);
      }
    }

    @Override
    public void onTitle(JsonReader r, int i) throws IOException {
      data.putString(KEY_TITLE, readFormattedString(r));
    }

    @Override
    public void onUps(JsonReader r, int i) throws IOException {
      data.putInt(KEY_UPS, r.nextInt());
    }

    @Override
    public void onUrl(JsonReader r, int i) throws IOException {
      data.putString(KEY_URL, readString(r, ""));
    }

    private String readFormattedString(JsonReader reader) throws IOException {
      return Strings.toString(formatter.formatNoSpans(readString(reader, "")));
    }
  }
}
