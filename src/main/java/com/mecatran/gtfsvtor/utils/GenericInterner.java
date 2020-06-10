package com.mecatran.gtfsvtor.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class GenericInterner<T> {

	private Map<T, T> cache = new HashMap<>();

	public GenericInterner() {
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
