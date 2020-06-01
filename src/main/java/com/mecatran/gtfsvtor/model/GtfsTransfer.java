package com.mecatran.gtfsvtor.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mecatran.gtfsvtor.utils.Sextet;

public class GtfsTransfer
		implements GtfsObject<Sextet<String, String, String, String, String, String>> {

	public static final String TABLE_NAME = "transfers.txt";

	// Note: we do not store internal ID here,
	// as it can be rebuilt from the from/to stop ID pair.
	private GtfsStop.Id fromStopId;
	private GtfsStop.Id toStopId;
	private GtfsRoute.Id fromRouteId;
	private GtfsRoute.Id toRouteId;
	private GtfsTrip.Id fromTripId;
	private GtfsTrip.Id toTripId;
	private GtfsTransferType transferType;
	private Integer minTransferTime;

	public GtfsTransfer.Id getId() {
		return id(getFromStopId(), getToStopId(), getFromRouteId(), getToRouteId(), getFromTripId(),
				getToTripId());
	}

	public GtfsStop.Id getFromStopId() {
		return fromStopId;
	}

	public GtfsStop.Id getToStopId() {
		return toStopId;
	}

	public GtfsRoute.Id getFromRouteId() {
		return fromRouteId;
	}

	public GtfsRoute.Id getToRouteId() {
		return toRouteId;
	}

	public GtfsTrip.Id getFromTripId() {
		return fromTripId;
	}

	public GtfsTrip.Id getToTripId() {
		return toTripId;
	}

	public Optional<GtfsTransferType> getType() {
		return Optional.ofNullable(transferType);
	}

	public GtfsTransferType getNonNullType() {
		return transferType == null ? GtfsTransferType.RECOMMENDED
				: transferType;
	}

	public Integer getMinTransferTime() {
		return minTransferTime;
	}

	@Override
	public String toString() {
		return "Transfer{from=" + fromStopId + ",to=" + toStopId + ",type="
				+ transferType + "}";
	}

	public static Id id(GtfsStop.Id fromStopId, GtfsStop.Id toStopId, GtfsRoute.Id fromRouteId,
			GtfsRoute.Id toRouteId, GtfsTrip.Id fromTripId, GtfsTrip.Id toTripId) {
		return fromStopId == null || toStopId == null ?
				null :
				Id.build(fromStopId, toStopId, fromRouteId, toRouteId, fromTripId, toTripId);
	}

	public static class Id extends
			GtfsAbstractId<Sextet<String, String, String, String, String, String>, GtfsTransfer> {

		private Id(Sextet<String, String, String, String, String, String> id) {
			super(id);
		}

		private static Map<Sextet<String, String, String, String, String, String>, Id> CACHE = new HashMap<>();

		private static synchronized Id build(GtfsStop.Id fromStopId, GtfsStop.Id toStopId,
				GtfsRoute.Id fromRouteId, GtfsRoute.Id toRouteId, GtfsTrip.Id fromTripId,
				GtfsTrip.Id toTripId) {
			return CACHE.computeIfAbsent(
					new Sextet<>(fromStopId.getInternalId(), toStopId.getInternalId(),
							fromRouteId != null ? fromRouteId.getInternalId() : null,
							toRouteId != null ? toRouteId.getInternalId() : null,
							fromTripId != null ? fromTripId.getInternalId() : null,
							toTripId != null ? toTripId.getInternalId() : null), Id::new);
		}

		@Override
		public boolean equals(Object obj) {
			return super.doEquals(obj, GtfsTransfer.Id.class);
		}
	}

	public static class Builder {
		private GtfsTransfer transfer;

		public Builder() {
			transfer = new GtfsTransfer();
		}

		public Builder withFromStopId(GtfsStop.Id fromStopId) {
			transfer.fromStopId = fromStopId;
			return this;
		}

		public Builder withToStopId(GtfsStop.Id toStopId) {
			transfer.toStopId = toStopId;
			return this;
		}

		public Builder withFromRouteId(GtfsRoute.Id fromRouteId) {
			transfer.fromRouteId = fromRouteId;
			return this;
		}

		public Builder withToRouteId(GtfsRoute.Id toRouteId) {
			transfer.toRouteId = toRouteId;
			return this;
		}

		public Builder withFromTripId(GtfsTrip.Id fromTripId) {
			transfer.fromTripId = fromTripId;
			return this;
		}

		public Builder withToTripId(GtfsTrip.Id toTripId) {
			transfer.toTripId = toTripId;
			return this;
		}

		public Builder withTransferType(GtfsTransferType transferType) {
			transfer.transferType = transferType;
			return this;
		}

		public Builder withMinTransferTime(Integer minTransferTime) {
			transfer.minTransferTime = minTransferTime;
			return this;
		}

		public GtfsTransfer build() {
			return transfer;
		}
	}
}
