package com.mecatran.gtfsvtor.reporting;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;

public class SourceInfoWithFields implements Comparable<SourceInfoWithFields> {

	private DataObjectSourceInfo sourceInfo;
	private Set<String> fieldNames;

	public SourceInfoWithFields(DataObjectSourceInfo sourceInfo) {
		this.sourceInfo = sourceInfo;
	}

	public SourceInfoWithFields(DataObjectSourceInfo sourceInfo,
			String... fieldNames) {
		this.sourceInfo = sourceInfo;
		this.fieldNames = new HashSet<>();
		for (String fieldName : fieldNames) {
			this.fieldNames.add(fieldName);
		}
	}

	public DataObjectSourceInfo getSourceInfo() {
		return sourceInfo;
	}

	public Set<String> getFieldNames() {
		if (fieldNames == null) {
			return Collections.emptySet();
		} else {
			return fieldNames;
		}
	}

	@Override
	public int compareTo(SourceInfoWithFields o) {
		return getSourceInfo().compareTo(o.getSourceInfo());
		// TODO Compare fieldNames if cmp==0
	}
}
