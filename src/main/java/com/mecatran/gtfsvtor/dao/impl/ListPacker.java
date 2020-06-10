package com.mecatran.gtfsvtor.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ListPacker<U, V, W> {

	public interface PackerUnpacker<U, V, W> {

		public W pack(U id, List<V> elems);

		public List<V> unpack(U id, W w);
	}

	// TODO Make this a list of current opened items
	private U currentId = null;
	private List<V> currentList = new ArrayList<>();
	private Map<U, W> packedItems = new HashMap<>();
	private int nInterleave = 0;
	private PackerUnpacker<U, V, W> packerUnpacker;
	private int elemSize = 0;

	public ListPacker(PackerUnpacker<U, V, W> packerUnpacker) {
		this.packerUnpacker = packerUnpacker;
	}

	public void push(U id, V e) {
		if (!id.equals(currentId)) {
			W oldW = packedItems.get(id);
			pack(currentId, currentList);
			currentList.clear();
			currentId = id;
			if (oldW != null) {
				/*
				 * This is highly inefficient using the current implementation.
				 */
				currentList.addAll(packerUnpacker.unpack(id, oldW));
				nInterleave++;
			}
		}
		currentList.add(e);
		elemSize++;
	}

	public void close() {
		pack(currentId, currentList);
		if (nInterleave > 0) {
			System.out.println("Warning: " + nInterleave
					+ " interleaved items have been seen.\n"
					+ "Using our current packing implementation, this works but highly inefficient.\n"
					+ "Contact the developers for more informations.");
			/*
			 * TODO Either provide an option to increase the max opened list
			 * count, or an option to disable the packing store.
			 */
		}
	}

	public int itemsCount() {
		return elemSize;
	}

	public int groupCount() {
		return packedItems.size();
	}

	public W get(U id) {
		return packedItems.get(id);
	}

	public Stream<W> all() {
		return packedItems.values().stream();
	}

	private void pack(U id, List<V> elems) {
		if (elems == null || elems.isEmpty())
			return;
		W packed = packerUnpacker.pack(id, elems);
		packedItems.put(id, packed);
	}
}
