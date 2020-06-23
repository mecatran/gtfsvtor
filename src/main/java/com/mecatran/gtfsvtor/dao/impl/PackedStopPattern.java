package com.mecatran.gtfsvtor.dao.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PackedStopPattern {

	private byte[] sdata;
	private List<String> headsigns;
	private int hashcode;

	public PackedStopPattern(byte[] sdata, List<String> headsigns) {
		this.sdata = sdata;
		this.headsigns = headsigns == null || headsigns.isEmpty() ? null
				: headsigns;
		this.hashcode = Arrays.hashCode(sdata);
		if (this.headsigns != null)
			this.hashcode += 31 * headsigns.hashCode();
	}

	public byte[] getSData() {
		return sdata;
	}

	public int getSDataSize() {
		return sdata.length * 1;
	}

	public List<String> getHeadsigns() {
		return headsigns;
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
		if (!(another instanceof PackedStopPattern))
			return false;
		PackedStopPattern other = (PackedStopPattern) another;
		return Arrays.equals(sdata, other.sdata)
				&& Objects.equals(headsigns, other.headsigns);
	}
}
