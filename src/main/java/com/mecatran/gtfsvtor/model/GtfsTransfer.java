package com.mecatran.gtfsvtor.model;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class GtfsTransfer implements GtfsObject<GtfsTransfer.Id> {

	public static final String TABLE_NAME = "transfers.txt";

	private Id id;
	private GtfsTransferType transferType;
	private Integer minTransferTime;

	public GtfsTransfer.Id getId() {
		return id;
	}

	public GtfsStop.Id getFromStopId() {
		return id.getFromStopId();
	}

	public GtfsStop.Id getToStopId() {
		return id.getToStopId();
	}

	public GtfsRoute.Id getFromRouteId() {
		return id.getFromRouteId();
	}

	public GtfsRoute.Id getToRouteId() {
		return id.getToRouteId();
	}

	public GtfsTrip.Id getFromTripId() {
		return id.getFromTripId();
	}

	public GtfsTrip.Id getToTripId() {
		return id.getToTripId();
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
		return "Transfer{from=" + id.getFromStopId() + ",to=" + id.getToStopId()
				+ ",type=" + transferType + "}";
	}

	public static Id id(GtfsStop.Id fromStopId, GtfsStop.Id toStopId) {
		return new Id.Builder().withFromStopId(fromStopId)
				.withToStopId(toStopId).build();
	}

	public static Id id(GtfsStop.Id fromStopId, GtfsStop.Id toStopId,
			GtfsRoute.Id fromRouteId, GtfsRoute.Id toRouteId,
			GtfsTrip.Id fromTripId, GtfsTrip.Id toTripId) {
		return new Id.Builder().withFromStopId(fromStopId)
				.withToStopId(toStopId).withFromRouteId(fromRouteId)
				.withToRouteId(toRouteId).withFromTripId(fromTripId)
				.withToTripId(toTripId).build();
	}

	public static class Id implements GtfsId<Id, GtfsTransfer> {

		private GtfsStop.Id fromStopId;
		private GtfsStop.Id toStopId;
		private GtfsRoute.Id fromRouteId;
		private GtfsRoute.Id toRouteId;
		private GtfsTrip.Id fromTripId;
		private GtfsTrip.Id toTripId;

		private Id() {
		}

		@Override
		public Id getInternalId() {
			return this;
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

		private static ConcurrentMap<Id, Id> CACHE = new ConcurrentHashMap<>();

		private Id intern() {
			return CACHE.computeIfAbsent(this, Function.identity());
		}

		@Override
		public int hashCode() {
			return Objects.hash(fromStopId, toStopId, fromRouteId, toRouteId,
					fromTripId, toTripId);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (obj == this)
				return true;
			if (!(obj instanceof Id)) {
				return false;
			}
			Id other = (Id) obj;
			return Objects.equals(fromStopId, other.fromStopId)
					&& Objects.equals(toStopId, other.toStopId)
					&& Objects.equals(fromRouteId, other.fromRouteId)
					&& Objects.equals(toRouteId, other.toRouteId)
					&& Objects.equals(fromTripId, other.fromTripId)
					&& Objects.equals(toTripId, other.toTripId);
		}

		@Override
		public String toString() {
			/*
			 * Be careful, this toString() will end-up in reports. Be
			 * consistent.
			 */
			return String.format("{%s, %s, %s, %s, %s, %s}", fromStopId,
					toStopId, fromRouteId, toRouteId, fromTripId, toTripId);
		}

		private static class Builder {
			private Id id;

			private Builder() {
				id = new Id();
			}

			public Builder withFromStopId(GtfsStop.Id fromStopId) {
				id.fromStopId = fromStopId;
				return this;
			}

			public Builder withToStopId(GtfsStop.Id toStopId) {
				id.toStopId = toStopId;
				return this;
			}

			public Builder withFromRouteId(GtfsRoute.Id fromRouteId) {
				id.fromRouteId = fromRouteId;
				return this;
			}

			public Builder withToRouteId(GtfsRoute.Id toRouteId) {
				id.toRouteId = toRouteId;
				return this;
			}

			public Builder withFromTripId(GtfsTrip.Id fromTripId) {
				id.fromTripId = fromTripId;
				return this;
			}

			public Builder withToTripId(GtfsTrip.Id toTripId) {
				id.toTripId = toTripId;
				return this;
			}

			public Id build() {
				return id.intern();
			}
		}
	}

	public static class Builder {
		private GtfsTransfer transfer;
		private Id.Builder idBuilder;

		public Builder() {
			transfer = new GtfsTransfer();
			idBuilder = new Id.Builder();
		}

		public Builder withFromStopId(GtfsStop.Id fromStopId) {
			idBuilder.withFromStopId(fromStopId);
			return this;
		}

		public Builder withToStopId(GtfsStop.Id toStopId) {
			idBuilder.withToStopId(toStopId);
			return this;
		}

		public Builder withFromRouteId(GtfsRoute.Id fromRouteId) {
			idBuilder.withFromRouteId(fromRouteId);
			return this;
		}

		public Builder withToRouteId(GtfsRoute.Id toRouteId) {
			idBuilder.withToRouteId(toRouteId);
			return this;
		}

		public Builder withFromTripId(GtfsTrip.Id fromTripId) {
			idBuilder.withFromTripId(fromTripId);
			return this;
		}

		public Builder withToTripId(GtfsTrip.Id toTripId) {
			idBuilder.withToTripId(toTripId);
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
			transfer.id = idBuilder.build();
			return transfer;
		}
	}
}
