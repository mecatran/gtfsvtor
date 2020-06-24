package com.mecatran.gtfsvtor.dao.packing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class ListPacker<U, V, W> {

	public interface PackerUnpacker<U, V, W> {

		public W pack(U id, List<V> elems);

		public List<V> unpack(U id, W w);
	}

	private static class OpenedItem<V> {
		private OpenedItem(List<V> list) {
			this.list = list;
		}

		private List<V> list;
	}

	private int initialListSize = 100;
	private LinkedHashMap<U, OpenedItem<V>> openedItems;
	private Map<U, W> packedItems = new HashMap<>();
	private int nInterleave = 0;
	private PackerUnpacker<U, V, W> packerUnpacker;
	private int elemSize = 0;
	private Function<Integer, Boolean> interleavingOverflowCallback;

	public ListPacker(PackerUnpacker<U, V, W> packerUnpacker, int maxOpened) {
		this.packerUnpacker = packerUnpacker;
		this.openedItems = new LinkedHashMap<U, OpenedItem<V>>(maxOpened, 0.75f,
				true) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(
					Map.Entry<U, OpenedItem<V>> eldest) {
				boolean remove = size() > maxOpened;
				if (remove) {
					packedItems.put(eldest.getKey(), packerUnpacker
							.pack(eldest.getKey(), eldest.getValue().list));
				}
				return remove;
			}
		};
	}

	public ListPacker<U, V, W> withInitialListSize(int size) {
		this.initialListSize = size;
		return this;
	}

	public ListPacker<U, V, W> withInterleavingOverflowCallback(
			Function<Integer, Boolean> callback) {
		this.interleavingOverflowCallback = callback;
		return this;
	}

	public void push(U id, V e) {
		boolean callCallback = false;
		OpenedItem<V> opened = openedItems.get(id);
		if (opened == null) {
			W closed = packedItems.remove(id);
			if (closed != null) {
				opened = new OpenedItem<>(packerUnpacker.unpack(id, closed));
				openedItems.put(id, opened);
				if (nInterleave == 0) {
					// Only call the overflow callback once
					callCallback = true;
				}
				nInterleave++;
			} else {
				opened = openedItems.computeIfAbsent(id,
						id2 -> new OpenedItem<>(
								new ArrayList<>(initialListSize)));
			}
		}
		opened.list.add(e);
		elemSize++;
		if (callCallback && interleavingOverflowCallback != null)
			interleavingOverflowCallback.apply(0);

	}

	public void close() {
		openedItems.forEach((id, opened) -> packedItems.put(id,
				packerUnpacker.pack(id, opened.list)));
		openedItems.clear();
		if (nInterleave > 0 && interleavingOverflowCallback == null) {
			System.out.println("Warning: " + nInterleave
					+ " interleaved items have been seen.\n"
					+ "Using our current packing implementation, this works but highly inefficient.\n"
					+ "Please increase the relevant --maxXxxInterleaving option.");
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

	public Stream<Map.Entry<U, W>> entries() {
		return packedItems.entrySet().stream();
	}
}
