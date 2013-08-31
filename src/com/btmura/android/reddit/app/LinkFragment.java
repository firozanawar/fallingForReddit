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

import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.btmura.android.reddit.R;
import com.btmura.android.reddit.util.Strings;

public class LinkFragment extends Fragment {

    public static final String TAG = "LinkFragment";

    private static final String ARG_URL = "url";

    private static final String STATE_URL = "url";

    private static final Pattern PATTERN_IMAGE = Pattern.compile(".*\\.(jpg|png|gif)$",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private WebView webView;
    private ProgressBar progress;

    public static LinkFragment newInstance(CharSequence url) {
        Bundle b = new Bundle(1);
        b.putCharSequence(ARG_URL, url);
        LinkFragment frag = new LinkFragment();
        frag.setArguments(b);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.link, container, false);
        webView = (WebView) view.findViewById(R.id.link);
        progress = (ProgressBar) view.findViewById(R.id.progress);
        setupWebView(webView);
        return view;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setPluginState(PluginState.ON_DEMAND);
        settings.setUseWideViewPort(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progress.setVisibility(View.GONE);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progress.setProgress(newProgress);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String url;
        if (savedInstanceState != null) {
            url = savedInstanceState.getString(STATE_URL);
        } else {
            url = getUrlArgument();
        }
        if (PATTERN_IMAGE.matcher(url).matches()) {
            String img = String.format("<img src=\"%s\" width=\"100%%\" />", url);
            webView.loadData(img, "text/html", null);
        } else {
            webView.loadUrl(url);
        }
    }

    private String getUrlArgument() {
        return Strings.safeToString(getArguments().getCharSequence(ARG_URL));
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    public void onPause() {
        webView.onPause();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_URL, getCurrentUrl());
    }

    private String getCurrentUrl() {
        return !TextUtils.isEmpty(webView.getUrl()) ? webView.getUrl() : getUrlArgument();
    }

    @Override
    public void onDetach() {
        webView.destroy();
        webView = null;
        progress = null;
        super.onDetach();
    }
}
