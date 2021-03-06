package com.mecatran.gtfsvtor.loader.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

import com.mecatran.gtfsvtor.loader.DataTable;
import com.mecatran.gtfsvtor.loader.NamedInputStreamSource;
import com.mecatran.gtfsvtor.loader.NamedTabularDataSource;

public class CsvDataSource implements NamedTabularDataSource {

	private NamedInputStreamSource inputStreamSource;
	private DataTable.Factory dataTableFactory = UnivocityCsvDataTable
			.factory();

	public CsvDataSource(NamedInputStreamSource inputStreamSource) {
		this.inputStreamSource = inputStreamSource;
	}

	public CsvDataSource setDataTableFactory(
			DataTable.Factory dataTableFactory) {
		this.dataTableFactory = dataTableFactory;
		return this;
	}

	@Override
	public DataTable getDataTable(String tableName) throws IOException {
		if (inputStreamSource == null)
			throw new IOException("Missing input");
		InputStream in = inputStreamSource.getInputStream(tableName);
		return dataTableFactory.createDataTable(tableName, in);
	}

	@Override
	public Collection<String> getUnreadEntries() {
		return inputStreamSource == null ? Collections.emptyList()
				: inputStreamSource.getUnreadEntries();
	}
}
