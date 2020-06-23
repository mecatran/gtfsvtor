package com.mecatran.gtfsvtor.model.factory;

import com.mecatran.gtfsvtor.model.GtfsShapePoint;

@Deprecated
public interface ObjectBuilderFactory {

	public GtfsShapePoint.Builder getShapePointBuilder();
}
