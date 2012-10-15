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

package com.btmura.android.reddit.database;

/**
 * Class containing logic for inserting and deleting new comments.
 */
public class CommentLogic {

    public interface CommentList {
        /** @return number of comments in this listing */
        int getCommentCount();

        /** @return nesting of the comment at the given position */
        int getCommentNesting(int position);

        /** @return sequence of the comment at the given position */
        int getCommentSequence(int position);
    }

    /**
     * @return nesting for a new comment that is a response to the comment at
     *         the given position
     */
    public static int getInsertNesting(CommentList list, int position) {
        int nesting = list.getCommentNesting(position);
        // Nest an additional level if this is a response to a comment.
        if (position != 0) {
            nesting++;
        }
        return nesting;
    }

    /**
     * @return sequence for a new comment that is a response to the comment at
     *         the given position
     */
    public static int getInsertSequence(CommentList list, int position) {
        int insertPosition = getInsertPosition(list, position);
        return list.getCommentSequence(insertPosition - 1);
    }

    /**
     * @return position to insert this new comment in the list
     */
    public static int getInsertPosition(CommentList list, int position) {
        // Sequence in response to header comment should be the last.
        // Sequence in response to comment should be the last in responses.
        if (position == 0) {
            return list.getCommentCount();
        } else {
            int nesting = list.getCommentNesting(position);
            int count = list.getCommentCount();
            for (int i = position + 1; i < count; i++) {
                int nextNesting = list.getCommentNesting(i);
                if (nesting + 1 == nextNesting) {
                    position = i;
                } else {
                    break;
                }
            }
            return position + 1;
        }
    }

    /**
     * @return whether a comment is completely removable. If it has children,
     *         then we will show [deleted] rather than erasing it completely.
     */
    public static boolean hasChildren(CommentList list, int position) {
        int nesting = list.getCommentNesting(position);
        if (position + 1 < list.getCommentCount()) {
            int nextNesting = list.getCommentNesting(position + 1);

            // If the next comment has the same nesting, then this comment must
            // have no children. It's safe to remove completely.
            if (nesting == nextNesting) {
                return false;
            }

            // If the next comment is nested once more, then it's reply to this
            // comment, so we want to just mark this comment as [deleted].
            if (nesting + 1 == nextNesting) {
                return true;
            }
        }

        // If this is the last comment, then there are no replies. It's safe to
        // remove completely.
        return false;
    }

    private CommentLogic() {
    }
}