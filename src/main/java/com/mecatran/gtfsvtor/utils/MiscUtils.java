package com.mecatran.gtfsvtor.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MiscUtils {

	public static <T extends Comparable<T>> int listCompare(List<T> o1,
			List<T> o2) {
		for (int i = 0; i < o1.size() && i < o2.size(); i++) {
			int cmp = o1.get(i).compareTo(o2.get(i));
			if (cmp != 0)
				return cmp;
		}
		// Ran out of comparable elements, break tie: smaller list first
		return Integer.compare(o1.size(), o2.size());
	}

	public static <T extends Comparable<T>> Comparator<List<T>> listComparator() {
		return new Comparator<List<T>>() {
			@Override
			public int compare(List<T> o1, List<T> o2) {
				return listCompare(o1, o2);
			}
		};
	}

	public static List<String> wordProcessorSplit(String longText,
			int maxWidth) {
		List<String> lines = new ArrayList<>();
		for (String paragraph : longText.split("\n")) {
			lines.addAll(wordProcessorSplitParagraph(paragraph, maxWidth));
		}
		return lines;
	}

	public static List<String> wordProcessorSplitParagraph(String longText,
			int maxWidth) {
		List<String> lines = new ArrayList<>(longText.length() * 2 / maxWidth);
		String[] words = longText.split(" ");
		int wordIndex = 0;
		while (wordIndex < words.length) {
			List<String> lineWords = new ArrayList<>();
			int lineLen = 0;
			String word = words[wordIndex];
			wordIndex++;
			lineWords.add(word);
			lineLen += word.length();
			while (wordIndex < words.length) {
				word = words[wordIndex];
				if (lineLen + 1 + word.length() > maxWidth)
					break;
				lineWords.add(word);
				wordIndex++;
				lineLen += 1 + word.length();
			}
			lines.add(String.join(" ", lineWords));
		}
		return lines;
	}
}
