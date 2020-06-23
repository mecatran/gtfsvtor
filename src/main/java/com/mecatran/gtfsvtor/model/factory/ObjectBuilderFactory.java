package com.mecatran.gtfsvtor.model.factory;

import com.mecatran.gtfsvtor.model.GtfsShapePoint;

public interface ObjectBuilderFactory {

	public GtfsShapePoint.Builder getShapePointBuilder();
}
