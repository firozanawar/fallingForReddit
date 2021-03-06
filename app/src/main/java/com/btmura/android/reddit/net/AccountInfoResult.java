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

import android.util.JsonReader;

import com.btmura.android.reddit.util.JsonParser;

import java.io.IOException;

public class AccountInfoResult extends JsonParser {

  /** Amount of link karma. */
  public int linkKarma;

  /** Amount of comment karma. */
  public int commentKarma;

  /** True if the account has mail. False otherwise. */
  public boolean hasMail;

  static AccountInfoResult getMyInfo(JsonReader r) throws IOException {
    AccountInfoResult result = new AccountInfoResult();
    result.parseEntityData(r);
    return result;
  }

  static AccountInfoResult getUserInfo(JsonReader r) throws IOException {
    AccountInfoResult result = new AccountInfoResult();
    result.parseEntity(r);
    return result;
  }

  private AccountInfoResult() {
  }

  @Override
  public void onLinkKarma(JsonReader r, int i) throws IOException {
    linkKarma = readInt(r, 0);
  }

  @Override
  public void onCommentKarma(JsonReader r, int i) throws IOException {
    commentKarma = readInt(r, 0);
  }

  @Override
  public void onHasMail(JsonReader r, int i) throws IOException {
    hasMail = readBoolean(r, false);
  }
}