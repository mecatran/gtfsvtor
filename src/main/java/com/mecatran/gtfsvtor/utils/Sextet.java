package com.mecatran.gtfsvtor.utils;

import java.util.Objects;

public class Sextet<E1, E2, E3, E4, E5, E6> {

	private E1 a;
	private E2 b;
	private E3 c;
	private E4 d;
	private E5 e;
	private E6 f;

	public Sextet(E1 a, E2 b, E3 c, E4 d, E5 e, E6 f) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.e = e;
		this.f = f;

	}

	public E1 getFirst() {
		return a;
	}

	public E2 getSecond() {
		return b;
	}

	@Override
	public int hashCode() {
		return Objects.hash(a, b, c, d, e, f);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Sextet<?, ?, ?, ?, ?, ?> other = (Sextet<?, ?, ?, ?, ?, ?>) obj;
		return Objects.equals(a, other.a) && Objects.equals(b, other.b) &&
				Objects.equals(c, other.c) && Objects.equals(d, other.d) &&
				Objects.equals(e, other.e) && Objects.equals(f, other.f);
	}

	public String toString() {
		return "{" + a + ", " + b + ", " + c + ", " + d + ", " + e + ", " + f + "}";
	}
}
