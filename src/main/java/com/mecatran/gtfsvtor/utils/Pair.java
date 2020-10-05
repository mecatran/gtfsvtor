package com.mecatran.gtfsvtor.utils;

import java.util.Objects;

public class Pair<E1, E2> {

	private E1 a;
	private E2 b;

	public Pair(E1 a, E2 b) {
		this.a = a;
		this.b = b;
	}

	public E1 getFirst() {
		return a;
	}

	public E2 getSecond() {
		return b;
	}

	@Override
	public int hashCode() {
		return Objects.hash(a, b);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Pair<?, ?> other = (Pair<?, ?>) obj;
		return Objects.equals(a, other.a) && Objects.equals(b, other.b);
	}

	public String toString() {
		return "{" + a + ", " + b + "}";
	}
}
