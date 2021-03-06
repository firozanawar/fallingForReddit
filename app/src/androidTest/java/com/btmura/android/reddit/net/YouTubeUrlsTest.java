/*
 * Copyright (C) 2013 Brian Muramatsu
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

import junit.framework.TestCase;

public class YouTubeUrlsTest extends TestCase {

  private static final String ATTRIBUTION_URL = "http://www.youtube.com/attribution_link"
      + "?u=%2Fwatch%3Fv%3Df00b4r%26feature%3Dshare&a=Q_FoCesxGPeVZUPTnF3jmg";

  private static final String BAD_ATTRIBUTION_URL = "http://www.youtube.com/attribution_link"
      + "?u=%2Fwatch%3Ffeature%3Dshare&a=Q_FoCesxGPeVZUPTnF3jmg";

  public void testIsYouTubeVideoUrl() throws Exception {
    assertYouTubeUrl("http://www.youtube.com/watch?v=foobar");
    assertYouTubeUrl("http://youtube.com/watch?v=foobar");
    assertNotYouTubeUrl("http://www.youtube.com");
  }

  public void testIsYouTubeVideoUrl_youtuBe() {
    assertYouTubeUrl("http://youtu.be/foobar");
    assertYouTubeUrl("http://www.youtu.be/foobar");
    assertNotYouTubeUrl("http://youtu.be");
  }

  public void testIsYouTubeVideoUrl_attributionLink() {
    assertYouTubeUrl(ATTRIBUTION_URL);
    assertNotYouTubeUrl(BAD_ATTRIBUTION_URL);
  }

  public void testGetVideoId() throws Exception {
    assertVideoId("http://youtube.com/watch?v=fOObar", "fOObar");
    assertVideoId("http://www.youtube.com/watch?v=foobar", "foobar");
    assertNoVideoId("http://youtube.com");
  }

  public void testGetVideoId_youtuBe() {
    assertVideoId("http://youtu.be/fOObar", "fOObar");
    assertVideoId("http://www.youtu.be/foobar", "foobar");
    assertNoVideoId("http://youtu.be");
  }

  public void testGetVideoId_attributionLink() {
    assertVideoId(ATTRIBUTION_URL, "f00b4r");
    assertNoVideoId(BAD_ATTRIBUTION_URL);
  }

  private void assertYouTubeUrl(String url) {
    assertTrue(YouTubeUrls.isYouTubeVideoUrl(url));
  }

  private void assertNotYouTubeUrl(String url) {
    assertFalse(YouTubeUrls.isYouTubeVideoUrl(url));
  }

  private void assertVideoId(String url, String expectedVideoId) {
    assertEquals(expectedVideoId, YouTubeUrls.getVideoId(url));
  }

  private void assertNoVideoId(String url) {
    assertNull(YouTubeUrls.getVideoId(url));
  }
}
