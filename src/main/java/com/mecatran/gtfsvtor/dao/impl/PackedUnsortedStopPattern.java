package com.mecatran.gtfsvtor.dao.impl;

import java.util.Arrays;

public class PackedUnsortedStopPattern {

	private long[] sdata;
	private int hashcode;

	public PackedUnsortedStopPattern(long[] sdata) {
		this.sdata = sdata;
		this.hashcode = Arrays.hashCode(sdata);
	}

	public long[] getSData() {
		return sdata;
	}

	public int getSDataSize() {
		return sdata.length * 8;
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
		if (!(another instanceof PackedUnsortedStopPattern))
			return false;
		PackedUnsortedStopPattern other = (PackedUnsortedStopPattern) another;
		return Arrays.equals(sdata, other.sdata);
	}
}
