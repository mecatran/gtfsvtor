package com.mecatran.gtfsvtor.model;

import java.util.Comparator;

/**
 * @param <U> The class of the internal ID
 * @param <V> The class of the object this ID is related to (for example:
 *        GtfsAgency).
 */
public interface GtfsId<U, V extends GtfsObject<U>> {

	public U getInternalId();

	public static <U extends Comparable<U>, V extends GtfsObject<U>> Comparator<GtfsId<U, V>> makeComparator() {
		return new Comparator<GtfsId<U, V>>() {
			@Override
			public int compare(GtfsId<U, V> o1, GtfsId<U, V> o2) {
				U u1 = o1.getInternalId();
				U u2 = o2.getInternalId();
				return u1.compareTo(u2);
			}
		};
	}
}
