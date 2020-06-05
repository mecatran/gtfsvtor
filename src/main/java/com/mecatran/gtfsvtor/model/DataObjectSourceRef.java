package com.mecatran.gtfsvtor.model;

import java.util.Objects;

public class DataObjectSourceRef implements Comparable<DataObjectSourceRef> {

	private String tableName;
	private long lineNumber;

	public DataObjectSourceRef(String tableName, long lineNumber) {
		this.tableName = tableName;
		this.lineNumber = lineNumber;
	}

	public String getTableName() {
		return tableName;
	}

	public long getLineNumber() {
		return lineNumber;
	}

	@Override
	public int compareTo(DataObjectSourceRef o) {
		int cmp = tableName.compareTo(o.getTableName());
		if (cmp != 0)
			return cmp;
		return Long.compare(lineNumber, o.getLineNumber());
	}

	@Override
	public int hashCode() {
		return Objects.hash(tableName, lineNumber);
	}

	@Override
	public boolean equals(Object another) {
		if (another == null)
			return false;
		if (another == this)
			return true;
		if (!(another instanceof DataObjectSourceRef))
			return false;
		DataObjectSourceRef otherRef = (DataObjectSourceRef) another;
		return otherRef.lineNumber == lineNumber
				&& Objects.equals(otherRef.tableName, tableName);
	}

	@Override
	public String toString() {
		return tableName + ", L" + lineNumber;
	}
}
