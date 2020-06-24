package com.mecatran.gtfsvtor.loader.impl;

import java.util.ArrayList;
import java.util.List;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.loader.DataRow;
import com.mecatran.gtfsvtor.loader.TableSourceInfo;
import com.mecatran.gtfsvtor.model.DataObjectSourceRef;

public class UnivocityCsvDataRow implements DataRow {

	private String[] record;
	private UnivocityCsvDataTable csvDataTable;

	public UnivocityCsvDataRow(UnivocityCsvDataTable csvDataTable,
			String[] record) {
		this.csvDataTable = csvDataTable;
		this.record = record;
	}

	@Override
	public String getString(String field) {
		int index = csvDataTable.fieldIndex(field);
		if (index < 0 || index >= record.length)
			return null;
		String ret = record[index];
		if (ret == null)
			return null;
		if (ret.isEmpty())
			return null;
		return ret;
	}

	@Override
	public DataObjectSourceInfo getSourceInfo() {
		TableSourceInfo tableSourceInfo = csvDataTable.getTableSourceInfo();
		List<String> headerColumns = tableSourceInfo.getHeaderColumns();
		List<String> fields = new ArrayList<>(headerColumns.size());
		for (String field : record) {
			fields.add(field == null ? null : field.intern());
		}
		return new DataObjectSourceInfoImpl(tableSourceInfo, fields,
				csvDataTable.getCurrentLineNumber());
	}

	@Override
	public DataObjectSourceRef getSourceRef() {
		return new DataObjectSourceRef(csvDataTable.getTableName(),
				csvDataTable.getCurrentLineNumber());
	}

	@Override
	public int getRecordCount() {
		return record.length;
	}
}
