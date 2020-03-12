package com.mecatran.gtfsvtor.utils;

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
}
