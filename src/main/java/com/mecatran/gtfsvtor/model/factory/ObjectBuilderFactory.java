package com.mecatran.gtfsvtor.model.factory;

import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.model.GtfsStopTime;

public interface ObjectBuilderFactory {

	public GtfsStopTime.Builder getStopTimeBuilder();

	public GtfsShapePoint.Builder getShapePointBuilder();
}
