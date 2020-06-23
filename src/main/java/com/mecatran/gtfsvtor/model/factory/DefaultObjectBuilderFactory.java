package com.mecatran.gtfsvtor.model.factory;

import com.mecatran.gtfsvtor.model.GtfsShapePoint;
import com.mecatran.gtfsvtor.model.impl.SimpleGtfsShapePoint;
import com.mecatran.gtfsvtor.model.impl.SmallGtfsShapePoint;

@Deprecated
public class DefaultObjectBuilderFactory implements ObjectBuilderFactory {

	private boolean smallShapePoint = false;

	public DefaultObjectBuilderFactory() {
	}

	public DefaultObjectBuilderFactory withSmallShapePoint(
			boolean smallShapePoint) {
		this.smallShapePoint = smallShapePoint;
		return this;
	}

	@Override
	public GtfsShapePoint.Builder getShapePointBuilder() {
		return smallShapePoint ? new SmallGtfsShapePoint.Builder()
				: new SimpleGtfsShapePoint.Builder();
	}
}
