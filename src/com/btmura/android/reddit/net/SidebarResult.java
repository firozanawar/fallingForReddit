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

import java.io.IOException;

import android.content.Context;
import android.util.JsonReader;

import com.btmura.android.reddit.text.Formatter;
import com.btmura.android.reddit.util.JsonParser;

/**
 * {@link SidebarResult} is the result of calling the
 * {@link RedditApi#getSidebar(Context, String, String)} method. It contains information about the
 * subreddit itself.
 */
public class SidebarResult extends JsonParser {

    public String subreddit;
    public CharSequence title;
    public CharSequence description;
    public int subscribers;

    private final Formatter formatter = new Formatter();
    private final Context context;

    public static SidebarResult fromJsonReader(Context context, JsonReader reader)
            throws IOException {
        SidebarResult result = new SidebarResult(context);
        result.parseEntity(reader);
        return result;
    }

    private SidebarResult(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void onDisplayName(JsonReader reader, int index) throws IOException {
        subreddit = reader.nextString();
    }

    @Override
    public void onTitle(JsonReader reader, int index) throws IOException {
        title = formatter.formatNoSpans(context, readTrimmedString(reader, ""));
    }

    @Override
    public void onDescription(JsonReader reader, int index) throws IOException {
        description = formatter.formatAll(context, readTrimmedString(reader, ""));
    }

    @Override
    public void onSubscribers(JsonReader reader, int index) throws IOException {
        subscribers = reader.nextInt();
    }
}