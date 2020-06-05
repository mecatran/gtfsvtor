package com.mecatran.gtfsvtor.loader.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.loader.DataRow;
import com.mecatran.gtfsvtor.loader.DataTable;
import com.mecatran.gtfsvtor.loader.NamedTabularDataSource;
import com.mecatran.gtfsvtor.model.DataObjectSourceRef;

public class SourceInfoDataReloader {

	public interface PostLoadable {
		/**
		 * @return The list of unloaded source refs to post load once all issues
		 *         have been reported.
		 */
		public Stream<DataObjectSourceRef> getSourceRefsToPostLoad();

		public void addSourceInfo(DataObjectSourceRef ref,
				DataObjectSourceInfo info);
	}

	private NamedTabularDataSource dataSource;
	private boolean verbose = false;

	public SourceInfoDataReloader(NamedTabularDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public SourceInfoDataReloader withVerbose(boolean verbose) {
		this.verbose = verbose;
		return this;
	}

	public void loadSourceInfos(PostLoadable postLoadable) {
		ListMultimap<String, DataObjectSourceRef> refsByTableAndLine = ArrayListMultimap
				.create();
		postLoadable.getSourceRefsToPostLoad()
				.forEach(sr -> refsByTableAndLine.put(sr.getTableName(), sr));
		if (verbose) {
			System.out.println("Reloading source infos from:");
			Multimaps.asMap(refsByTableAndLine)
					.forEach((table, refs) -> System.out.println(
							"  " + table + ": " + refs.size() + " lines"));
		}
		Multimaps.asMap(refsByTableAndLine).entrySet().forEach(kv -> {
			String tableName = kv.getKey();
			List<DataObjectSourceRef> refs = kv.getValue();
			Collections.sort(refs);
			try {
				DataTable dataTable = dataSource.getDataTable(tableName);
				int refIndex = 0;
				DataObjectSourceRef nextRef = refs.get(refIndex);
				if (nextRef.getLineNumber() == 1L) {
					// Special treatment for CSV header
					postLoadable.addSourceInfo(nextRef,
							dataTable.getSourceInfo());
					refIndex++;
					if (refIndex < refs.size()) {
						nextRef = refs.get(refIndex);
					} else {
						// Here only the header is to be post-loaded
						nextRef = null;
					}
				}
				if (nextRef != null) {
					for (DataRow row : dataTable) {
						if (dataTable.getCurrentLineNumber() == nextRef
								.getLineNumber()) {
							postLoadable.addSourceInfo(nextRef,
									row.getSourceInfo());
							refIndex++;
							if (refIndex >= refs.size())
								break;
							nextRef = refs.get(refIndex);
						}
					}
				}
				dataTable.close();
			} catch (IOException e) {
				// This would be strange, as we already managed to load some
				// data once. Break badly anyway
				throw new RuntimeException(e);
			}
		});
	}
}
