package com.mecatran.gtfsvtor.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class AStar<T> implements PathFinder<T> {

	private class State implements Comparable<State> {

		private final T node;
		private final State previous;
		private final double costFromStart;
		private final double remainingCostEstimate;

		private State(T node, State previous, double costFromStart,
				double remainingCostEstimate) {
			this.node = node;
			this.previous = previous;
			this.costFromStart = costFromStart;
			this.remainingCostEstimate = remainingCostEstimate;
		}

		private List<T> makePath() {
			List<T> result = new LinkedList<T>();
			State state = this;
			while (state != null) {
				result.add(0, state.node);
				state = state.previous;
			}
			return result;
		}

		@Override
		public int compareTo(State other) {
			return Double.compare(costFromStart + remainingCostEstimate,
					other.costFromStart + other.remainingCostEstimate);
		}

		@Override
		public String toString() {
			return String.format("Node{w=%.3f,node=%s}", costFromStart, node);
		}
	}

	private boolean _debug = false;

	@Override
	public List<T> findPath(Graph<T> graph, T start, T goal) {
		PriorityQueue<State> q = new PriorityQueue<>();
		Map<T, State> closed = new HashMap<>();
		State startState = new State(start, null, 0.0,
				graph.pathCostEstimate(start, goal));
		q.add(startState);
		if (_debug)
			System.out.println(
					"*** Computing path from " + start + " to " + goal);
		while (!q.isEmpty()) {
			if (_debug) {
				System.out.println("Q contains: " + q.size() + " elements: "
						+ Arrays.toString(q.toArray()));
				System.out.println(
						" Closed contains: " + closed.size() + " elements: "
								+ Arrays.toString(closed.values().toArray()));
			}
			State state = q.remove();
			if (_debug)
				System.out.println(" Processing w=" + state.costFromStart
						+ " + " + state.remainingCostEstimate + ", "
						+ state.node);
			if (goal.equals(state.node)) {
				// Found!
				if (_debug)
					System.out.println("*** Found goal! " + state.node);
				return state.makePath();
			} else {
				State closedState = closed.get(state.node);
				if (closedState != null
						&& closedState.costFromStart < state.costFromStart) {
					// Already visited with a better cost, bail-out.
					continue;
				}
				for (TraverseInfo<T> traverseInfo : graph
						.neighbors(state.node)) {
					T newNode = traverseInfo.getNext();
					double traverseCost = traverseInfo.getCost();
					double newCost = state.costFromStart + traverseCost;
					State closedNode = closed.get(newNode);
					if (closedNode != null
							&& closedNode.costFromStart <= newCost) {
						// Already visited with a better cost, bail-out.
						continue;
					}
					// Use previous value if available
					double costEstimate = closedNode == null
							? graph.pathCostEstimate(newNode, goal)
							: closedNode.remainingCostEstimate;
					State newState = new State(newNode, state, newCost,
							costEstimate);
					if (_debug)
						System.out.println(" Better w=" + newCost + " at node "
								+ newNode + " state " + newState);
					closed.put(newNode, newState);
					q.add(newState);
				}
			}
			closed.put(state.node, state);
		}
		return null;
	}
}
