package com.mecatran.gtfsvtor.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class GtfsCompositeId<U, V extends GtfsObject<List<U>>>
		implements GtfsId<List<U>, V> {

	private List<U> ids;

	protected GtfsCompositeId(List<GtfsId<U, ?>> ids) {
		if (ids == null) {
			throw new IllegalArgumentException(
					"NULL composite IDs are not allowed.");
		}
		this.ids = ids.stream()
				.map(id -> id == null ? null : id.getInternalId())
				.collect(Collectors.toList());
	}

	@SafeVarargs
	protected GtfsCompositeId(U... ids) {
		if (ids == null) {
			throw new IllegalArgumentException(
					"NULL composite IDs are not allowed.");
		}
		this.ids = Arrays.asList(ids);
	}

	@Override
	public List<U> getInternalId() {
		return this.ids;
	}

	@Override
	public int hashCode() {
		return ids.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof GtfsCompositeId)) {
			return false;
		}
		@SuppressWarnings("unchecked")
		GtfsCompositeId<U, V> other = (GtfsCompositeId<U, V>) obj;
		return Objects.equals(ids, other.ids);
	}

	@Override
	public String toString() {
		/*
		 * Be careful, this toString() will end-up in reports. Be consistent.
		 */
		return "{" + ids.stream().map(s -> s == null ? "" : s.toString())
				.collect(Collectors.joining(", ")) + "}";
	}

}
