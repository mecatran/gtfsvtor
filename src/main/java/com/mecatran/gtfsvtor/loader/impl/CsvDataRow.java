package com.mecatran.gtfsvtor.loader.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.loader.DataRow;

public class CsvDataRow implements DataRow {

	private CSVRecord record;
	private CsvDataTable csvDataTable;

	public CsvDataRow(CsvDataTable csvDataTable, CSVRecord record) {
		this.csvDataTable = csvDataTable;
		this.record = record;
	}

	@Override
	public String getString(String field) {
		if (record.getRecordNumber() == 1)
			csvDataTable.recordReadField(field);
		if (!record.isSet(field))
			return null;
		String ret = record.get(field);
		if (ret == null)
			return null;
		if (ret.isEmpty())
			return null;
		return ret;
	}

	@Override
	public DataObjectSourceInfo getSourceInfo() {
		List<String> headerColumns = csvDataTable.getHeaderColumns();
		List<String> fields = new ArrayList<>(headerColumns.size());
		for (String column : headerColumns) {
			try {
				fields.add(record.get(column));
			} catch (IllegalArgumentException e) {
				fields.add(null);
			}
		}
		return new DataObjectSourceInfoImpl(csvDataTable.getTableSourceInfo(),
				fields, csvDataTable.getCurrentLineNumber());
	}
}
