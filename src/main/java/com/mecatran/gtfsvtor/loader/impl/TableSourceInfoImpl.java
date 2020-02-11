package com.mecatran.gtfsvtor.loader.impl;

import java.util.List;
import java.util.Objects;

import com.mecatran.gtfsvtor.loader.TableSourceInfo;

public class TableSourceInfoImpl implements TableSourceInfo {

	private String tableName;
	private List<String> headerColumns;

	public TableSourceInfoImpl(String tableName, List<String> headerColumns) {
		this.tableName = tableName;
		this.headerColumns = headerColumns;
	}

	@Override
	public String getTableName() {
		return tableName;
	}

	@Override
	public List<String> getHeaderColumns() {
		return headerColumns;
	}

	@Override
	public int hashCode() {
		return Objects.hash(tableName);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof TableSourceInfo))
			return false;
		TableSourceInfo other = (TableSourceInfo) obj;
		return Objects.equals(tableName, other.getTableName());
	}

	@Override
	public int compareTo(TableSourceInfo other) {
		return tableName.compareTo(other.getTableName());
	}
}
