package com.mecatran.gtfsvtor.dao.impl;

import java.util.Arrays;

public class PackedUnsortedTimePattern {

	private long[] tdata;
	private int hashcode;

	public PackedUnsortedTimePattern(long[] tdata) {
		this.tdata = tdata;
		this.hashcode = Arrays.hashCode(tdata);
	}

	public long[] getTData() {
		return tdata;
	}

	public int getTDataSize() {
		return tdata.length * 8;
	}

	@Override
	public int hashCode() {
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
