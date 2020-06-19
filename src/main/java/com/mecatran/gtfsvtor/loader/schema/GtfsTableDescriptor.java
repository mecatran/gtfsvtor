package com.mecatran.gtfsvtor.loader.schema;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.mecatran.gtfsvtor.dao.AppendableDao;
import com.mecatran.gtfsvtor.loader.DataRowConverter;
import com.mecatran.gtfsvtor.model.GtfsObject;
import com.mecatran.gtfsvtor.model.factory.ObjectBuilderFactory;
import com.mecatran.gtfsvtor.utils.Annotations;

public interface GtfsTableDescriptor {

	public interface Context {

		public AppendableDao getAppendableDao();

		public AppendableDao.SourceContext getSourceContext();

		public ObjectBuilderFactory getBuilderFactory();
	}

	public default String getTableName() {
		return Annotations.getAnnotation(TableDescriptorPolicy.class,
				String.class, this, TableDescriptorPolicy::tableName);
	}

	public default boolean isTableMandatory(Set<String> loadedTables) {
		return Annotations.getAnnotation(TableDescriptorPolicy.class,
				Boolean.class, this, TableDescriptorPolicy::mandatory);
	}

	public default Class<? extends GtfsObject<?>> getObjectClass() {
		@SuppressWarnings("unchecked")
		Class<? extends GtfsObject<?>> ret = Annotations.getAnnotation(
				TableDescriptorPolicy.class, Class.class, this,
				TableDescriptorPolicy::objectClass);
		return ret;
	}

	public default List<String> getMandatoryColumns(int nObjects) {
		return Arrays.asList(Annotations.getAnnotation(
				TableDescriptorPolicy.class, String[].class, this,
				TableDescriptorPolicy::mandatoryColumns));
	}

	/**
	 * Parse from a data table row, build the object, and save it to the DAO.
	 * 
	 * @param erow The row data converter to get and convert row values from.
	 * @param context The context from where to get the DAO to save the data to.
	 * @return The object parsed from the row.
	 */
	public GtfsObject<?> parseAndSave(DataRowConverter erow, Context context);

}
