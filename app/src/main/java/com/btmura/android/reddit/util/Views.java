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

package com.btmura.android.reddit.util;

import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ListView;

public class Views {

  public static int getCheckedPosition(ListView listView) {
    SparseBooleanArray checked = listView.getCheckedItemPositions();
    int size = listView.getCount();
    for (int i = 0; i < size; i++) {
      if (checked.get(i)) {
        return i;
      }
    }
    return -1;
  }

  public static void setVisibility(int visibility, View v1, View v2) {
    if (v1 != null) {
      v1.setVisibility(visibility);
    }
    if (v2 != null) {
      v2.setVisibility(visibility);
    }
  }
}
