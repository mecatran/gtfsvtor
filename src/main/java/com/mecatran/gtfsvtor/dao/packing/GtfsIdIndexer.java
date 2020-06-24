package com.mecatran.gtfsvtor.dao.packing;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mecatran.gtfsvtor.model.GtfsId;
import com.mecatran.gtfsvtor.model.GtfsObject;
import com.mecatran.gtfsvtor.model.GtfsStop;

public class GtfsIdIndexer<U, V extends GtfsObject<U>, T extends GtfsId<U, V>> {

	private BiMap<T, Integer> indexes = HashBiMap.create();

	// Keep 0 for special values if needed
	// TODO Insert 0 for null
	private int nextIndex = 1;

	public GtfsIdIndexer() {
	}

	public static class GtfsStopIdIndexer
			extends GtfsIdIndexer<String, GtfsStop, GtfsStop.Id> {
	};

	/**
	 * Insert the value if not present, return the associated index
	 * 
	 * @param id
	 * @return
	 */
	public int index(T id) {
		return indexes.computeIfAbsent(id, id2 -> nextIndex++);
	}

	public T unindex(int index) {
		return indexes.inverse().get(index);
	}
}
