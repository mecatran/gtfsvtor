package com.mecatran.gtfsvtor.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Stream;

public class GenericInterner<T> {

	private Map<T, T> cache;

	public GenericInterner() {
		this(true);
	}

	public GenericInterner(boolean weak) {
		if (weak)
			cache = new WeakHashMap<>();
		else
			cache = new HashMap<>();
	}

	public int size() {
		return cache.size();
	}

	public T intern(T t) {
		return cache.computeIfAbsent(t, t2 -> t2);
	}

	public Stream<T> all() {
		return cache.values().stream();
	}
}
