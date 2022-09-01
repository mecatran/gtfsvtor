package com.mecatran.gtfsvtor.model;

import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GtfsFareProduct
		implements GtfsObject<String>, GtfsObjectWithSourceRef {

	public static final String TABLE_NAME = "fare_products.txt";

	private GtfsFareProduct.Id id;
	private String name;
	private Double amount;
	private Currency currency;

	private long sourceLineNumber;

	public GtfsFareProduct.Id getId() {
		return id;
	}

	@Override
	public DataObjectSourceRef getSourceRef() {
		return new DataObjectSourceRef(TABLE_NAME, sourceLineNumber);
	}

	public Optional<String> getName() {
		return Optional.ofNullable(name);
	}

	public Double getAmount() {
		return amount;
	}

	public Currency getCurrency() {
		return currency;
	}

	@Override
	public String toString() {
		return "GtfsFareProduct{id=" + id + ",name='" + name + "',amount="
				+ amount + ",currency=" + currency + "}";
	}

	public static Id id(String id) {
		return id == null || id.isEmpty() ? null : Id.build(id);
	}

	public static class Id extends GtfsAbstractId<String, GtfsFareProduct> {

		private Id(String id) {
			super(id);
		}

		private static Map<String, Id> CACHE = new HashMap<>();

		private static synchronized Id build(String id) {
			return CACHE.computeIfAbsent(id, Id::new);
		}

		@Override
		public boolean equals(Object obj) {
			return super.doEquals(obj, GtfsFareProduct.Id.class);
		}
	}

	public static class Builder {
		private GtfsFareProduct fareProduct;

		public Builder(String id) {
			fareProduct = new GtfsFareProduct();
			fareProduct.id = id(id);
		}

		public Builder withSourceLineNumber(long lineNumber) {
			fareProduct.sourceLineNumber = lineNumber;
			return this;
		}

		public Builder withName(String name) {
			fareProduct.name = name;
			return this;
		}

		public Builder withAmount(Double amount) {
			fareProduct.amount = amount;
			return this;
		}

		public Builder withCurrency(Currency currency) {
			fareProduct.currency = currency;
			return this;
		}

		public GtfsFareProduct build() {
			return fareProduct;
		}
	}
}
