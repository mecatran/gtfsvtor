package com.mecatran.gtfsvtor.loader.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mecatran.gtfsvtor.dao.AppendableDao;
import com.mecatran.gtfsvtor.dao.AppendableDao.SourceContext;
import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.loader.DataLoader;
import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.loader.DataRow;
import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.loader.DataTable;
import com.mecatran.gtfsvtor.loader.NamedTabularDataSource;
import com.mecatran.gtfsvtor.loader.schema.GtfsTableDescriptor;
import com.mecatran.gtfsvtor.loader.schema.GtfsTableSchema;
import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.model.GtfsObject;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.DeprecatedColumnWarning;
import com.mecatran.gtfsvtor.reporting.issues.DuplicatedColumnError;
import com.mecatran.gtfsvtor.reporting.issues.EmptyTableError;
import com.mecatran.gtfsvtor.reporting.issues.InconsistentNumberOfFieldsWarning;
import com.mecatran.gtfsvtor.reporting.issues.InvalidCharsetError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidEncodingError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryColumnError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryTableError;
import com.mecatran.gtfsvtor.reporting.issues.SpaceInColumnWarning;
import com.mecatran.gtfsvtor.reporting.issues.TableIOError;
import com.mecatran.gtfsvtor.reporting.issues.UnknownFileInfo;
import com.mecatran.gtfsvtor.reporting.issues.UnrecognizedColumnInfo;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

public class GtfsDataLoader implements DataLoader {

	private NamedTabularDataSource dataSource;
	private GtfsTableSchema tableSchema;

	public GtfsDataLoader(NamedTabularDataSource dataSource,
			GtfsTableSchema tableSchema) {
		this.dataSource = dataSource;
		this.tableSchema = tableSchema;
	}

	@Override
	public void load(DataLoader.Context context) {

		Set<String> loadedTables = new HashSet<>();
		for (GtfsTableDescriptor tableDescriptor : tableSchema
				.getTableDescriptors()) {
			loadTable(context, tableDescriptor, loadedTables);
		}

		reportUnreadTables(context.getReportSink());
		context.getAppendableDao().close();
	}

	private void loadTable(DataLoader.Context context,
			GtfsTableDescriptor tableDescriptor, Set<String> loadedTables) {
		String tableName = tableDescriptor.getTableName();
		DataTable table = getDataTable(tableName,
				tableDescriptor.isTableMandatory(loadedTables),
				context.getReportSink());
		if (table == null)
			return;
		loadedTables.add(tableName); // This is a bit hackish

		DataLoaderContext sourceContext = new DataLoaderContext(table, context);
		Class<? extends GtfsObject<?>> objClass = tableDescriptor
				.getObjectClass();

		int nObjects = 0;
		for (DataRow row : table) {
			DataRowConverter erow = new DataRowConverter(row,
					context.getReportSink());
			sourceContext.setAndValidateRow(row);
			GtfsObject<?> obj = tableDescriptor.parseAndSave(erow,
					sourceContext);
			context.getStreamingValidator().validate(objClass, obj,
					sourceContext);
			if (nObjects % 50000 == 0)
				System.out.print(
						"Loading " + tableName + ": " + nObjects + "...\r");
			nObjects++;
		}
		System.out.println("Loaded  " + tableName + ": " + nObjects + " rows.");
		checkColumns(context.getReportSink(), table,
				tableDescriptor.getMandatoryColumns(nObjects),
				tableDescriptor.getDeprecatedColumns(nObjects));
		closeTable(table, context.getReportSink(),
				tableDescriptor.getDeprecatedColumns(nObjects));
	}

	private DataTable getDataTable(String tableName, boolean mandatory,
			ReportSink reportSink) {
		try {
			DataTable table = dataSource.getDataTable(tableName);
			if (!table.getCharset().equals(StandardCharsets.UTF_8)) {
				reportSink.report(
						new InvalidCharsetError(tableName, table.getCharset()));
			}
			return table;
		} catch (IOException e) {
			if (mandatory) {
				reportSink.report(new MissingMandatoryTableError(tableName));
			}
			return null;
		}
	}

	private void closeTable(DataTable table, ReportSink reportSink,
			List<String> deprecatedColumns) {
		if (table.isEmpty()) {
			reportSink.report(new EmptyTableError(
					table.getTableSourceInfo().getTableName()));
		}
		for (String unknownColumn : table.getUnreadColumnHeaders()) {
			if (deprecatedColumns.contains(unknownColumn)) {
				// O(n) but n is often 0, sometimes 1
				continue;
			}
			reportSink.report(new UnrecognizedColumnInfo(
					new DataObjectSourceRef(table.getTableName(), 1L),
					unknownColumn), table.getSourceInfo());
			if (unknownColumn.chars().anyMatch(c -> c == 0xFFFD || c == 0)) {
				reportSink.report(new InvalidEncodingError(
						new DataObjectSourceRef(table.getTableName(), 1L),
						unknownColumn, unknownColumn), table.getSourceInfo());
			}
		}
		try {
			table.close();
		} catch (IOException e) {
			reportSink.report(
					new TableIOError(table.getTableSourceInfo().getTableName(),
							e.getLocalizedMessage()));
		}
	}

	private void reportUnreadTables(ReportSink reportSink) {
		for (String unreadTable : dataSource.getUnreadEntries()) {
			reportSink.report(new UnknownFileInfo(unreadTable));
		}
	}

	private void checkColumns(ReportSink reportSink, DataTable table,
			List<String> mandatoryColumnHeaders,
			List<String> deprecatedColumnHeaders) {
		Set<String> headerSet = new HashSet<>();
		List<String> columnHeaders = table.getColumnHeaders();
		List<String> rawColHeaders = table.getRawColumnHeaders();
		for (String columnHeader : columnHeaders) {
			if (headerSet.contains(columnHeader)) {
				reportSink
						.report(new DuplicatedColumnError(table.getSourceRef(),
								columnHeader), table.getSourceInfo());
			} else {
				headerSet.add(columnHeader);
			}
		}
		for (String columnHeader : mandatoryColumnHeaders) {
			if (!headerSet.contains(columnHeader)) {
				reportSink.report(
						new MissingMandatoryColumnError(table.getSourceRef(),
								columnHeader),
						table.getSourceInfo());
			}
		}
		for (String columnHeader : deprecatedColumnHeaders) {
			if (headerSet.contains(columnHeader)) {
				reportSink.report(
						new DeprecatedColumnWarning(table.getSourceRef(),
								columnHeader),
						table.getSourceInfo());
			}
		}
		for (int i = 0; i < columnHeaders.size()
				&& i < rawColHeaders.size(); i++) {
			String columnHeader = columnHeaders.get(i);
			String rawColHeader = rawColHeaders.get(i);
			if (!columnHeader.equals(rawColHeader)) {
				reportSink.report(
						new SpaceInColumnWarning(table.getSourceRef(),
								columnHeader, rawColHeader),
						table.getSourceInfo());
			}
		}
	}

	private static class DataLoaderContext
			implements AppendableDao.SourceContext, StreamingValidator.Context,
			GtfsTableDescriptor.Context {

		private DataTable dataTable;
		private DataRow row;
		private DataLoader.Context context;

		private DataLoaderContext(DataTable dataTable,
				DataLoader.Context context) {
			this.dataTable = dataTable;
			this.context = context;
		}

		private void setAndValidateRow(DataRow row) {
			this.row = row;
			int numberOfHeaderColumns = dataTable.getColumnHeaders().size();
			if (row.getRecordCount() != numberOfHeaderColumns) {
				context.getReportSink()
						.report(new InconsistentNumberOfFieldsWarning(
								row.getSourceRef(), row.getRecordCount(),
								numberOfHeaderColumns));
			}
		}

		@Override
		public DataObjectSourceRef getSourceRef() {
			return row == null
					? new DataObjectSourceRef(dataTable.getTableName(), 1L)
					: row.getSourceRef();
		}

		@Override
		public DataObjectSourceInfo getSourceInfo() {
			return row == null ? dataTable.getSourceInfo()
					: row.getSourceInfo();
		}

		@Override
		public ReportSink getReportSink() {
			return context.getReportSink();
		}

		@Override
		public ReadOnlyDao getPartialDao() {
			return context.getReadOnlyDao();
		}

		@Override
		public AppendableDao getAppendableDao() {
			return context.getAppendableDao();
		}

		@Override
		public SourceContext getSourceContext() {
			return this;
		}
	}
}
