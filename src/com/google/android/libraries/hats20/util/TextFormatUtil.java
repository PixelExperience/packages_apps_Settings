package com.google.android.libraries.hats20.util;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFormatUtil {
    private static Pattern boldRegexPattern = Pattern.compile("(?<=(['\"]|\\s|^))(\\*(\\w|[.!?,'\"#$*])+\\*)(?=([.!?,'\"]|\\s|$))");
    private static Pattern italicRegexPattern = Pattern.compile("(?<=(['\"]|\\s|^))(_(\\w|[.!?,'\"#$*])+_)(?=([.!?,'\"]|\\s|$))");

    public static Spannable format(String str) {
        if (TextUtils.isEmpty(str)) {
            return new SpannableString("");
        }
        if (!str.contains(Character.toString('*')) && !str.contains(Character.toString('_'))) {
            return new SpannableString(str);
        }
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        Matcher matcher = italicRegexPattern.matcher(str);
        Matcher matcher2 = boldRegexPattern.matcher(str);
        PriorityQueue priorityQueue = new PriorityQueue();
        priorityQueue.addAll(WordMatch.parseMatches(matcher));
        priorityQueue.addAll(WordMatch.parseMatches(matcher2));
        int i = 0;
        while (i < str.length()) {
            if (isThereAFormattedItemAtStartPosition(i, priorityQueue)) {
                WordMatch wordMatch = (WordMatch) priorityQueue.remove();
                addAndFormatTextToOutput(spannableStringBuilder, wordMatch);
                i = wordMatch.end - 1;
            } else {
                spannableStringBuilder.append(str.charAt(i));
            }
            i++;
        }
        return spannableStringBuilder;
    }

    private static boolean isThereAFormattedItemAtStartPosition(int i, Queue<WordMatch> queue) {
        return !queue.isEmpty() && i == queue.peek().start;
    }

    private static void addAndFormatTextToOutput(SpannableStringBuilder spannableStringBuilder, WordMatch wordMatch) {
        char charAt = wordMatch.word.charAt(0);
        if (charAt == '*' || charAt == '_') {
            String str = wordMatch.word;
            spannableStringBuilder.append(str.substring(1, str.length() - 1));
            int i = wordMatch.end - wordMatch.start;
            int i2 = 2;
            int i3 = i - 2;
            if (charAt == '*') {
                i2 = 1;
            }
            spannableStringBuilder.setSpan(new StyleSpan(i2), spannableStringBuilder.length() - i3, spannableStringBuilder.length(), 33);
            return;
        }
        spannableStringBuilder.append(wordMatch.word);
    }

    private static class WordMatch implements Comparable<WordMatch> {
        final int end;
        final int start;
        final String word;

        WordMatch(Matcher matcher) {
            this.start = matcher.start();
            this.end = matcher.end();
            this.word = matcher.group();
        }

        public int compareTo(WordMatch wordMatch) {
            return Integer.compare(this.start, wordMatch.start);
        }

        /* access modifiers changed from: private */
        public static List<WordMatch> parseMatches(Matcher matcher) {
            ArrayList arrayList = new ArrayList();
            while (matcher.find()) {
                arrayList.add(new WordMatch(matcher));
            }
            return arrayList;
        }
    }
}
