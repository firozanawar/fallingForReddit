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

import android.text.TextUtils;

import com.btmura.android.reddit.database.Kinds;
import com.btmura.android.reddit.util.ThingIds;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Urls {

    public static final String BASE_URL = "https://www.reddit.com";
    private static final String BASE_SSL_URL = "https://ssl.reddit.com";

    public static final String API_ACCESS_TOKEN_URL = BASE_SSL_URL + "/api/v1/access_token";

    private static final String API_COMMENTS_URL = BASE_URL + "/api/comment";
    private static final String API_COMPOSE_URL = BASE_URL + "/api/compose";
    private static final String API_DELETE_URL = BASE_URL + "/api/del";
    private static final String API_EDIT_URL = BASE_URL + "/api/editusertext";
    private static final String API_HIDE_URL = BASE_URL + "/api/hide";
    private static final String API_INFO_URL = BASE_URL + "/api/info";
    private static final String API_ME_URL = BASE_URL + "/api/me";
    private static final String API_READ_MESSAGE = BASE_URL + "/api/read_message";
    private static final String API_SAVE_URL = BASE_URL + "/api/save";
    private static final String API_SUBMIT_URL = BASE_URL + "/api/submit/";
    private static final String API_SUBSCRIBE_URL = BASE_URL + "/api/subscribe/";
    private static final String API_UNHIDE_URL = BASE_URL + "/api/unhide";
    private static final String API_UNREAD_MESSAGE = BASE_URL + "/api/unread_message";
    private static final String API_UNSAVE_URL = BASE_URL + "/api/unsave";
    private static final String API_VOTE_URL = BASE_URL + "/api/vote/";

    private static final String BASE_CAPTCHA_URL = BASE_URL + "/captcha/";

    public static CharSequence aboutMe() {
        return new StringBuilder(API_ME_URL).append(".json");
    }

    public static CharSequence captcha(String id) {
        return new StringBuilder(BASE_CAPTCHA_URL).append(id).append(".png");
    }

    public static CharSequence comments() {
        return API_COMMENTS_URL;
    }

    public static CharSequence commentsQuery(String thingId, String text, String modhash) {
        return thingTextQuery(thingId, text, modhash);
    }

    public static CharSequence edit() {
        return API_EDIT_URL;
    }

    public static CharSequence editQuery(String thingId, String text, String modhash) {
        return thingTextQuery(thingId, text, modhash);
    }

    private static CharSequence thingTextQuery(String thingId, String text, String modhash) {
        return new StringBuilder()
                .append("thing_id=").append(encode(thingId))
                .append("&text=").append(encode(text))
                .append("&uh=").append(encode(modhash))
                .append("&api_type=json");
    }

    public static CharSequence compose() {
        return API_COMPOSE_URL;
    }

    public static String composeQuery(String to, String subject, String text, String captchaId,
                                      String captchaGuess, String modhash) {
        StringBuilder b = new StringBuilder();
        b.append("to=").append(encode(to));
        b.append("&subject=").append(encode(subject));
        b.append("&text=").append(encode(text));
        if (!TextUtils.isEmpty(captchaId)) {
            b.append("&iden=").append(encode(captchaId));
        }
        if (!TextUtils.isEmpty(captchaGuess)) {
            b.append("&captcha=").append(encode(captchaGuess));
        }
        b.append("&uh=").append(encode(modhash));
        b.append("&api_type=json");
        return b.toString();
    }

    public static CharSequence delete() {
        return API_DELETE_URL;
    }

    public static CharSequence deleteQuery(String thingId, String modhash) {
        return thingQuery(thingId, modhash);
    }

    public static CharSequence hide(boolean hide) {
        return hide ? API_HIDE_URL : API_UNHIDE_URL;
    }

    public static CharSequence hideQuery(String thingId, String modhash) {
        return thingQuery(thingId, modhash);
    }

    public static CharSequence info(String thingId) {
        return new StringBuilder(API_INFO_URL)
                .append(".json?id=")
                .append(ThingIds.addTag(thingId, Kinds.getTag(Kinds.KIND_LINK)));
    }

    public static CharSequence loginCookie(String cookie) {
        StringBuilder b = new StringBuilder();
        b.append("reddit_session=").append(encode(cookie));
        return b;
    }

    public static CharSequence readMessage() {
        return API_READ_MESSAGE;
    }

    public static CharSequence readMessageQuery(String thingId, String modhash) {
        return thingQuery(thingId, modhash);
    }

    public static CharSequence saveQuery(String thingId, String modhash) {
        return thingQuery(thingId, modhash);
    }

    private static CharSequence thingQuery(String thingId, String modhash) {
        StringBuilder b = new StringBuilder();
        b.append("id=").append(encode(thingId));
        b.append("&uh=").append(encode(modhash));
        b.append("&api_type=json");
        return b;
    }

    public static CharSequence subscribeQuery(String subreddit, boolean subscribe,
                                              String modhash) {
        StringBuilder b = new StringBuilder();
        b.append("action=").append(subscribe ? "sub" : "unsub");
        b.append("&uh=").append(encode(modhash));
        b.append("&sr_name=").append(encode(subreddit));
        b.append("&api_type=json");
        return b;
    }

    public static CharSequence perma(String permaLink, String thingId) {
        StringBuilder b = new StringBuilder(BASE_URL).append(permaLink);
        if (!TextUtils.isEmpty(thingId)) {
            b.append(ThingIds.removeTag(thingId));
        }
        return b;
    }

    public static CharSequence save(boolean save) {
        return save ? API_SAVE_URL : API_UNSAVE_URL;
    }

    public static CharSequence submit() {
        return API_SUBMIT_URL;
    }

    public static CharSequence submitQuery(String subreddit,
                                           String title,
                                           String text,
                                           boolean link,
                                           String captchaId,
                                           String captchaGuess,
                                           String modhash) {
        StringBuilder b = new StringBuilder();
        b.append(link ? "kind=link" : "kind=self");
        b.append("&uh=").append(encode(modhash));
        b.append("&sr=").append(encode(subreddit));
        b.append("&title=").append(encode(title));
        b.append(link ? "&url=" : "&text=").append(encode(text));
        if (!TextUtils.isEmpty(captchaId)) {
            b.append("&iden=").append(encode(captchaId));
        }
        if (!TextUtils.isEmpty(captchaGuess)) {
            b.append("&captcha=").append(encode(captchaGuess));
        }
        b.append("&api_type=json");
        return b;
    }

    public static CharSequence subscribe() {
        return API_SUBSCRIBE_URL;
    }

    public static CharSequence unreadMessage() {
        return API_UNREAD_MESSAGE;
    }

    public static CharSequence unreadMessageQuery(String thingId, String modhash) {
        return thingQuery(thingId, modhash);
    }

    public static CharSequence vote() {
        return API_VOTE_URL;
    }

    public static CharSequence voteQuery(String thingId, int vote, String modhash) {
        StringBuilder b = new StringBuilder();
        b.append("id=").append(thingId);
        b.append("&dir=").append(encode(Integer.toString(vote)));
        b.append("&uh=").append(encode(modhash));
        b.append("&api_type=json");
        return b;
    }

    public static String encode(String param) {
        try {
            return URLEncoder.encode(param, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
