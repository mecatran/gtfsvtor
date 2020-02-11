package com.mecatran.gtfsvtor.model;

/**
 * @param <U> The class of the internal ID
 * @param <V> The class of the object this ID is related to (for example:
 *        GtfsAgency).
 */
public interface GtfsId<U, V extends GtfsObject<U>> {

	public U getInternalId();

}
