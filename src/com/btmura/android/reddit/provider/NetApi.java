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

package com.btmura.android.reddit.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import android.content.Context;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;

import com.btmura.android.reddit.Debug;
import com.btmura.android.reddit.data.Urls;
import com.btmura.android.reddit.entity.Comment;
import com.btmura.android.reddit.entity.LoginResult;
import com.btmura.android.reddit.entity.Subreddit;
import com.btmura.android.reddit.entity.Thing;

public class NetApi {

    public static final String TAG = "NetApi";
    public static final boolean DEBUG = Debug.DEBUG;

    private static final String CHARSET = "UTF-8";
    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded;charset="
            + CHARSET;
    private static final String USER_AGENT = "reddit by brian (rbb) for Android by /u/btmura";

    public static ArrayList<String> querySubreddits(String cookie) throws IOException {
        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            URL url = Urls.subredditListUrl();
            conn = (HttpURLConnection) url.openConnection();
            setCommonHeaders(conn, cookie);
            conn.connect();

            in = conn.getInputStream();
            JsonReader reader = new JsonReader(new InputStreamReader(in));
            SubredditParser parser = new SubredditParser();
            parser.parseListingObject(reader);
            return parser.results;
        } finally {
            close(in, conn);
        }
    }

    public static ArrayList<Thing> queryThings(Context context, URL url, String cookie,
            String parentSubreddit, List<Thing> initThings) throws IOException {
        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            setCommonHeaders(conn, cookie);
            conn.connect();

            in = conn.getInputStream();
            JsonReader reader = new JsonReader(new InputStreamReader(in));
            ThingParser parser = new ThingParser(context, parentSubreddit, initThings);
            parser.parseListingObject(reader);
            return parser.things;
        } finally {
            close(in, conn);
        }
    }

    public static ArrayList<Comment> queryComments(Context context, URL url, String cookie)
            throws IOException {
        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            setCommonHeaders(conn, cookie);
            conn.connect();

            in = conn.getInputStream();
            JsonReader reader = new JsonReader(new InputStreamReader(in));
            CommentParser parser = new CommentParser(context);
            parser.parseListingArray(reader);
            return parser.comments;
        } finally {
            close(in, conn);
        }
    }

    public static Subreddit querySidebar(Context context, String subreddit, String cookie)
            throws IOException {
        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            URL url = Urls.sidebarUrl(subreddit);
            conn = (HttpURLConnection) url.openConnection();
            setCommonHeaders(conn, cookie);
            conn.connect();

            in = conn.getInputStream();
            JsonReader reader = new JsonReader(new InputStreamReader(in));
            SidebarParser parser = new SidebarParser(context);
            parser.parseEntity(reader);
            return parser.results;
        } finally {
            close(in, conn);
        }
    }

    public static LoginResult login(Context context, String login, String password)
            throws IOException {
        HttpsURLConnection conn = null;
        InputStream in = null;
        try {
            URL url = Urls.loginUrl(login);
            conn = (HttpsURLConnection) url.openConnection();
            setCommonHeaders(conn, null);
            setFormDataHeaders(conn);
            conn.connect();

            writeFormData(conn, Urls.loginQuery(login, password));
            in = conn.getInputStream();
            return LoginParser.parseResponse(in);
        } finally {
            close(in, conn);
        }
    }

    public static void submit(String subreddit, String title, String text, String cookie,
            String modhash) throws IOException {
        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            URL url = Urls.submitUrl();
            conn = (HttpURLConnection) url.openConnection();
            setCommonHeaders(conn, cookie);
            setFormDataHeaders(conn);
            conn.connect();

            writeFormData(conn, Urls.submitTextQuery(modhash, subreddit, title, text));
            in = conn.getInputStream();
            if (DEBUG) {
                logResponse(in);
            }
        } finally {
            close(in, conn);
        }
    }

    static void subscribe(String cookie, String modhash, String subreddit, boolean subscribe)
            throws IOException {
        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            URL url = Urls.subscribeUrl();
            conn = (HttpURLConnection) url.openConnection();
            setCommonHeaders(conn, cookie);
            setFormDataHeaders(conn);
            conn.connect();

            writeFormData(conn, Urls.subscribeQuery(modhash, subreddit, subscribe));
            in = conn.getInputStream();
            if (DEBUG) {
                logResponse(in);
            }
        } finally {
            close(in, conn);
        }
    }

    public static void vote(Context context, String name, int vote, String cookie,
            String modhash) throws IOException {
        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            URL url = Urls.voteUrl();
            conn = (HttpURLConnection) url.openConnection();
            setCommonHeaders(conn, cookie);
            setFormDataHeaders(conn);
            conn.connect();

            writeFormData(conn, Urls.voteQuery(modhash, name, vote));
            in = conn.getInputStream();
            if (DEBUG) {
                logResponse(in);
            }
        } finally {
            close(in, conn);
        }
    }

    private static void setCommonHeaders(HttpURLConnection conn, String cookie) {
        conn.setRequestProperty("Accept-Charset", CHARSET);
        conn.setRequestProperty("User-Agent", USER_AGENT);
        if (!TextUtils.isEmpty(cookie)) {
            conn.setRequestProperty("Cookie", Urls.loginCookie(cookie));
        }
    }

    private static void setFormDataHeaders(HttpURLConnection conn) {
        conn.setRequestProperty("Content-Type", CONTENT_TYPE);
        conn.setDoOutput(true);
    }

    private static void writeFormData(HttpURLConnection conn, String data) throws IOException {
        OutputStream output = null;
        try {
            output = conn.getOutputStream();
            output.write(data.getBytes(CHARSET));
            output.close();
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    private static void logResponse(InputStream in) {
        Scanner sc = new Scanner(in);
        while (sc.hasNextLine()) {
            Log.d(TAG, sc.nextLine());
        }
        sc.close();
    }

    private static void close(InputStream in, HttpURLConnection conn) throws IOException {
        if (in != null) {
            in.close();
        }
        if (conn != null) {
            conn.disconnect();
        }
    }
}
