package com.mecatran.gtfsvtor.dao.stoptimes;

import java.util.Arrays;

public class PackedUnsortedTimePattern {

	private int size;
	private long[] tdata;
	private Integer hashcode = null;

	public PackedUnsortedTimePattern(int initialSize) {
		this.tdata = new long[initialSize];
	}

	public int size() {
		return size;
	}

	public int getDataSize() {
		return tdata.length * 8;
	}

	public void addTData(long t) {
		if (size == tdata.length) {
			grow();
		}
		tdata[size] = t;
		size++;
		hashcode = null;
	}

	public long getTData(int i) {
		return tdata[i];
	}

	private void grow() {
		int len = tdata.length * 2;
		long[] tdata2 = new long[len];
		System.arraycopy(tdata, 0, tdata2, 0, tdata.length);
		tdata = tdata2;
	}

	@Override
	public int hashCode() {
		if (hashcode == null)
			hashcode = Arrays.hashCode(tdata);
		return hashcode;
	}

	@Override
	public boolean equals(Object another) {
		if (another == null)
			return false;
		if (another == this)
			return true;
		if (!(another instanceof PackedUnsortedTimePattern))
			return false;
		PackedUnsortedTimePattern other = (PackedUnsortedTimePattern) another;
		return Arrays.equals(tdata, other.tdata);
	}
}
