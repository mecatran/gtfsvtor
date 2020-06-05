package com.mecatran.gtfsvtor.model;

import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

public class GtfsFareAttribute
		implements GtfsObject<String>, GtfsObjectWithSourceRef {

	public static final String TABLE_NAME = "fare_attributes.txt";

	private GtfsFareAttribute.Id id;
	private Double price;
	private Currency currencyType;
	private GtfsPaymentMethod paymentMethod;
	private GtfsNumTransfers transfers;
	private GtfsAgency.Id agencyId;
	private Integer transferDuration;

	private long sourceLineNumber;

	public GtfsFareAttribute.Id getId() {
		return id;
	}

	@Override
	public DataObjectSourceRef getSourceRef() {
		return new DataObjectSourceRef(TABLE_NAME, sourceLineNumber);
	}

	public Double getPrice() {
		return price;
	}

	public Currency getCurrencyType() {
		return currencyType;
	}

	public GtfsPaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public GtfsNumTransfers getTransfers() {
		return transfers;
	}

	public GtfsAgency.Id getAgencyId() {
		return agencyId;
	}

	public Integer getTransferDuration() {
		return transferDuration;
	}

	@Override
	public String toString() {
		return "GtfsFareAttribute{id=" + id + ",price=" + price + ",currency="
				+ currencyType + ",payment=" + paymentMethod + ",transfers="
				+ transfers + ",transferDuration=" + transferDuration + "}";
	}

	public static Id id(String id) {
		return id == null || id.isEmpty() ? null : Id.build(id);
	}

	public static class Id extends GtfsAbstractId<String, GtfsFareAttribute> {

		private Id(String id) {
			super(id);
		}

		private static Map<String, Id> CACHE = new HashMap<>();

		private static synchronized Id build(String id) {
			return CACHE.computeIfAbsent(id, Id::new);
		}

		@Override
		public boolean equals(Object obj) {
			return super.doEquals(obj, GtfsFareAttribute.Id.class);
		}
	}

	public static class Builder {
		private GtfsFareAttribute fareAttribute;

		public Builder(String id) {
			fareAttribute = new GtfsFareAttribute();
			fareAttribute.id = id(id);
		}

		public Builder withSourceLineNumber(long lineNumber) {
			fareAttribute.sourceLineNumber = lineNumber;
			return this;
		}

		public Builder withPrice(Double price) {
			fareAttribute.price = price;
			return this;
		}

		public Builder withCurrencyType(Currency currencyType) {
			fareAttribute.currencyType = currencyType;
			return this;
		}

		public Builder withPaymentMethod(GtfsPaymentMethod paymentMethod) {
			fareAttribute.paymentMethod = paymentMethod;
			return this;
		}

		public Builder withTransfers(GtfsNumTransfers transfers) {
			fareAttribute.transfers = transfers;
			return this;
		}

		public Builder withAgencyId(GtfsAgency.Id agencyId) {
			fareAttribute.agencyId = agencyId;
			return this;
		}

		public Builder withTransferDuration(Integer transferDuration) {
			fareAttribute.transferDuration = transferDuration;
			return this;
		}

		public GtfsFareAttribute build() {
			return fareAttribute;
		}
	}
}
