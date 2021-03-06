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

package com.btmura.android.reddit.text;

public class MarkdownFormatter_RelativeLinksTest extends AbstractFormatterTest {

  public void testFormat_subreddit() {
    CharSequence s = assertSubredditFormat("/r/food", "/r/food");
    assertSubredditSpan(s, 0, 7, "food");

    s = assertSubredditFormat("/r/food/", "/r/food/");
    assertSubredditSpan(s, 0, 8, "food");

    s = assertSubredditFormat("/r/under_score/", "/r/under_score/");
    assertSubredditSpan(s, 0, 15, "under_score");

    s = assertSubredditFormat("/r/plus+minus/", "/r/plus+minus/");
    assertSubredditSpan(s, 0, 15, "plus+minus");
  }

  public void testFormat_user() {
    CharSequence s = assertSubredditFormat("/u/dude", "/u/dude");
    assertUserSpan(s, 0, 7, "dude");

    s = assertSubredditFormat("/u/dude/", "/u/dude/");
    assertUserSpan(s, 0, 8, "dude");
  }
}
