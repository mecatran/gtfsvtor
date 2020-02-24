package com.mecatran.gtfsvtor.model;

import java.util.Comparator;

/**
 * @param <U> The class of the internal ID
 * @param <V> The class of the object this ID is related to (for example:
 *        GtfsAgency).
 */
public interface GtfsId<U extends Comparable<U>, V extends GtfsObject<U>>
		extends Comparable<GtfsId<U, V>> {

	public U getInternalId();

	@Override
	public default int compareTo(GtfsId<U, V> other) {
		return Comparator.nullsFirst(U::compareTo).compare(this.getInternalId(),
				other.getInternalId());
	}

}
