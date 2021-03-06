package com.mecatran.gtfsvtor.dao.stoptimes;

import java.util.Arrays;

public class PackedTimePattern {

	private byte[] tdata;
	private int hashcode;

	public PackedTimePattern(byte[] tdata) {
		this.tdata = tdata;
		this.hashcode = Arrays.hashCode(tdata);
	}

	public byte[] getTData() {
		return tdata;
	}

	public int getTDataSize() {
		return tdata.length * 1;
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
		if (!(another instanceof PackedTimePattern))
			return false;
		PackedTimePattern other = (PackedTimePattern) another;
		return Arrays.equals(tdata, other.tdata);
	}
}
