package com.mecatran.gtfsvtor.model;

import java.util.Arrays;
import java.util.List;

public class GtfsFareLegRule
		implements GtfsObject<List<String>>, GtfsObjectWithSourceRef {

	public static final String TABLE_NAME = "fare_leg_rules.txt";

	private GtfsLegGroup.Id legGroupId;
	private GtfsNetwork.Id networkId;
	private GtfsArea.Id fromAreaId;
	private GtfsArea.Id toAreaId;
	private GtfsFareProduct.Id fareProductId;

	private long sourceLineNumber;

	public GtfsFareLegRule.Id getId() {
		return id(networkId, fromAreaId, toAreaId, fareProductId);
	}

	@Override
	public DataObjectSourceRef getSourceRef() {
		return new DataObjectSourceRef(TABLE_NAME, sourceLineNumber);
	}

	public GtfsLegGroup.Id getLegGroupId() {
		return legGroupId;
	}

	public GtfsNetwork.Id getNetworkId() {
		return networkId;
	}

	public GtfsArea.Id getFromAreaId() {
		return fromAreaId;
	}

	public GtfsArea.Id getToAreaId() {
		return toAreaId;
	}

	public GtfsFareProduct.Id getFareProductId() {
		return fareProductId;
	}

	@Override
	public String toString() {
		return "GtfsFareLegRule{fareProductId=" + fareProductId + ",legGroupId="
				+ legGroupId + ",networkId=" + networkId + ",fromAreaId="
				+ fromAreaId + ",toAreaId=" + toAreaId + "}";
	}

	public static Id id(GtfsNetwork.Id networkId, GtfsArea.Id fromAreaId,
			GtfsArea.Id toAreaId, GtfsFareProduct.Id fareProductId) {
		return new Id(networkId, fromAreaId, toAreaId, fareProductId);
	}

	public static class Id extends GtfsCompositeId<String, GtfsFareLegRule> {

		private Id(GtfsNetwork.Id networkId, GtfsArea.Id fromAreaId,
				GtfsArea.Id toAreaId, GtfsFareProduct.Id fareProductId) {
			super(Arrays.asList(networkId, fromAreaId, toAreaId,
					fareProductId));
		}
	}

	public static class Builder {
		private GtfsFareLegRule fareLegRule;

		public Builder() {
			fareLegRule = new GtfsFareLegRule();
		}

		public Builder withSourceLineNumber(long lineNumber) {
			fareLegRule.sourceLineNumber = lineNumber;
			return this;
		}

		public Builder withLegGroupId(GtfsLegGroup.Id legGroupId) {
			fareLegRule.legGroupId = legGroupId;
			return this;
		}

		public Builder withNetworkId(GtfsNetwork.Id networkId) {
			fareLegRule.networkId = networkId;
			return this;
		}

		public Builder withFromAreaId(GtfsArea.Id fromAreaId) {
			fareLegRule.fromAreaId = fromAreaId;
			return this;
		}

		public Builder withToAreaId(GtfsArea.Id toAreaId) {
			fareLegRule.toAreaId = toAreaId;
			return this;
		}

		public Builder withFareProductId(GtfsFareProduct.Id fareProductId) {
			fareLegRule.fareProductId = fareProductId;
			return this;
		}

		public GtfsFareLegRule build() {
			return fareLegRule;
		}
	}
}
