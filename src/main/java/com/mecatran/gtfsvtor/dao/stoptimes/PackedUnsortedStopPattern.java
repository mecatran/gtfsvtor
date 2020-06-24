package com.mecatran.gtfsvtor.dao.stoptimes;

import java.util.Arrays;

public class PackedUnsortedStopPattern {

	private int size;
	private long[] sdata; // stop ID index and seq
	private float[] pdata = null; // shape dist
	private String[] hdata = null; // headsigns
	private Integer hashcode;

	public PackedUnsortedStopPattern(int initialSize) {
		this.sdata = new long[initialSize];
		this.hashcode = Arrays.hashCode(sdata);
	}

	public int size() {
		return size;
	}

	public int getDataSize() {
		// Note: we do not include headsign string size in the output
		// Since they are interned, the computation is rather complex
		return sdata.length * 8 + (pdata == null ? 0 : pdata.length * 4)
				+ (hdata == null ? 0 : hdata.length * 4);
	}

	public void addData(long s, Double shapeDist, String headsign) {
		if (size == sdata.length) {
			grow();
		}
		sdata[size] = s;
		if (shapeDist != null) {
			if (this.pdata == null) {
				// Lazy allocate
				this.pdata = new float[this.sdata.length];
				for (int i = 0; i < size; i++)
					this.pdata[i] = Float.NaN;
			}
			this.pdata[size] = shapeDist.floatValue();
		} else if (this.pdata != null) {
			// Mark as null
			this.pdata[size] = Float.NaN;
		}
		if (headsign != null) {
			if (this.hdata == null) {
				// Lazy allocate
				this.hdata = new String[this.sdata.length];
				// Default to null
			}
			this.hdata[size] = headsign.intern();
		}
		size++;
		hashcode = null;
	}

	public long getSData(int i) {
		return sdata[i];
	}

	public Double getShapeDist(int i) {
		if (pdata == null)
			return null;
		Float f = pdata[i];
		if (Float.isNaN(f))
			return null;
		return f.doubleValue();
	}

	public String getHeadsign(int i) {
		if (hdata == null)
			return null;
		return hdata[i];
	}

	private void grow() {
		int len = sdata.length * 2;
		long[] sdata2 = new long[len];
		System.arraycopy(sdata, 0, sdata2, 0, sdata.length);
		sdata = sdata2;
		if (pdata != null) {
			float[] pdata2 = new float[len];
			System.arraycopy(pdata, 0, pdata2, 0, pdata.length);
			pdata = pdata2;
		}
		if (hdata != null) {
			String[] hdata2 = new String[len];
			System.arraycopy(hdata, 0, hdata2, 0, hdata.length);
			hdata = hdata2;
		}
	}

	@Override
	public int hashCode() {
		if (hashcode == null) {
			hashcode = Arrays.hashCode(sdata) + 31
					* (Arrays.hashCode(pdata) + 31 * Arrays.hashCode(hdata));
		}
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
