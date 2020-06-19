package com.mecatran.gtfsvtor.loader.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
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
import com.mecatran.gtfsvtor.model.factory.ObjectBuilderFactory;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.DuplicatedColumnError;
import com.mecatran.gtfsvtor.reporting.issues.EmptyTableError;
import com.mecatran.gtfsvtor.reporting.issues.InconsistentNumberOfFieldsWarning;
import com.mecatran.gtfsvtor.reporting.issues.InvalidCharsetError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidEncodingError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryColumnError;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryTableError;
import com.mecatran.gtfsvtor.reporting.issues.TableIOError;
import com.mecatran.gtfsvtor.reporting.issues.UnknownFileInfo;
import com.mecatran.gtfsvtor.reporting.issues.UnrecognizedColumnInfo;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

public class GtfsDataLoader implements DataLoader {

	private NamedTabularDataSource dataSource;
	private GtfsTableSchema tableSchema;
	private ObjectBuilderFactory builderFactory;

	public GtfsDataLoader(NamedTabularDataSource dataSource,
			GtfsTableSchema tableSchema, ObjectBuilderFactory builderFactory) {
		this.dataSource = dataSource;
		this.tableSchema = tableSchema;
		this.builderFactory = builderFactory;
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

		DataLoaderContext sourceContext = new DataLoaderContext(table, context,
				builderFactory);
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
			nObjects++;
		}
		checkMandatoryColumns(context.getReportSink(), table, tableDescriptor
				.getMandatoryColumns(nObjects).toArray(new String[0]));

		closeTable(table, context.getReportSink());
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

	private void closeTable(DataTable table, ReportSink reportSink) {
		if (table.isEmpty()) {
			reportSink.report(new EmptyTableError(
					table.getTableSourceInfo().getTableName()));
		}
		for (String unknownColumn : table.getUnreadColumnHeaders()) {
			reportSink.report(new UnrecognizedColumnInfo(
					new DataObjectSourceRef(table.getTableName(), 1L),
					unknownColumn));
			if (unknownColumn.chars().anyMatch(c -> c == 0xFFFD || c == 0)) {
				reportSink.report(new InvalidEncodingError(
						new DataObjectSourceRef(table.getTableName(), 1L),
						unknownColumn, unknownColumn));
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

	private void checkMandatoryColumns(ReportSink reportSink, DataTable table,
			String... columnHeaders) {
		Set<String> headerSet = new HashSet<>();
		for (String columnHeader : table.getColumnHeaders()) {
			if (headerSet.contains(columnHeader)) {
				reportSink
						.report(new DuplicatedColumnError(table.getSourceRef(),
								columnHeader), table.getSourceInfo());
			} else {
				headerSet.add(columnHeader);
			}
		}
		for (String columnHeader : columnHeaders) {
			if (!headerSet.contains(columnHeader)) {
				reportSink.report(
						new MissingMandatoryColumnError(table.getSourceRef(),
								columnHeader),
						table.getSourceInfo());
			}
		}
	}

	private static class DataLoaderContext
			implements AppendableDao.SourceContext, StreamingValidator.Context,
			GtfsTableDescriptor.Context {

		private DataTable dataTable;
		private DataRow row;
		private ReportSink reportSink;
		private AppendableDao appendableDao;
		private ReadOnlyDao readOnlyDao;
		private DataLoader.Context context;
		private ObjectBuilderFactory builderFactory;

		// TODO Remove this
		@Deprecated
		private DataLoaderContext(DataTable dataTable, ReportSink reportSink,
				ReadOnlyDao readOnlyDao) {
			this.dataTable = dataTable;
			this.reportSink = reportSink;
			this.readOnlyDao = readOnlyDao;
		}

		private DataLoaderContext(DataTable dataTable,
				DataLoader.Context context,
				ObjectBuilderFactory builderFactory) {
			this.dataTable = dataTable;
			this.context = context;
			// TODO Save context instead of all fields
			this.reportSink = context.getReportSink();
			this.appendableDao = context.getAppendableDao();
			this.readOnlyDao = context.getReadOnlyDao();
			this.builderFactory = builderFactory;
		}

		private void setAndValidateRow(DataRow row) {
			this.row = row;
			int numberOfHeaderColumns = dataTable.getColumnHeaders().size();
			if (row.getRecordCount() != numberOfHeaderColumns) {
				reportSink.report(new InconsistentNumberOfFieldsWarning(
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
			return reportSink;
		}

		@Override
		public ReadOnlyDao getPartialDao() {
			return readOnlyDao;
		}

		@Override
		public AppendableDao getAppendableDao() {
			return appendableDao;
		}

		@Override
		public SourceContext getSourceContext() {
			return this;
		}

		@Override
		public ObjectBuilderFactory getBuilderFactory() {
			return builderFactory;
		}
	}
}
