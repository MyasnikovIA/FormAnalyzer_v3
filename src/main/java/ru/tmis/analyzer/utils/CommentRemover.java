// utils/CommentRemover.java
package ru.tmis.analyzer.utils;

import java.util.regex.Pattern;

public class CommentRemover {

    private static final Pattern ALL_COMMENTS_PATTERN = Pattern.compile(
            "(?s)" +
                    "/\\*.*?\\*/|" +
                    "<!--.*?-->|" +
                    "(?m)^\\s*--[^\\n]*|\\s+--[^\\n]*|" +
                    "(?m)^\\s*//[^\\n]*|\\s+//[^\\n]*"
    );

    public static String removeAllComments(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        return ALL_COMMENTS_PATTERN.matcher(content).replaceAll("");
    }
}