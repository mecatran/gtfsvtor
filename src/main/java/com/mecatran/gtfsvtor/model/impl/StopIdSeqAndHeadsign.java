package com.mecatran.gtfsvtor.model.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsTripStopSequence;

/**
 * This class is used for the SmallGtfsStopTime implementation.
 * 
 * Since lots of quadruplet (stop ID, stop sequence, stop headsigns, shape dist
 * traveled) will be the same across stop times, we intern the common values to
 * share the same object when possible. Many trips sharing the same pattern will
 * often use the same values; stop headsign are often null, shape dist traveled
 * are often not specified.
 */
public class StopIdSeqAndHeadsign {

	/* Denormalize the encapsulated values a bit */
	private GtfsStop.Id stopId;
	private GtfsTripStopSequence stopSequence;
	private String stopHeadsign;
	private double shapeDistTraveled;

	private static Map<StopIdSeqAndHeadsign, StopIdSeqAndHeadsign> CACHE = new HashMap<>();

	public static class Builder {
		private StopIdSeqAndHeadsign sish;

		public Builder() {
			this.sish = new StopIdSeqAndHeadsign();
		}

		public Builder withStopId(GtfsStop.Id stopId) {
			sish.stopId = stopId;
			return this;
		}

		public Builder withStopSequence(GtfsTripStopSequence stopSequence) {
			sish.stopSequence = stopSequence;
			return this;
		}

		public Builder withStopHeadsign(String stopHeadsign) {
			sish.stopHeadsign = stopHeadsign;
			return this;
		}

		public Builder withShapeDistTraveled(Double shapeDistTraveled) {
			sish.shapeDistTraveled = shapeDistTraveled == null ? Double.NaN
					: shapeDistTraveled;
			return this;
		}

		public StopIdSeqAndHeadsign build() {
			// This will intern the values
			StopIdSeqAndHeadsign interned = CACHE.computeIfAbsent(sish,
					sish -> sish);
			return interned;
		}
	}

	public GtfsStop.Id getStopId() {
		return stopId;
	}

	public GtfsTripStopSequence getStopSequence() {
		return stopSequence;
	}

	public String getStopHeadsign() {
		return stopHeadsign;
	}

	public Double getShapeDistTraveled() {
		return Double.isNaN(shapeDistTraveled) ? null : shapeDistTraveled;
	}

	@Override
	public int hashCode() {
		return Objects.hash(stopId == null ? null : stopId.getInternalId(),
				stopSequence == null ? null : stopSequence.getSequence(),
				stopHeadsign);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof StopIdSeqAndHeadsign)) {
			return false;
		}
		StopIdSeqAndHeadsign other = (StopIdSeqAndHeadsign) obj;
		// Note: == does *not* work with NaN
		if (!Objects.equals(stopId, other.stopId))
			return false;
		if (!Objects.equals(stopSequence, other.stopSequence))
			return false;
		if (!Objects.equals(stopHeadsign, other.stopHeadsign))
			return false;

		boolean nan1 = Double.isNaN(shapeDistTraveled);
		boolean nan2 = Double.isNaN(other.shapeDistTraveled);
		if (nan1 != nan2)
			return false;
		if (nan1)
			return true;
		return shapeDistTraveled == other.shapeDistTraveled;
	}
}
