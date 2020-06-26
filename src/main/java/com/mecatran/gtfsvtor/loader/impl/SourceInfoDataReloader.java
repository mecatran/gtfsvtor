package com.mecatran.gtfsvtor.loader.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.loader.DataRow;
import com.mecatran.gtfsvtor.loader.DataTable;
import com.mecatran.gtfsvtor.loader.NamedTabularDataSource;
import com.mecatran.gtfsvtor.model.DataObjectSourceRef;
import com.mecatran.gtfsvtor.reporting.SourceInfoFactory;

public class SourceInfoDataReloader implements SourceInfoFactory {

	private NamedTabularDataSource dataSource;
	private boolean verbose = false;

	private Map<DataObjectSourceRef, DataObjectSourceInfo> sourceInfos = new HashMap<>();
	private SetMultimap<String, DataObjectSourceRef> sourceRefsToLoad = HashMultimap
			.create();

	public SourceInfoDataReloader(NamedTabularDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public SourceInfoDataReloader withVerbose(boolean verbose) {
		this.verbose = verbose;
		return this;
	}

	@Override
	public DataObjectSourceInfo getSourceInfo(DataObjectSourceRef ref) {
		if (!sourceRefsToLoad.get(ref.getTableName()).isEmpty()) {
			reloadSourceInfos(ref.getTableName());
		}
		DataObjectSourceInfo info = sourceInfos.get(ref);
		if (info == null) {
			// TODO Accept this and return dummy source info
			throw new IllegalArgumentException(
					"Source info not found for " + ref);
		}
		return info;
	}

	@Override
	public void registerSourceInfo(DataObjectSourceRef ref,
			DataObjectSourceInfo sourceInfo) {
		sourceInfos.put(ref, sourceInfo);
		sourceRefsToLoad.get(ref.getTableName()).remove(ref);
	}

	@Override
	public void registerSourceRef(DataObjectSourceRef ref) {
		if (!sourceInfos.containsKey(ref))
			sourceRefsToLoad.put(ref.getTableName(), ref);
	}

	private void reloadSourceInfos(String tableName) {
		List<DataObjectSourceRef> refs = new ArrayList<>(
				sourceRefsToLoad.get(tableName));
		Collections.sort(refs);
		sourceRefsToLoad.removeAll(tableName);
		if (verbose) {
			System.out
					.println(String.format("Reloading %d source infos from %s",
							refs.size(), tableName));
		}
		try {
			DataTable dataTable = dataSource.getDataTable(tableName);
			int refIndex = 0;
			DataObjectSourceRef nextRef = refs.get(refIndex);
			if (nextRef.getLineNumber() == 1L) {
				// Special treatment for CSV header
				sourceInfos.put(nextRef, dataTable.getSourceInfo());
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
						sourceInfos.put(nextRef, row.getSourceInfo());
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
	}
}
