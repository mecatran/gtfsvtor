package com.mecatran.gtfsvtor.model.factory;

import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.model.GtfsStopTime;
import com.mecatran.gtfsvtor.model.impl.SimpleGtfsShapePoint;
import com.mecatran.gtfsvtor.model.impl.SimpleGtfsStopTime;
import com.mecatran.gtfsvtor.model.impl.SmallGtfsShapePoint;
import com.mecatran.gtfsvtor.model.impl.SmallGtfsStopTime;

public class DefaultObjectBuilderFactory implements ObjectBuilderFactory {

	private boolean smallStopTime = false;
	private boolean smallShapePoint = false;

	public DefaultObjectBuilderFactory() {
	}

	public DefaultObjectBuilderFactory withSmallStopTime(
			boolean smallStopTime) {
		this.smallStopTime = smallStopTime;
		return this;
	}

	public DefaultObjectBuilderFactory withSmallShapePoint(
			boolean smallShapePoint) {
		this.smallShapePoint = smallShapePoint;
		return this;
	}

	public GtfsStopTime.Builder getStopTimeBuilder() {
		return smallStopTime ? new SmallGtfsStopTime.Builder()
				: new SimpleGtfsStopTime.Builder();
	}

	public GtfsShapePoint.Builder getShapePointBuilder() {
		return smallShapePoint ? new SmallGtfsShapePoint.Builder()
				: new SimpleGtfsShapePoint.Builder();
	}
}
