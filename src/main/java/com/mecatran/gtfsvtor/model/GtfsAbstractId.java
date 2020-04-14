package com.mecatran.gtfsvtor.model;

import java.util.Objects;

public abstract class GtfsAbstractId<U, V extends GtfsObject<U>>
		implements GtfsId<U, V> {

	private U id;

	/**
	 * TODO Implement internal ID interning (caching)
	 */
	protected GtfsAbstractId(U id) {
		if (id == null) {
			// Should we do that?
			throw new IllegalArgumentException("NULL IDs are not allowed.");
		}
		this.id = id;
	}

	@Override
	public U getInternalId() {
		return id;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	protected boolean doEquals(Object obj,
			Class<? extends GtfsAbstractId<U, V>> clazz) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!obj.getClass().equals(clazz)) {
			return false;
		}
		@SuppressWarnings("unchecked")
		GtfsAbstractId<U, V> other = (GtfsAbstractId<U, V>) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return id == null ? "(null)" : id.toString();
	}
}
