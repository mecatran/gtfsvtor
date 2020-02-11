package com.mecatran.gtfsvtor.loader.impl;

import java.util.List;
import java.util.Objects;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.loader.TableSourceInfo;

public class DataObjectSourceInfoImpl implements DataObjectSourceInfo {

	private TableSourceInfo table;
	private List<String> fields;
	private long lineNumber;

	public DataObjectSourceInfoImpl(TableSourceInfo table) {
		this.table = table;
		this.fields = null;
		this.lineNumber = 1L; // Hack alert
	}

	public DataObjectSourceInfoImpl(TableSourceInfo table, List<String> fields,
			long lineNumber) {
		this.table = table;
		this.fields = fields;
		this.lineNumber = lineNumber;
	}

	@Override
	public TableSourceInfo getTable() {
		return table;
	}

	@Override
	public List<String> getFields() {
		return fields;
	}

	@Override
	public long getLineNumber() {
		return lineNumber;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(lineNumber);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof DataObjectSourceInfo))
			return false;
		DataObjectSourceInfo other = (DataObjectSourceInfo) obj;
		return lineNumber == other.getLineNumber()
				&& Objects.equals(table, other.getTable());
	}

	@Override
	public int compareTo(DataObjectSourceInfo other) {
		int cmp = table.compareTo(other.getTable());
		if (cmp != 0)
			return cmp;
		return Long.compare(lineNumber, other.getLineNumber());
	}
}
