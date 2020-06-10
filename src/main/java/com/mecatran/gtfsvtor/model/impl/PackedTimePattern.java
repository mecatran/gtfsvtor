package com.mecatran.gtfsvtor.model.impl;

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
