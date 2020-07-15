package com.mecatran.gtfsvtor.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;

import com.mecatran.gtfsvtor.model.GtfsTranslation;
import com.mecatran.gtfsvtor.model.GtfsTranslationTable;

public class TestInternedTranslation {

	@Test
	public void testRecordId() {
		GtfsTranslation.Builder builder = new InternedGtfsTranslation.Builder();
		builder.withSourceLineNumber(1000L);
		builder.withTableName(GtfsTranslationTable.AGENCY);
		builder.withFieldName("agency_name");
		builder.withLanguage(Locale.FRENCH);
		builder.withTranslation("Agence Régionale de Transport d'Hündarœ");
		builder.withRecordId("AGENCY1");
		GtfsTranslation tr1 = builder.build();

		assertEquals("translations.txt", tr1.getSourceRef().getTableName());
		assertEquals(1000L, tr1.getSourceRef().getLineNumber());
		assertEquals(GtfsTranslationTable.AGENCY, tr1.getTableName());
		assertEquals("agency_name", tr1.getFieldName());
		assertEquals(Locale.FRENCH, tr1.getLanguage());
		assertEquals("Agence Régionale de Transport d'Hündarœ",
				tr1.getTranslation());
		assertEquals("AGENCY1", tr1.getRecordId().get());
		assertFalse(tr1.getRecordSubId().isPresent());
		assertFalse(tr1.getFieldValue().isPresent());
	}

	@Test
	public void testFieldValue() {
		GtfsTranslation.Builder builder = new InternedGtfsTranslation.Builder();
		builder.withTableName(GtfsTranslationTable.ROUTES);
		builder.withFieldName("route_long_name");
		builder.withLanguage(Locale.FRENCH);
		builder.withTranslation("Seconde ligne");
		builder.withFieldValue("Second route");
		GtfsTranslation tr1 = builder.build();

		assertFalse(tr1.getRecordId().isPresent());
		assertFalse(tr1.getRecordSubId().isPresent());
		assertEquals("Second route", tr1.getFieldValue().get());
	}

	@Test
	public void testInterning() {
		InternedGtfsTranslation.Builder builder = new InternedGtfsTranslation.Builder();
		builder.withTableName(GtfsTranslationTable.ROUTES);
		builder.withFieldName("route_long_name");
		builder.withLanguage(Locale.FRENCH);
		builder.withTranslation("Seconde ligne");
		builder.withFieldValue("Second route");
		InternedGtfsTranslation tr1 = builder.build();

		builder = new InternedGtfsTranslation.Builder();
		builder.withTableName(GtfsTranslationTable.ROUTES);
		builder.withFieldName("route_long_name");
		builder.withLanguage(Locale.FRENCH);
		builder.withTranslation("Troisième ligne");
		builder.withFieldValue("Third route");
		InternedGtfsTranslation tr2 = builder.build();

		assertTrue(tr1.getId().getColRef() == tr2.getId().getColRef());

		builder = new InternedGtfsTranslation.Builder();
		builder.withTableName(GtfsTranslationTable.ROUTES);
		builder.withFieldName("route_long_name");
		builder.withLanguage(Locale.ITALIAN);
		builder.withTranslation("Terza linea");
		builder.withFieldValue("Third route");
		InternedGtfsTranslation tr3 = builder.build();

		assertTrue(tr2.getId().getValRef() == tr3.getId().getValRef());
	}

}
