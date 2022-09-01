package com.mecatran.gtfsvtor.loader.schema;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.loader.DataRowConverter.Requiredness;
import com.mecatran.gtfsvtor.model.GtfsFareProduct;
import com.mecatran.gtfsvtor.model.GtfsObject;

@TableDescriptorPolicy(objectClass = GtfsFareProduct.class, tableName = GtfsFareProduct.TABLE_NAME, mandatory = false, mandatoryColumns = {
		"fare_product_id", "amount", "currency" })
public class GtfsFareProductTableDescriptor implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsFareProduct.Builder builder = new GtfsFareProduct.Builder(
				erow.getString("fare_product_id"));
		builder.withSourceLineNumber(
				context.getSourceContext().getSourceRef().getLineNumber())
				.withAmount(erow.getDouble("amount", Requiredness.MANDATORY))
				.withCurrency(erow.getCurrency("currency")).withName(erow
						.getString("fare_product_name", Requiredness.OPTIONAL));
		GtfsFareProduct fareProduct = builder.build();
		context.getAppendableDao().addFareProduct(fareProduct,
				context.getSourceContext());
		return fareProduct;
	}
}
