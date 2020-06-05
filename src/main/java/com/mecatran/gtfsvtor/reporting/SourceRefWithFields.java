package com.mecatran.gtfsvtor.reporting;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.mecatran.gtfsvtor.model.DataObjectSourceRef;

public class SourceRefWithFields implements Comparable<SourceRefWithFields> {

	private DataObjectSourceRef sourceRef;
	private Set<String> fieldNames;

	public SourceRefWithFields(DataObjectSourceRef sourceRef) {
		this.sourceRef = sourceRef;
	}

	public SourceRefWithFields(DataObjectSourceRef sourceRef,
			String... fieldNames) {
		this.sourceRef = sourceRef;
		this.fieldNames = new HashSet<>();
		for (String fieldName : fieldNames) {
			this.fieldNames.add(fieldName);
		}
	}

	public DataObjectSourceRef getSourceRef() {
		return sourceRef;
	}

	public Set<String> getFieldNames() {
		if (fieldNames == null) {
			return Collections.emptySet();
		} else {
			return fieldNames;
		}
	}

	@Override
	public int compareTo(SourceRefWithFields o) {
		return getSourceRef().compareTo(o.getSourceRef());
		// TODO Compare fieldNames if cmp==0
	}

	@Override
	public String toString() {
		// TODO Nicer formatting, sort field names
		return sourceRef.toString() + (fieldNames == null ? ""
				: (": " + Arrays.toString(fieldNames.toArray())));
	}
}
