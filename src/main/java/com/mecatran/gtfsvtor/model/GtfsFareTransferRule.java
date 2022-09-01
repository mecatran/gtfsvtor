package com.mecatran.gtfsvtor.model;

import java.util.List;
import java.util.Optional;

public class GtfsFareTransferRule
		implements GtfsObject<List<String>>, GtfsObjectWithSourceRef {

	public static final String TABLE_NAME = "fare_transfer_rules.txt";

	private GtfsLegGroup.Id fromLegGroupId;
	private GtfsLegGroup.Id toLegGroupId;
	private Integer transferCount;
	private Integer durationLimit;
	private GtfsFareDurationLimitType durationLimitType;
	private GtfsFareTransferType fareTransferType;
	private GtfsFareProduct.Id fareProductId;

	private long sourceLineNumber;

	public GtfsFareTransferRule.Id getId() {
		return id(fromLegGroupId, toLegGroupId, fareProductId, transferCount,
				durationLimit);
	}

	@Override
	public DataObjectSourceRef getSourceRef() {
		return new DataObjectSourceRef(TABLE_NAME, sourceLineNumber);
	}

	public Optional<GtfsLegGroup.Id> getFromLegGroupId() {
		return Optional.ofNullable(fromLegGroupId);
	}

	public Optional<GtfsLegGroup.Id> getToLegGroupId() {
		return Optional.ofNullable(toLegGroupId);
	}

	public Optional<Integer> getTransferCount() {
		return Optional.ofNullable(transferCount);
	}

	public Optional<Integer> getDurationLimit() {
		return Optional.ofNullable(durationLimit);
	}

	public Optional<GtfsFareDurationLimitType> getDurationLimitType() {
		return Optional.ofNullable(durationLimitType);
	}

	public GtfsFareTransferType getFareTransferType() {
		return fareTransferType;
	}

	public Optional<GtfsFareProduct.Id> getFareProductId() {
		return Optional.ofNullable(fareProductId);
	}

	@Override
	public String toString() {
		return "GtfsFareTransferRule{fromLegGroupId=" + fromLegGroupId
				+ ",toLegGroupId=" + toLegGroupId + ",fareProductId="
				+ fareProductId + "}";
	}

	public static Id id(GtfsLegGroup.Id fromLegGroupId,
			GtfsLegGroup.Id toLegGroupId, GtfsFareProduct.Id fareProductId,
			Integer transferCount, Integer durationLimit) {
		return new Id(fromLegGroupId, toLegGroupId, fareProductId,
				transferCount, durationLimit);
	}

	public static class Id
			extends GtfsCompositeId<String, GtfsFareTransferRule> {

		private Id(GtfsLegGroup.Id fromLegGroupId, GtfsLegGroup.Id toLegGroupId,
				GtfsFareProduct.Id fareProductId, Integer transferCount,
				Integer durationLimit) {
			super(nullSafeToString(fromLegGroupId),
					nullSafeToString(toLegGroupId),
					nullSafeToString(fareProductId),
					nullSafeToString(transferCount),
					nullSafeToString(durationLimit));
		}

		private static final String nullSafeToString(Object obj) {
			return obj == null ? null : obj.toString();
		}
	}

	public static class Builder {
		private GtfsFareTransferRule fareTransferRule;

		public Builder() {
			fareTransferRule = new GtfsFareTransferRule();
		}

		public Builder withSourceLineNumber(long lineNumber) {
			fareTransferRule.sourceLineNumber = lineNumber;
			return this;
		}

		public Builder withFromLegGroupId(GtfsLegGroup.Id fromLegGroupId) {
			fareTransferRule.fromLegGroupId = fromLegGroupId;
			return this;
		}

		public Builder withToLegGroupId(GtfsLegGroup.Id toLegGroupId) {
			fareTransferRule.toLegGroupId = toLegGroupId;
			return this;
		}

		public Builder withTransferCount(Integer transferCount) {
			fareTransferRule.transferCount = transferCount;
			return this;
		}

		public Builder withDurationLimit(Integer durationLimit) {
			fareTransferRule.durationLimit = durationLimit;
			return this;
		}

		public Builder withDurationLimitType(
				GtfsFareDurationLimitType durationLimitType) {
			fareTransferRule.durationLimitType = durationLimitType;
			return this;
		}

		public Builder withFareTransferType(
				GtfsFareTransferType fareTransferType) {
			fareTransferRule.fareTransferType = fareTransferType;
			return this;
		}

		public Builder withFareProductId(GtfsFareProduct.Id fareProductId) {
			fareTransferRule.fareProductId = fareProductId;
			return this;
		}

		public GtfsFareTransferRule build() {
			return fareTransferRule;
		}
	}
}
