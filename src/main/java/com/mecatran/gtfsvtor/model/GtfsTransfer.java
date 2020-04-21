package com.mecatran.gtfsvtor.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mecatran.gtfsvtor.utils.Pair;

public class GtfsTransfer implements GtfsObject<Pair<String, String>> {

	public static final String TABLE_NAME = "transfers.txt";

	// Note: we do not store internal ID here,
	// as it can be rebuilt from the from/to stop ID pair.
	private GtfsStop.Id fromStopId;
	private GtfsStop.Id toStopId;
	private GtfsTransferType transferType;
	private Integer minTransferTime;

	public GtfsTransfer.Id getId() {
		return id(getFromStopId(), getToStopId());
	}

	public GtfsStop.Id getFromStopId() {
		return fromStopId;
	}

	public GtfsStop.Id getToStopId() {
		return toStopId;
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

	public static Id id(GtfsStop.Id fromStopId, GtfsStop.Id toStopId) {
		return fromStopId == null || toStopId == null ? null
				: Id.build(fromStopId, toStopId);
	}

	public static class Id
			extends GtfsAbstractId<Pair<String, String>, GtfsTransfer> {

		private Id(Pair<String, String> id) {
			super(id);
		}

		private static Map<Pair<String, String>, Id> CACHE = new HashMap<>();

		private static synchronized Id build(GtfsStop.Id fromStopId,
				GtfsStop.Id toStopId) {
			return CACHE.computeIfAbsent(new Pair<>(fromStopId.getInternalId(),
					toStopId.getInternalId()), Id::new);
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
