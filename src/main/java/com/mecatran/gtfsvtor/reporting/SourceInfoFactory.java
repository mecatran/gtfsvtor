package com.mecatran.gtfsvtor.reporting;

import java.util.Optional;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;
import com.mecatran.gtfsvtor.model.DataObjectSourceRef;

public interface SourceInfoFactory {

	public Optional<DataObjectSourceInfo> getSourceInfo(
			DataObjectSourceRef ref);

	public void registerSourceInfo(DataObjectSourceRef ref,
			DataObjectSourceInfo sourceInfo);

	public void registerSourceRef(DataObjectSourceRef ref);

}
