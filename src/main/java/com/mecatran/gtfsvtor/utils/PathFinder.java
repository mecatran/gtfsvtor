package com.mecatran.gtfsvtor.utils;

import java.util.List;

public interface PathFinder<T> {

	public static class TraverseInfo<T> {

		private T next;
		private double cost;

		public TraverseInfo(T next, double cost) {
			this.next = next;
			this.cost = cost;
		}

		public T getNext() {
			return next;
		}

		public double getCost() {
			return cost;
		}
	}

	public interface Graph<T> {

		public double pathCostEstimate(T node, T goal);

		public Iterable<TraverseInfo<T>> neighbors(T node);
	}

	List<T> findPath(Graph<T> graph, T start, T goal);
}
