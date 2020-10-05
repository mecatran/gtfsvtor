package com.mecatran.gtfsvtor.utils;

import java.util.Objects;

public class Triplet<E1, E2, E3> {

	private E1 a;
	private E2 b;
	private E3 c;

	public Triplet(E1 a, E2 b, E3 c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public E1 getFirst() {
		return a;
	}

	public E2 getSecond() {
		return b;
	}

	public E3 getThird() {
		return c;
	}

	@Override
	public int hashCode() {
		return Objects.hash(a, b, c);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Triplet<?, ?, ?> other = (Triplet<?, ?, ?>) obj;
		return Objects.equals(a, other.a) && Objects.equals(b, other.b)
				&& Objects.equals(c, other.c);
	}

	public String toString() {
		return "{" + a + ", " + b + ", " + c + "}";
	}
}
