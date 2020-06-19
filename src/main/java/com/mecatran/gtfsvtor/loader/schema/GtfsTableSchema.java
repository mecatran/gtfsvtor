package com.mecatran.gtfsvtor.loader.schema;

import java.util.List;

public interface GtfsTableSchema {

	/**
	 * Warning! The order of the returned list is important, as streaming
	 * validators rely on the partially loaded DAO to check for references, when
	 * possible. For exemple when loading a trip we check if the calendar or
	 * shape ID exists, if defined. etc...
	 * 
	 * @return The list of table descriptors to load data from.
	 */
	public List<GtfsTableDescriptor> getTableDescriptors();
}
