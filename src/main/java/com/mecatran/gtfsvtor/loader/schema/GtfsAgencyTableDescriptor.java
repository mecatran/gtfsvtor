package com.mecatran.gtfsvtor.loader.schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsObject;

@TableDescriptorPolicy(objectClass = GtfsAgency.class, tableName = GtfsAgency.TABLE_NAME, mandatory = true)
public class GtfsAgencyTableDescriptor implements GtfsTableDescriptor {

	@Override
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context) {
		GtfsAgency.Builder builder = new GtfsAgency.Builder(
				erow.getString("agency_id"));

		builder.withSourceLineNumber(
				context.getSourceContext().getSourceRef().getLineNumber())
				.withName(erow.getString("agency_name", true))
				.withUrl(erow.getString("agency_url", true))
				.withTimezone(erow.getTimeZone("agency_timezone", true))
				.withLang(erow.getLocale("agency_lang", false))
				.withPhone(erow.getString("agency_phone"))
				.withFareUrl(erow.getString("agency_fare_url"))
				.withEmail(erow.getString("agency_email"));
		GtfsAgency agency = builder.build();
		context.getAppendableDao().addAgency(agency,
				context.getSourceContext());
		return agency;
	}

	@Override
	public List<String> getMandatoryColumns(int nObjects) {
		List<String> ret = new ArrayList<>(
				Arrays.asList("agency_name", "agency_url", "agency_timezone"));
		// agency_id is mandatory only if having more than one agency
		if (nObjects >= 2) {
			ret.add("agency_id");
		}
		return ret;
	}
}
