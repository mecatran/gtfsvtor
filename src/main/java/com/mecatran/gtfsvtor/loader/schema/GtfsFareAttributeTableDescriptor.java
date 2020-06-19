package com.mecatran.gtfsvtor.loader.schema;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsFareAttribute;
import com.mecatran.gtfsvtor.model.GtfsObject;

@TableDescriptorPolicy(objectClass = GtfsFareAttribute.class, tableName = GtfsFareAttribute.TABLE_NAME, mandatory = false, mandatoryColumns = {
		"fare_id", "price", "currency_type", "payment_method", "transfers" })
public class GtfsFareAttributeTableDescriptor implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsFareAttribute.Builder builder = new GtfsFareAttribute.Builder(
				erow.getString("fare_id"));
		builder.withSourceLineNumber(
				context.getSourceContext().getSourceRef().getLineNumber())
				.withPrice(erow.getDouble("price", true))
				.withCurrencyType(erow.getCurrency("currency_type"))
				.withPaymentMethod(erow.getPaymentMethod("payment_method"))
				.withTransfers(erow.getNumTransfers("transfers"))
				.withAgencyId(GtfsAgency.id(erow.getString("agency_id")))
				.withTransferDuration(
						erow.getInteger("transfer_duration", false));
		GtfsFareAttribute fareAttribute = builder.build();
		context.getAppendableDao().addFareAttribute(fareAttribute,
				context.getSourceContext());
		return fareAttribute;
	}
}
