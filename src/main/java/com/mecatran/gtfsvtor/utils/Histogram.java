package com.mecatran.gtfsvtor.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Strings;

public class Histogram<T extends Comparable<T>> {

	private String what;
	private Map<T, AtomicInteger> hist = new HashMap<>();

	public Histogram(String what) {
		this.what = what;
	}

	public void count(T t) {
		hist.computeIfAbsent(t, t2 -> new AtomicInteger()).addAndGet(1);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(what).append("\n");
		int max = hist.values().stream().mapToInt(AtomicInteger::get).max()
				.orElse(1);
		hist.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.forEach(kv -> sb.append(String.format("%-8s: %8d %s\n",
						kv.getKey(), kv.getValue().get(),
						Strings.repeat("=", kv.getValue().get() * 80 / max))));
		return sb.toString();
	}

}
