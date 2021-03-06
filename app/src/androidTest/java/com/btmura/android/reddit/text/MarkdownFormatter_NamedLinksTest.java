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

public class MarkdownFormatter_NamedLinksTest extends AbstractFormatterTest {

  public void testFormat() {
    CharSequence cs = assertNamedLinksFormat("[foo](abc)", "foo");
    assertUrlSpan(cs, 0, 3, "http://abc");

    cs = assertNamedLinksFormat("[foo] (abc)", "foo");
    assertUrlSpan(cs, 0, 3, "http://abc");

    cs = assertNamedLinksFormat("[foo] (abc desc)", "foo");
    assertUrlSpan(cs, 0, 3, "http://abc");

    cs = assertNamedLinksFormat("[foo]\n(abc desc)", "foo");
    assertUrlSpan(cs, 0, 3, "http://abc");

    cs = assertNamedLinksFormat("[foo\n](abc desc)", "foo\n");
    assertUrlSpan(cs, 0, 3, "http://abc");
  }

  public void testFormat_nestedBrackets() {
    CharSequence cs = assertNamedLinksFormat("[[link]](/abc)", "[link]");
    assertUrlSpan(cs, 0, 6, "https://www.reddit.com/abc");
  }

  public void testFormat_nestedBrackets2() {
    CharSequence cs = assertNamedLinksFormat("[[a]](/a) [[b]](/b)", "[a] [b]");
    assertUrlSpan(cs, 0, 3, "https://www.reddit.com/a");
    assertUrlSpan(cs, 4, 7, "https://www.reddit.com/b");
  }

  public void testFormat_nestedBrackets3() {
    CharSequence cs = assertNamedLinksFormat("[[a](/a)]", "[a]");
    assertUrlSpan(cs, 0, 3, "https://www.reddit.com/a");
  }

  public void testFormat_nestedParens() {
    CharSequence cs = assertNamedLinksFormat(
        "Here is a [link](/abc (123) (456)).",
        "Here is a link.");
    assertUrlSpan(cs, 10, 14, "https://www.reddit.com/abc");
  }

  public void testFormat_nestedParens2() {
    CharSequence cs = assertNamedLinksFormat("[abc (def)](/abc (123) (456))",
        "abc (def)");
    assertUrlSpan(cs, 0, 9, "https://www.reddit.com/abc");
  }

  public void testFormat_multiple() {
    CharSequence cs = assertNamedLinksFormat(
        "[Link 1](/a (123)) and [Link 2](/b (456))",
        "Link 1 and Link 2");
    assertUrlSpan(cs, 0, 6, "https://www.reddit.com/a");
    assertUrlSpan(cs, 11, 17, "https://www.reddit.com/b");
  }

  public void testFormat_nestedParensMultipleLines() {
    CharSequence cs = assertNamedLinksFormat(
        "[Link 1](/a (123))\n[Link 2](/b (456))",
        "Link 1\nLink 2");
    assertUrlSpan(cs, 0, 6, "https://www.reddit.com/a");
    assertUrlSpan(cs, 7, 13, "https://www.reddit.com/b");
  }
}
